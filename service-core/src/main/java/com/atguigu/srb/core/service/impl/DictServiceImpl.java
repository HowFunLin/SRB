package com.atguigu.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.core.listener.ExcelDictDTOListener;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-19
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Resource
    private RedisTemplate<String, List<Dict>> redisTemplate;

    /**
     * 导入 Excel 文件内容并利用 Listener 写入数据库
     *
     * @param inputStream Excel 文件输入流
     */
    @Transactional(rollbackFor = Exception.class) // 默认 RuntimeException 才回滚
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel 导入成功");
    }

    /**
     * 导出为 Excel 文件形式
     */
    @Override
    public List<ExcelDictDTO> listDictData() {
        List<Dict> dictionaries = baseMapper.selectList(null);
        List<ExcelDictDTO> result = new ArrayList<>(dictionaries.size());

        dictionaries.forEach(dict -> {
            ExcelDictDTO dto = new ExcelDictDTO();

            BeanUtils.copyProperties(dict, dto);

            result.add(dto);
        });

        return result;
    }

    /**
     * 数据字典页面展示
     *
     * @param parentId 父节点 ID
     */
    @Override
    public List<Dict> listByParentId(Long parentId) {
        List<Dict> dictionaries;

        // Redis 服务器异常之后仍可从 MySQL 查询数据而不应该直接结束
        try {
            dictionaries = redisTemplate.opsForValue().get("srb:core:dictionaries:" + parentId);

            if (dictionaries != null) {
                log.info("从 Redis 获取到数据字典");
                return dictionaries;
            }
        } catch (Exception e) {
            log.error("Redis 服务器读取异常：{}", ExceptionUtils.getStackTrace(e));
        }

        dictionaries = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));

        dictionaries.forEach(dict -> dict.setHasChildren(hasChildren(dict.getId())));

        try {
            redisTemplate.opsForValue().set("srb:core:dictionaries:" + parentId, dictionaries, 3, TimeUnit.MINUTES);
            log.info("数据字典缓存到 Redis");
        } catch (Exception e) {
            log.error("Redis 服务器写入异常：{}", ExceptionUtils.getStackTrace(e));
        }

        return dictionaries;
    }

    /**
     * 判断当前节点是否拥有子节点
     *
     * @param id 节点 ID
     */
    public Boolean hasChildren(Long id) {
        return this.count(new QueryWrapper<Dict>().eq("parent_id", id)) > 0;
    }
}
