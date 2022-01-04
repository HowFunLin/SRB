package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.vo.LoginVO;
import com.atguigu.srb.core.pojo.vo.RegisterVO;
import com.atguigu.srb.core.pojo.vo.UserInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface UserInfoService extends IService<UserInfo> {
    /**
     * 注册用户
     */
    void register(RegisterVO registerVO);

    /**
     * 用户登录
     */
    UserInfoVO login(LoginVO loginVO, String ip);
}
