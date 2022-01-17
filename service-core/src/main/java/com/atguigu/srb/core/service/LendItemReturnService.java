package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.LendItemReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 标的出借回款记录表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface LendItemReturnService extends IService<LendItemReturn> {
    /**
     * 根据标的 ID 和当前登录用户 ID 获取回款记录
     *
     * @param lendId 标的 ID
     * @param userId 当前登录用户 ID
     * @return 回款记录列表
     */
    List<LendItemReturn> selectByLendId(Long lendId, Long userId);
}
