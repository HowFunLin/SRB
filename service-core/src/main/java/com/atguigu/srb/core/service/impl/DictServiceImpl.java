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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    @Transactional(rollbackFor = Exception.class) // 默认 RuntimeException 才回滚
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel 导入成功");
    }

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

    @Override
    public List<Dict> listByParentId(Long parentId) {
        List<Dict> dictionaries = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));

        dictionaries.forEach(dict -> {
            dict.setHasChildren(hasChildren(dict.getId()));
        });

        return dictionaries;
    }

    private Boolean hasChildren(Long id) {
        return this.count(new QueryWrapper<Dict>().eq("parent_id", id)) > 0;
    }


}
