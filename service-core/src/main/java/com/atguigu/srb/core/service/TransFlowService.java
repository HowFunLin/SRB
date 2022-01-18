package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.TransFlow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface TransFlowService extends IService<TransFlow> {
    /**
     * 保存交易流水
     *
     * @param transFlowBO 交易流水信息 BO
     */
    void saveTransFlow(TransFlowBO transFlowBO);

    /**
     * 判断流水是否已经被保存
     *
     * @param agentBillNo 流水号
     * @return 流水是否已经被保存
     */
    boolean isSavedTransFlow(String agentBillNo);

    /**
     * 根据当前登录用户 ID 获取资金流水列表
     *
     * @param userId 当前登录用户 ID
     * @return 资金流水列表
     */
    List<TransFlow> selectByUserId(Long userId);
}
