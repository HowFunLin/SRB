package com.atguigu.srb.core.service.impl;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.BorrowInfoStatusEnum;
import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.UserBindEnum;
import com.atguigu.srb.core.mapper.BorrowInfoMapper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.IntegralGradeMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowInfoService;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private IntegralGradeMapper integralGradeMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        // 获取用户积分
        UserInfo userInfo = userInfoMapper.selectById(userId);

        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR); // 防止未登录伪造请求

        Integer integral = userInfo.getIntegral();

        // 根据积分查询额度
        IntegralGrade integralGrade = integralGradeMapper.selectOne(
                new QueryWrapper<IntegralGrade>()
                        .le("integral_start", integral)
                        .ge("integral_end", integral)
        );

        if (integralGrade == null)
            return new BigDecimal("0");

        return integralGrade.getBorrowAmount();
    }

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {
        // 获取用户信息
        UserInfo userInfo = userInfoMapper.selectById(userId);

        // 判断用户绑定状态
        Assert.equals(userInfo.getBindStatus(), UserBindEnum.BIND_OK.getStatus(), ResponseEnum.USER_NO_BIND_ERROR);

        // 判断借款人额度审批状态
        Assert.equals(userInfo.getBorrowAuthStatus(), BorrowerStatusEnum.AUTH_OK.getStatus(), ResponseEnum.USER_NO_AMOUNT_ERROR);

        // 判断借款额度是否充足
        Assert.isTrue(borrowInfo.getAmount().compareTo(getBorrowAmount(userId)) <= 0, ResponseEnum.USER_AMOUNT_LESS_ERROR);

        // 设置其他数据，插入 borrow_info 表
        borrowInfo.setUserId(userId);
        borrowInfo.setBorrowYearRate(borrowInfo.getBorrowYearRate().divide(new BigDecimal("100"), 2, BigDecimal.ROUND_UP));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());

        baseMapper.insert(borrowInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        BorrowInfo borrowInfo = baseMapper.selectOne(new QueryWrapper<BorrowInfo>().eq("user_id", userId));

        if (borrowInfo == null)
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();

        return borrowInfo.getStatus();
    }

    @Override
    public List<BorrowInfo> selectList() {
        // 多表联合匹配查询多量数据，使用 SQL 语句更加方便
        List<BorrowInfo> borrowInfoList = baseMapper.selectBorrowInfoList();

        borrowInfoList.forEach(borrowInfo -> {
            borrowInfo.getParam().put("returnMethod", dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod()));
            borrowInfo.getParam().put("moneyUse", dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse()));
            borrowInfo.getParam().put("status", BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus()));
        });

        return borrowInfoList;
    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {
        // 查询借款信息 BorrowInfo
        BorrowInfo borrowInfo = baseMapper.selectById(id);

        borrowInfo.getParam().put("returnMethod", dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod()));
        borrowInfo.getParam().put("moneyUse", dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse()));
        borrowInfo.getParam().put("status", BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus()));

        // 查询借款详情 BorrowDetailVO
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(
                borrowerMapper.selectOne(new QueryWrapper<Borrower>().eq("user_id", borrowInfo.getUserId())).getId()
        );

        Map<String, Object> map = new HashMap<>();

        map.put("borrowInfo", borrowInfo);
        map.put("borrower", borrowerDetailVO);

        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        // 修改 borrow_info 借款审核状态
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoApprovalVO.getId());
        Integer status = borrowInfoApprovalVO.getStatus(); // 审核状态

        borrowInfo.setStatus(status);

        baseMapper.updateById(borrowInfo);

        // 审核通过，插入标的记录 lend
        if (status.equals(BorrowInfoStatusEnum.CHECK_OK.getStatus())) {

        }
    }
}
