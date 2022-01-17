package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.exception.BusinessException;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.ReturnMethodEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.*;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {
    @Resource
    private DictService dictService;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend();

        lend.setUserId(borrowInfo.getUserId());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setAmount(borrowInfo.getAmount());
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setReturnMethod(borrowInfo.getReturnMethod());

        lend.setTitle(borrowInfoApprovalVO.getTitle());
        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());

        // 费率可能发生修改，以审批后费率为准
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate()
                .divide(new BigDecimal(100), 2, BigDecimal.ROUND_UP)
        );
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate()
                .divide(new BigDecimal(100), 2, BigDecimal.ROUND_UP)
        );

        lend.setLendNo(LendNoUtils.getLendNo());
        lend.setLowestAmount(new BigDecimal(100)); // 最低投资金额
        lend.setInvestAmount(new BigDecimal(0)); // 已投资金额
        lend.setInvestNum(0); // 已投资人数
        lend.setPublishDate(LocalDateTime.now());

        // 起息日期 & 结束日期
        LocalDate lendStartDate = LocalDate.parse(
                borrowInfoApprovalVO.getLendStartDate(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );

        lend.setLendStartDate(lendStartDate);
        lend.setLendEndDate(lendStartDate.plusMonths(borrowInfo.getPeriod()));

        // 平台预期收益 = 标的金额 * (年化 / 12 * 期数)
        lend.setExpectAmount(
                lend.getAmount().multiply(
                        lend.getServiceRate()
                                .divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN)
                                .multiply(new BigDecimal(lend.getPeriod()))
                )
        );

        lend.setRealAmount(new BigDecimal(0));
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        lend.setCheckTime(LocalDateTime.now()); // 审核时间
        lend.setCheckAdminId(1L); // 审核管理员 ID

        baseMapper.insert(lend);
    }

    @Override
    public List<Lend> selectList() {
        List<Lend> lendList = baseMapper.selectList(null);

        lendList.forEach(lend -> {
            lend.getParam().put(
                    "returnMethod",
                    dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod())
            );
            lend.getParam().put("status", LendStatusEnum.getMsgByStatus(lend.getStatus()));
        });

        return lendList;
    }

    @Override
    public Map<String, Object> getLendDetail(Long id) {
        // 查询标的信息并封装 Lend
        Lend lend = baseMapper.selectById(id);

        lend.getParam().put(
                "returnMethod",
                dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod())
        );
        lend.getParam().put("status", LendStatusEnum.getMsgByStatus(lend.getStatus()));

        // 查询借款人详情 BorrowerDetailVO
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(
                borrowerMapper.selectOne(new QueryWrapper<Borrower>().eq("user_id", lend.getUserId())).getId()
        );

        Map<String, Object> map = new HashMap<>();

        map.put("lend", lend);
        map.put("borrower", borrowerDetailVO);

        return map;
    }

    @Override
    public BigDecimal getInterestCount(
            BigDecimal invest,
            BigDecimal yearRate,
            Integer totalMonth,
            Integer returnMethod
    ) {
        BigDecimal interestCount;

        if (returnMethod.equals(ReturnMethodEnum.ONE.getMethod())) {
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalMonth);
        } else if (returnMethod.equals(ReturnMethodEnum.TWO.getMethod())) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalMonth);
        } else if (returnMethod.equals(ReturnMethodEnum.THREE.getMethod())) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalMonth);
        } else { // 不会出现除上述四种以外其他情况
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalMonth);
        }

        return interestCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void makeLoan(Long id) {
        Lend lend = baseMapper.selectById(id);

        // 提交远程请求
        BigDecimal realAmount = lend.getServiceRate()
                .divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN) // 月化
                .multiply(lend.getInvestAmount()) // 金额
                .multiply(new BigDecimal(lend.getPeriod())); // 时长
        String agentBillNo = LendNoUtils.getLoanNo();

        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentProjectCode", lend.getLendNo()); //标的编号
        paramMap.put("agentBillNo", agentBillNo);
        paramMap.put("mchFee", realAmount);
        paramMap.put("note", "满标放款");
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);

        // 放款失败
        if (!"0000".equals(result.getString("resultCode")))
            throw new BusinessException(result.getString("resultMsg"));

        // 放款成功

//        （1）标的状态和标的平台收益
        lend.setRealAmount(realAmount);
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setPaymentTime(LocalDateTime.now());

        baseMapper.updateById(lend);

//        （2）给借款账号转入金额
        String bindCode = userInfoMapper.selectById(lend.getUserId()).getBindCode();
        BigDecimal voteAmt = new BigDecimal(result.getString("voteAmt"));

        userAccountMapper.updateAccount(
                bindCode,
                voteAmt,
                new BigDecimal(0)
        );

//        （3）增加借款交易流水
        transFlowService.saveTransFlow(new TransFlowBO(
                agentBillNo,
                bindCode,
                voteAmt,
                TransTypeEnum.BORROW_BACK,
                "项目放款，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle()
        ));

//        （4）解冻并扣除投资人资金，需要投资记录的状态为已支付
        List<LendItem> lendItemList = lendItemService.selectListByLendId(id, 1);

        lendItemList.forEach(lendItem -> {
            String investBindCode = userInfoMapper.selectById(lendItem.getInvestUserId()).getBindCode();
            BigDecimal investAmount = lendItem.getInvestAmount();

            userAccountMapper.updateAccount(
                    investBindCode,
                    new BigDecimal(0),
                    investAmount.negate()
            );

//        （5）增加投资人交易流水
            transFlowService.saveTransFlow(new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "项目放款，冻结资金转出，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle()
            ));
        });

//        （6）生成借款人还款计划和出借人回款计划
        repaymentPlan(lend);
    }

    /**
     * 还款计划（借款人还款）
     *
     * @param lend 标的
     */
    private void repaymentPlan(Lend lend) {
        int n = lend.getPeriod();

        List<LendReturn> lendReturnList = new ArrayList<>(n);

        for (int i = 1; i <= n; i++) {
            LendReturn lendReturn = new LendReturn();

            lendReturn.setLendId(lend.getId());
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setBaseAmount(lend.getInvestAmount());
            lendReturn.setCurrentPeriod(i);
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setReturnMethod(lend.getReturnMethod());

            // principal & interest & total 三个字段需要回款计划

            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i));
            lendReturn.setOverdue(false);
            lendReturn.setLast(i == n); // 防止除不尽导致的还钱总和达不到最终标的金额，最后一个月直接减去已还的钱作为需要的钱
            lendReturn.setStatus(0);

            lendReturnList.add(lendReturn);
        }

        // 保存记录以获得 MySQL 插入记录时回填到对象的 ID
        lendReturnService.saveBatch(lendReturnList);

        // 构建还款期数与还款计划 ID 对应 Map
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );

        // 所有投资人回款计划
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();

        // 获取所有已支付投资记录
        List<LendItem> lendItemList = lendItemService.selectListByLendId(lend.getId(), 1);

        // 为每条投资记录计算其相应的回款计划
        for (LendItem lendItem : lendItemList)
            // 获取 当前投资记录的回款计划 并加入 所有投资人回款计划
            lendItemReturnAllList.addAll(returnInvest(lendItem.getId(), lendReturnMap, lend));

        // 构建 principal & interest & total 三个字段
        // 每期应还的本金 principal / 利息 interest / 总金额 total 相加即为最终 lend_return 表的数据
        for (LendReturn lendReturn : lendReturnList) {
            lendReturn.setPrincipal(lendItemReturnAllList.stream()
                    .filter(lendItemReturn -> lendItemReturn.getLendReturnId().equals(lendReturn.getId()))
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            lendReturn.setInterest(lendItemReturnAllList.stream()
                    .filter(lendItemReturn -> lendItemReturn.getLendReturnId().equals(lendReturn.getId()))
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            lendReturn.setTotal(lendItemReturnAllList.stream()
                    .filter(lendItemReturn -> lendItemReturn.getLendReturnId().equals(lendReturn.getId()))
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );
        }

        // 更新字段记录
        lendReturnService.updateBatchById(lendReturnList);
    }

    /**
     * 回款计划（指定 lend_item 表 ID 的一笔投资记录的投资人回款）
     *
     * @param lendItemId    标的投资记录 ID
     * @param lendReturnMap 还款期数与还款计划 ID 对应 Map （lend_item_return 表字段所需数据）
     * @param lend          标的
     * @return 回款计划列表
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {
        // num 个投资人
        // 每期还款按投资金额拆分成 num 份
        LendItem lendItem = lendItemService.getById(lendItemId);

        BigDecimal amount = lendItem.getInvestAmount();
        BigDecimal yearRate = lendItem.getLendYearRate();
        int totalMonth = lend.getPeriod();

        Map<Integer, BigDecimal> mapInterest, mapPrincipal; // 期数 -> 利息 / 本金

        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().equals(ReturnMethodEnum.ONE.getMethod())) {
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth); //利息
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth); //本金
        } else if (lend.getReturnMethod().equals(ReturnMethodEnum.TWO.getMethod())) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().equals(ReturnMethodEnum.THREE.getMethod())) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        List<LendItemReturn> lendItemReturnList = new ArrayList<>(); // 回款计划列表

        // 对 利息 或 本金 映射进行遍历均可，期数相同
        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();
            Long lendReturnId = lendReturnMap.get(currentPeriod);

            LendItemReturn lendItemReturn = new LendItemReturn();

            lendItemReturn.setLendReturnId(lendReturnId);
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());

            if (lendItemReturnList.size() > 0 && currentPeriod.equals(lend.getPeriod())) {
                lendItemReturn.setPrincipal( // 本金
                        lendItem.getInvestAmount().subtract(
                                lendItemReturnList.stream()
                                        .map(LendItemReturn::getPrincipal)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        )
                );
                lendItemReturn.setInterest( // 利息
                        lendItem.getExpectAmount().subtract(
                                lendItemReturnList.stream()
                                        .map(LendItemReturn::getInterest)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        )
                );
            } else {
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }

            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest())); // 本金 + 利息
            lendItemReturn.setFee(new BigDecimal(0));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);

            lendItemReturnList.add(lendItemReturn);
        }

        lendItemReturnService.saveBatch(lendItemReturnList);

        return lendItemReturnList;
    }
}
