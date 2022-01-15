package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserBind;
import com.atguigu.srb.core.pojo.vo.UserBindVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface UserBindService extends IService<UserBind> {
    /**
     * 绑定帐户提交信息
     */
    String commitBindUser(UserBindVO userBindVO, Long userId);

    /**
     * 账户绑定异步回调
     */
    void notify(Map<String, Object> paramMap);

    /**
     * 通过用户 ID 获取 绑定协议号
     */
    String getBindCodeByUserId(Long userId);
}
