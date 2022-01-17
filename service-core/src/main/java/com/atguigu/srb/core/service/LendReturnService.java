package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.LendReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 还款记录表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface LendReturnService extends IService<LendReturn> {
    /**
     * 根据标的 ID 获取还款计划列表
     *
     * @param lendId 标的 ID
     * @return 还款计划列表
     */
    List<LendReturn> selectByLendId(Long lendId);
}
