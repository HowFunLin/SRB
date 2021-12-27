package com.atguigu.srb.core.mapper;

import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface DictMapper extends BaseMapper<Dict> {
    /**
     * 批量插入缓存中数据
     *
     * @param list 缓存
     */
    void insertBatch(List<ExcelDictDTO> list);
}
