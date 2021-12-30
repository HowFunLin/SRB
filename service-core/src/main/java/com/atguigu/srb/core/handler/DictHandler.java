package com.atguigu.srb.core.handler;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableScheduling
public class DictHandler {
    @Resource
    private CanalConnector connector;

    @Resource
    private RedisTemplate<String, List<Dict>> redisTemplate;

    @Resource
    private DictService dictService;

    @Resource
    private DictMapper dictMapper;

    @PostConstruct
    public void init() {
        connector.connect();
        connector.subscribe(); // 订阅并以服务端规则为准
        connector.rollback(); // 回滚到尚未应答的变更位置
    }

    @Scheduled(fixedRate = 1000)
    public void handle() throws InvalidProtocolBufferException {
        Message message = connector.getWithoutAck(10);

        long batchId = message.getId();

        for (CanalEntry.Entry entry : message.getEntries()) {
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

            CanalEntry.EventType eventType = rowChange.getEventType();

            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.INSERT) {
                    updateRedisData(rowData.getAfterColumnsList());
                } else if (eventType == CanalEntry.EventType.DELETE) {
                    updateRedisData(rowData.getBeforeColumnsList());
                } else if (eventType == CanalEntry.EventType.UPDATE) {
                    updateRedisData(rowData.getAfterColumnsList());
                }
            }
        }

        connector.ack(batchId);
    }

    private void updateRedisData(List<CanalEntry.Column> columnsList) {
        for (CanalEntry.Column column : columnsList) {
            if ("parent_id".equals(column.getName())) {
                String parentId = column.getValue();

                List<Dict> dictList = dictMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));

                dictList.forEach(dict -> dict.setHasChildren(dictService.hasChildren(dict.getId())));
                redisTemplate.opsForValue().set("srb:core:dictList:" + parentId, dictList, 5, TimeUnit.MINUTES);

                log.info("parent_id 为 {} 的缓存已得到更新", parentId);
            }
        }
    }
}
