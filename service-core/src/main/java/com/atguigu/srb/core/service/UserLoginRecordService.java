package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserLoginRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户登录记录表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface UserLoginRecordService extends IService<UserLoginRecord> {
    /**
     * 获取会员最近 50 次登录记录
     */
    List<UserLoginRecord> listTop50(Long userId);
}
