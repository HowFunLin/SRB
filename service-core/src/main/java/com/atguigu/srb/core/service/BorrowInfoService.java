package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 借款信息表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface BorrowInfoService extends IService<BorrowInfo> {
    /**
     * 获取借款额度
     *
     * @param userId 当前登录用户 ID
     * @return 借款额度
     */
    BigDecimal getBorrowAmount(Long userId);

    /**
     * 保存借款信息
     *
     * @param borrowInfo 借款信息
     * @param userId     当前登录用户 ID
     */
    void saveBorrowInfo(BorrowInfo borrowInfo, Long userId);

    /**
     * 通过当前登录用户 ID 获取借款申请状态
     *
     * @param userId 当前登录用户 ID
     * @return 借款申请状态对应的整数
     */
    Integer getStatusByUserId(Long userId);
}
