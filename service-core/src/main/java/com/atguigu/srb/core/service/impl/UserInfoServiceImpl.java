package com.atguigu.srb.core.service.impl;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.common.util.MD5;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.mapper.UserLoginRecordMapper;
import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.UserLoginRecord;
import com.atguigu.srb.core.pojo.query.UserInfoQuery;
import com.atguigu.srb.core.pojo.vo.LoginVO;
import com.atguigu.srb.core.pojo.vo.RegisterVO;
import com.atguigu.srb.core.pojo.vo.UserIndexVO;
import com.atguigu.srb.core.pojo.vo.UserInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserLoginRecordMapper userLoginRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterVO registerVO) {
        // 判断邮箱地址是否已经被注册
        Assert.isTrue(
                baseMapper.selectCount(new QueryWrapper<UserInfo>().eq("mobile", registerVO.getMobile())) == 0,
                ResponseEnum.EMAIL_EXIST_ERROR);

        // INSERT 用户信息
        UserInfo userInfo = new UserInfo();

        userInfo.setUserType(registerVO.getUserType());

        userInfo.setNickName(registerVO.getMobile());
        userInfo.setName(registerVO.getMobile());
        userInfo.setMobile(registerVO.getMobile());
        userInfo.setEmail(registerVO.getMobile());

        userInfo.setPassword(MD5.encrypt(registerVO.getPassword()));
        userInfo.setStatus(UserInfo.STATUS_NORMAL);
        userInfo.setHeadImg(UserInfo.USER_AVATAR);

        baseMapper.insert(userInfo);

        // INSERT 用户账户
        UserAccount userAccount = new UserAccount();

        userAccount.setUserId(userInfo.getId());

        userAccountMapper.insert(userAccount);
    }

    @Override
    public UserInfoVO login(LoginVO loginVO, String ip) {
        String email = loginVO.getMobile(), password = loginVO.getPassword();
        Integer userType = loginVO.getUserType();

        // 用户是否存在
        UserInfo userInfo = baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("mobile", email).eq("user_type", userType));

        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);

        // 密码是否正确
        Assert.equals(MD5.encrypt(password), userInfo.getPassword(), ResponseEnum.LOGIN_PASSWORD_ERROR);

        // 用户是否锁定
        Assert.equals(userInfo.getStatus(), UserInfo.STATUS_NORMAL, ResponseEnum.LOGIN_LOCKED_ERROR);

        // 插入登录记录
        UserLoginRecord userLoginRecord = new UserLoginRecord();

        userLoginRecord.setUserId(userInfo.getId());
        userLoginRecord.setIp(ip);

        userLoginRecordMapper.insert(userLoginRecord);

        // 生成 token
        String token = JwtUtils.createToken(userInfo.getId(), userInfo.getName());

        // 封装返回对象
        UserInfoVO userInfoVO = new UserInfoVO();

        BeanUtils.copyProperties(userInfo, userInfoVO);
        userInfoVO.setToken(token);

        return userInfoVO;
    }

    @Override
    public IPage<UserInfo> listPage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery) {
        if (userInfoQuery == null)
            return baseMapper.selectPage(pageParam, null);

        String mobile = userInfoQuery.getMobile();
        Integer status = userInfoQuery.getStatus();
        Integer userType = userInfoQuery.getUserType();

        return baseMapper.selectPage(pageParam,
                new QueryWrapper<UserInfo>()
                        .eq(StringUtils.isNotBlank(mobile), "mobile", mobile)
                        .eq(status != null, "status", status)
                        .eq(userType != null, "user_type", userType)
        );
    }

    @Override
    public void lock(Long id, Integer status) {
        UserInfo userInfo = new UserInfo();

        userInfo.setId(id);
        userInfo.setStatus(status);

        baseMapper.updateById(userInfo);
    }

    @Override
    public boolean checkMobile(String mobile) {
        return baseMapper.selectCount(new QueryWrapper<UserInfo>().eq("mobile", mobile)) > 0;
    }

    @Override
    public UserIndexVO getIndexUserInfo(Long userId) {
        UserIndexVO userIndexVO = new UserIndexVO();

        UserInfo userInfo = baseMapper.selectById(userId);

        BeanUtils.copyProperties(userInfo, userIndexVO);

        UserAccount userAccount = userAccountMapper.selectOne(
                new QueryWrapper<UserAccount>().eq("user_id", userId)
        );

        BeanUtils.copyProperties(userAccount, userIndexVO);

        userIndexVO.setLastLoginTime(
                userLoginRecordMapper
                        .selectOne(
                                new QueryWrapper<UserLoginRecord>()
                                        .eq("user_id", userId)
                                        .orderByDesc("id")
                                        .last("limit 1")
                        )
                        .getCreateTime()
        );

        return userIndexVO;
    }

    @Override
    public String getMobileByBindCode(String bindCode) {
        return baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("bind_code", bindCode)).getMobile();
    }
}
