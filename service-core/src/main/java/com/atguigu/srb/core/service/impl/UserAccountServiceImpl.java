package com.atguigu.srb.core.service.impl;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.FormHelper;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.atguigu.srb.core.service.TransFlowService;
import com.atguigu.srb.core.service.UserAccountService;
import com.atguigu.srb.core.service.UserBindService;
import com.atguigu.srb.core.util.LendNoUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserAccountService userAccountService;

    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {
        String bindCode = userInfoMapper.selectById(userId).getBindCode();

        Assert.notEmpty(bindCode, ResponseEnum.USER_NO_BIND_ERROR);

        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getChargeNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("chargeAmt", chargeAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        return FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(Map<String, Object> map) {
        // 幂等性判断（根据流水 trans_flow 记录的账单号是否已经存在判断），防止汇付宝平台重试回调导致多次充值

        // MySQL 定义 UNIQUE 索引从数据库层面保证幂等性
        String agentBillNo = (String) map.get("agentBillNo");

        // 已经存在相同记录则直接终止方法
        if (transFlowService.isSavedTransFlow(agentBillNo))
            return;

        // 账户处理
        String bindCode = (String) map.get("bindCode");
        BigDecimal chargeAmt = new BigDecimal((String) map.get("chargeAmt"));

        baseMapper.updateAccount(bindCode, chargeAmt, new BigDecimal(0));

        // 记录账户流水
        TransFlowBO transFlowBO = new TransFlowBO(agentBillNo, bindCode, chargeAmt, TransTypeEnum.RECHARGE, "账户余额充值");

        transFlowService.saveTransFlow(transFlowBO);
    }

    @Override
    public BigDecimal getAccount(Long userId) {
        return baseMapper.selectOne(new QueryWrapper<UserAccount>().eq("user_id", userId)).getAmount();
    }

    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {
        Assert.isTrue(
                userAccountService.getAccount(userId).compareTo(fetchAmt) >= 0,
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR
        );

        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode", userBindService.getBindCodeByUserId(userId));
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        return FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
    }

    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {
        String agentBillNo = (String) paramMap.get("agentBillNo");

        // 幂等判断
        if (transFlowService.isSavedTransFlow(agentBillNo))
            return;

        String bindCode = (String) paramMap.get("bindCode");
        BigDecimal fetchAmt = new BigDecimal((String) paramMap.get("fetchAmt"));

        // 账户同步
        baseMapper.updateAccount(bindCode, fetchAmt.negate(), new BigDecimal(0));

        // 交易流水
        transFlowService.saveTransFlow(new TransFlowBO(
                agentBillNo,
                bindCode,
                fetchAmt,
                TransTypeEnum.WITHDRAW,
                "提现")
        );
    }
}
