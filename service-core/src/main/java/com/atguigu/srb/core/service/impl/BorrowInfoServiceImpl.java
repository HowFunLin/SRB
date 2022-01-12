package com.atguigu.srb.core.service.impl;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.BorrowInfoStatusEnum;
import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.UserBindEnum;
import com.atguigu.srb.core.mapper.BorrowInfoMapper;
import com.atguigu.srb.core.mapper.IntegralGradeMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.service.BorrowInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

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
}
