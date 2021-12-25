package com.atguigu.srb.core.handler;

import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.service.impl.DictServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 父节点（类型）稳定不变，当前仅当子节点（选项）发生修改时，利用 Canal 通知 Redis 更新缓存
 */
@Slf4j
@Component
@CanalTable("dict")
public class DictHandler implements EntryHandler<Dict> {
    @Resource
    private DictMapper dictMapper;

    @Resource
    private DictServiceImpl dictService;

    @Resource
    private RedisTemplate<String, List<Dict>> redisTemplate;

    @Override
    public void insert(Dict dict) {
        Long parentId = dict.getParentId();

        List<Dict> dictionaries = dictMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));

        dictionaries.forEach(d -> d.setHasChildren(dictService.hasChildren(d.getId())));

        try {
            redisTemplate.opsForValue().set("srb:core:dictionaries:" + parentId, dictionaries, 3, TimeUnit.MINUTES);
            log.info("MySQL 数据字典新增记录，缓存到 Redis");
        } catch (Exception e) {
            log.error("Redis 服务器写入异常：{}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void update(Dict before, Dict after) {
        Long parentId = after.getParentId();

        List<Dict> dictionaries = dictMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));

        dictionaries.forEach(d -> d.setHasChildren(dictService.hasChildren(d.getId())));

        try {
            redisTemplate.opsForValue().set("srb:core:dictionaries:" + parentId, dictionaries, 3, TimeUnit.MINUTES);
            log.info("MySQL 数据字典更新记录，缓存到 Redis");
        } catch (Exception e) {
            log.error("Redis 服务器写入异常：{}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void delete(Dict dict) {
        Long parentId = dict.getParentId();

        List<Dict> dictionaries = dictMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));

        dictionaries.forEach(d -> d.setHasChildren(dictService.hasChildren(d.getId())));

        try {
            redisTemplate.opsForValue().set("srb:core:dictionaries:" + parentId, dictionaries, 3, TimeUnit.MINUTES);
            log.info("MySQL 数据字典新增记录，缓存到 Redis");
        } catch (Exception e) {
            log.error("Redis 服务器写入异常：{}", ExceptionUtils.getStackTrace(e));
        }
    }
}
