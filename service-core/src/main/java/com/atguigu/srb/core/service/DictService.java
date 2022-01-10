package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface DictService extends IService<Dict> {
    /**
     * 导入 Excel 文件对应的输入流中的数据
     *
     * @param inputStream 输入流
     */
    void importData(InputStream inputStream);

    /**
     * 导出 Dict 表中的所有数据封装为 List
     *
     * @return Dict 表中的所有数据
     */
    List<ExcelDictDTO> listDictData();

    /**
     * 根据上级 ID 获取数据字典中对应数据
     *
     * @param parentId 上级 ID
     * @return parent ID 为对应值的行
     */
    List<Dict> listByParentId(Long parentId);

    /**
     * 根据当前行 ID 判断是否存在子行
     *
     * @param id 当前行 ID
     * @return 是否存在子行
     */
    boolean hasChildren(Long id);

    /**
     * 根据 dictCode 查询其子节点
     *
     * @param dictCode 数据字典二级节点名称
     * @return dictCode 对应所有子节点
     */
    List<Dict> findByDictCode(String dictCode);

    /**
     * 根据 父节点字典编码 dict_code 和 当前节点值 value 获取 当前节点 name
     *
     * @param dictCode 父节点字典编码
     * @param value    当前节点值
     * @return 当前节点 name
     */
    String getNameByParentDictCodeAndValue(String dictCode, Integer value);
}
