package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.mapper.BorrowerAttachMapper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.vo.BorrowerVO;
import com.atguigu.srb.core.service.BorrowerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId) {
        // 从 UserInfo 表获取当前用户信息和提交的借款人信息存入 Borrower 表
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Borrower borrower = new Borrower();

        BeanUtils.copyProperties(borrowerVO, borrower);
        BeanUtils.copyProperties(userInfo, borrower);

        borrower.setUserId(userId);
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());

        baseMapper.insert(borrower);

        // 更新 UserInfo 表中相关字段
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());

        userInfoMapper.updateById(userInfo);

        // 保存附件相关信息
        borrowerVO.getBorrowerAttachList().forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        Borrower borrower = baseMapper.selectOne(new QueryWrapper<Borrower>().eq("user_id", userId));

        if (borrower == null)
            return BorrowerStatusEnum.NO_AUTH.getStatus();

        return borrower.getStatus();
    }
}
