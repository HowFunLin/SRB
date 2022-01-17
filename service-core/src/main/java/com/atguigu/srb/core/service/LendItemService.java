package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.vo.InvestVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface LendItemService extends IService<LendItem> {
    /**
     * 构建投资信息自动提交表单
     *
     * @param investVO 封装投资信息 VO
     * @return 自动提交表单
     */
    String commitInvest(InvestVO investVO);

    /**
     * 汇付宝 回调方法
     *
     * @param paramMap 参数映射 Map
     */
    void notify(Map<String, Object> paramMap);

    /**
     * 根据标的 ID 获取标的投资记录列表
     *
     * @param lendId 标的 ID
     * @param status 标的投资记录状态
     * @return 标的投资记录列表
     */
    List<LendItem> selectListByLendId(Long lendId, Integer status);
}
