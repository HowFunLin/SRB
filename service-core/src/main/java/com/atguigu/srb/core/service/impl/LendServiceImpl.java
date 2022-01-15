package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.ReturnMethodEnum;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.atguigu.srb.core.service.LendService;
import com.atguigu.srb.core.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100), 2, BigDecimal.ROUND_UP));
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100), 2, BigDecimal.ROUND_UP));

        lend.setLendNo(LendNoUtils.getLendNo());
        lend.setLowestAmount(new BigDecimal(100)); // 最低投资金额
        lend.setInvestAmount(new BigDecimal(0)); // 已投资金额
        lend.setInvestNum(0); // 已投资人数
        lend.setPublishDate(LocalDateTime.now());

        // 起息日期 & 结束日期
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

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
            lend.getParam().put("returnMethod", dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod()));
            lend.getParam().put("status", LendStatusEnum.getMsgByStatus(lend.getStatus()));
        });

        return lendList;
    }

    @Override
    public Map<String, Object> getLendDetail(Long id) {
        // 查询标的信息并封装 Lend
        Lend lend = baseMapper.selectById(id);

        lend.getParam().put("returnMethod", dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod()));
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
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalMonth, Integer returnMethod) {
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
}
