package com.atguigu.srb.core.service.impl;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.FormHelper;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.LendItemMapper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.vo.InvestVO;
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
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {
    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserBindService userBindService;

    @Resource
    private LendService lendService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Override
    public String commitInvest(InvestVO investVO) {
        // 校验
        Lend lend = lendMapper.selectById(investVO.getLendId());
        Long investUserId = investVO.getInvestUserId();

        // 判断是否募资中
        Assert.isTrue(lend.getStatus().equals(LendStatusEnum.INVEST_RUN.getStatus()), ResponseEnum.LEND_INVEST_ERROR);

        // 不允许超投标
        Assert.isTrue(
                lend.getInvestAmount()
                        .add(new BigDecimal(investVO.getInvestAmount())).compareTo(lend.getAmount()) <= 0,
                ResponseEnum.LEND_FULL_SCALE_ERROR
        );

        // 投资金额不能大于余额
        Assert.isTrue(
                userAccountService.getAccount(investUserId)
                        .compareTo(new BigDecimal(investVO.getInvestAmount())) >= 0,
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR
        );

        // 生成标的的投资记录
        LendItem lendItem = new LendItem();
        String lendItemNo = LendNoUtils.getLendItemNo();

        lendItem.setInvestUserId(investUserId);
        lendItem.setInvestName(investVO.getInvestName());
        lendItem.setLendItemNo(lendItemNo);
        lendItem.setLendId(investVO.getLendId());

        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount()));

        lendItem.setLendYearRate(lend.getLendYearRate());
        lendItem.setInvestTime(LocalDateTime.now());
        lendItem.setLendStartDate(lend.getLendStartDate());
        lendItem.setLendEndDate(lend.getLendEndDate());

        lendItem.setExpectAmount(
                lendService.getInterestCount(
                        lendItem.getInvestAmount(),
                        lendItem.getLendYearRate(),
                        lend.getPeriod(),
                        lend.getReturnMethod()
                )
        );

        lendItem.setRealAmount(new BigDecimal(0));
        lendItem.setStatus(0);

        baseMapper.insert(lendItem);

        // 封装提交至汇付宝的参数
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", userBindService.getBindCodeByUserId(investUserId));
        paramMap.put("benefitBindCode", userBindService.getBindCodeByUserId(lend.getUserId()));
        paramMap.put("agentProjectCode", lend.getLendNo());//项目标号
        paramMap.put("agentProjectName", lend.getTitle());

        // 在资金托管平台上的投资订单的唯一编号，要和 lendItemNo 保持一致。
        paramMap.put("agentBillNo", lendItemNo);//订单编号
        paramMap.put("voteAmt", investVO.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount()); //标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        return FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(Map<String, Object> paramMap) {
        // 校验幂等性
        String agentBillNo = (String) paramMap.get("agentBillNo");

        if (transFlowService.isSavedTransFlow(agentBillNo))
            return;

        // 修改账户余额、冻结金额、投资状态
        String voteAmtStr = (String) paramMap.get("voteAmt");
        BigDecimal voteAmt = new BigDecimal(voteAmtStr);
        String bindCode = (String) paramMap.get("voteBindCode");

        userAccountMapper.updateAccount(
                bindCode,
                new BigDecimal("-" + voteAmtStr),
                voteAmt
        );

        // lend_item 表中 lend_item_no 字段与汇付宝 agent_bill_no 字段相等
        LendItem lendItem = baseMapper.selectOne(new QueryWrapper<LendItem>().eq("lend_item_no", agentBillNo));

        lendItem.setStatus(1);

        baseMapper.updateById(lendItem);

        // 修改标的的投资人数、已投金额
        Lend lend = lendMapper.selectById(lendItem.getLendId());

        lend.setInvestNum(lend.getInvestNum() + 1);
        lend.setInvestAmount(lend.getInvestAmount().add(lendItem.getInvestAmount()));

        lendMapper.updateById(lend);

        // 新增交易流水
        transFlowService.saveTransFlow(new TransFlowBO(
                agentBillNo,
                bindCode,
                voteAmt,
                TransTypeEnum.INVEST_LOCK,
                "投资项目编号" + lend.getLendNo() + "，项目名称：" + lend.getTitle()
        ));
    }
}
