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
 * @since 2021-12-27
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Resource
    private RedisTemplate<String, List<Dict>> redisTemplate;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel 文件中的数据导入完成");
    }

    @Override
    public List<ExcelDictDTO> listDictData() {
        List<Dict> dictList = baseMapper.selectList(null);
        List<ExcelDictDTO> excelDictDTOList = new ArrayList<>(dictList.size()); // 将 Dict 列表转换成 ExcelDictDTO 列表

        dictList.forEach(dict -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();

            BeanUtils.copyProperties(dict, excelDictDTO); // 复制相同属性值到 ExcelDictDTO
            excelDictDTOList.add(excelDictDTO);
        });

        return excelDictDTOList;
    }

    @Override
    public List<Dict> listByParentId(Long parentId) {
        List<Dict> dictList;

        try {
            dictList = redisTemplate.opsForValue().get("srb:core:dictList:" + parentId);

            // 缓存查询成功则直接返回
            if (dictList != null) {
                log.info("从 Redis 中读取缓存");
                return dictList;
            }
        } catch (Exception e) { // 需修改 Axios 错误等待时间更长一些，防止 Redis 宕机导致 AJAX 请求超时
            log.error("Redis服务器异常：" + ExceptionUtils.getStackTrace(e)); // 继续执行后续代码，不应该因为 Redis 服务器异常而不继续查询 MySQL
        }

        log.info("从 MySQL 中查询数据");

        dictList = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));

        dictList.forEach(dict -> dict.setHasChildren(hasChildren(dict.getId())));

        try {
            redisTemplate.opsForValue().set("srb:core:dictList:" + parentId, dictList, 5, TimeUnit.MINUTES);
            log.info("数据字典存入缓存");
        } catch (Exception e) {
            log.error("Redis服务器异常：" + ExceptionUtils.getStackTrace(e));// 继续执行后续代码，不应该因为 Redis 服务器异常而不返回查询结果
        }

        return dictList;
    }

    @Override
    public boolean hasChildren(Long id) {
        return this.count(new QueryWrapper<Dict>().eq("parent_id", id)) > 0;
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        return this.listByParentId(baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", dictCode)).getId());
    }

    @Override
    public String getNameByParentDictCodeAndValue(String dictCode, Integer value) {
        Dict parent = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", dictCode));

        if (parent == null)
            return "";

        Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>()
                .eq("parent_id", parent.getId())
                .eq("value", value)
        );

        if (dict == null)
            return "";

        return dict.getName();
    }
}
