package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.query.UserInfoQuery;
import com.atguigu.srb.core.pojo.vo.LoginVO;
import com.atguigu.srb.core.pojo.vo.RegisterVO;
import com.atguigu.srb.core.pojo.vo.UserIndexVO;
import com.atguigu.srb.core.pojo.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 分页显示
     */
    IPage<UserInfo> listPage(Page<UserInfo> userInfoPage, UserInfoQuery userInfoQuery);

    /**
     * 用户锁定
     */
    void lock(Long id, Integer status);

    /**
     * 判断邮箱地址是否被注册
     */
    boolean checkMobile(String mobile);

    /**
     * 获取用户首页信息
     *
     * @param userId 当前登录用户 ID
     * @return 封装用户首页信息的 VO
     */
    UserIndexVO getIndexUserInfo(Long userId);

    /**
     * 根据绑定协议码获取手机号
     *
     * @param bindCode 绑定协议码
     * @return 手机号
     */
    String getMobileByBindCode(String bindCode);
}
