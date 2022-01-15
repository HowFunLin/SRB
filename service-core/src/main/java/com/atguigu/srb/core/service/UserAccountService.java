package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface UserAccountService extends IService<UserAccount> {
    /**
     * 提交充值
     *
     * @param chargeAmt 充值金额
     * @param userId    当前用户 ID
     * @return 自动提交汇付宝表单字符串
     */
    String commitCharge(BigDecimal chargeAmt, Long userId);

    /**
     * 汇付宝 用户充值回调接口
     *
     * @param map 回调参数
     */
    void notify(Map<String, Object> map);

    /**
     * 获取用户余额信息
     *
     * @param userId 当前用户 ID
     * @return 用户账户余额
     */
    BigDecimal getAccount(Long userId);
}
