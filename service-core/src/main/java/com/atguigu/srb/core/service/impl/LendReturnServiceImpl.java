package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.FormHelper;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.*;
import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.entity.LendItemReturn;
import com.atguigu.srb.core.pojo.entity.LendReturn;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.util.LendNoUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class LendReturnServiceImpl extends ServiceImpl<LendReturnMapper, LendReturn> implements LendReturnService {
    @Resource
    private LendItemReturnService lendItemReturnService;

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private LendItemReturnMapper lendItemReturnMapper;

    @Resource
    private LendItemMapper lendItemMapper;

    @Override
    public List<LendReturn> selectByLendId(Long lendId) {
        return baseMapper.selectList(new QueryWrapper<LendReturn>().eq("lend_id", lendId));
    }

    @Override
    public String commitReturn(Long lendReturnId, Long userId) {
        LendReturn lendReturn = baseMapper.selectById(lendReturnId);

        Assert.isTrue(
                userAccountService.getAccount(userId).compareTo(lendReturn.getTotal()) >= 0,
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR
        );

        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentGoodsName", lendMapper.selectById(lendReturn.getLendId()).getTitle());
        paramMap.put("agentBatchNo", lendReturn.getReturnNo()); // 还款批次
        paramMap.put("fromBindCode", userBindService.getBindCodeByUserId(userId));
        paramMap.put("totalAmt", lendReturn.getTotal());
        paramMap.put("note", "");

        // 组装还款明细（对应的回款 Map）
        paramMap.put("data", JSONObject.toJSONString(lendItemReturnService.addReturnDetail(lendReturnId)));

        paramMap.put("voteFeeAmt", BigDecimal.ZERO);
        paramMap.put("notifyUrl", HfbConst.BORROW_RETURN_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.BORROW_RETURN_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        return FormHelper.buildForm(HfbConst.BORROW_RETURN_URL, paramMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(Map<String, Object> paramMap) {
        String agentBatchNo = (String) paramMap.get("agentBatchNo");

        // 幂等性判断
        if (transFlowService.isSavedTransFlow(agentBatchNo))
            return;

        // 获取还款状态
        LendReturn lendReturn = baseMapper.selectOne(
                new QueryWrapper<LendReturn>().eq("return_no", agentBatchNo)
        );

        // 更新还款状态
        lendReturn.setStatus(1);
        lendReturn.setFee(new BigDecimal((String) paramMap.get("voteFeeAmt")));
        lendReturn.setRealReturnTime(LocalDateTime.now());

        baseMapper.updateById(lendReturn);

        Lend lend = lendMapper.selectById(lendReturn.getLendId());

        // 更新标的信息（最后一次还款需更新标的状态）
        if (lendReturn.getLast()) {
            lend.setStatus(LendStatusEnum.PAY_OK.getStatus());
            lendMapper.updateById(lend);
        }

        BigDecimal totalAmt = new BigDecimal((String) paramMap.get("totalAmt"));
        String bindCode = userBindService.getBindCodeByUserId(lend.getUserId());

        // 借款账号转出金额
        userAccountMapper.updateAccount(bindCode, totalAmt.negate(), BigDecimal.ZERO);

        // 借款人交易流水
        transFlowService.saveTransFlow(
                new TransFlowBO(
                        agentBatchNo,
                        bindCode,
                        totalAmt,
                        TransTypeEnum.RETURN_DOWN,
                        "借款人还款扣减，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle())
        );

        // 获取回款明细
        for (LendItemReturn lendItemReturn : lendItemReturnService.selectLendItemReturnList(lendReturn.getId())) {
            // 更新回款状态
            lendItemReturn.setStatus(1);
            lendItemReturn.setRealReturnTime(LocalDateTime.now());

            lendItemReturnMapper.updateById(lendItemReturn);

            // 更新出借信息
            LendItem lendItem = lendItemMapper.selectById(lendItemReturn.getLendItemId());

            lendItem.setRealAmount(lendItemReturn.getInterest()); // 实际收益为所得利息

            lendItemMapper.updateById(lendItem);

            String investBindCode = userBindService.getBindCodeByUserId(lendItemReturn.getInvestUserId());
            BigDecimal total = lendItemReturn.getTotal(); // 总金额

            // 投资账号转入金额
            userAccountMapper.updateAccount(investBindCode, total, BigDecimal.ZERO);

            // 投资账号交易流水
            transFlowService.saveTransFlow(
                    new TransFlowBO(
                            LendNoUtils.getReturnItemNo(),
                            investBindCode,
                            total,
                            TransTypeEnum.INVEST_BACK,
                            "还款到账，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle())
            );
        }
    }
}
