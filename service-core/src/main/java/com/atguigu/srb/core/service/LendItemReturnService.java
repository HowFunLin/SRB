package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.LendItemReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

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

    /**
     * 根据还款记录 ID 组装回款明细 Map 并加入列表
     *
     * @param lendReturnId 还款记录 ID
     * @return 回款明细列表
     */
    List<Map<String, Object>> addReturnDetail(Long lendReturnId);

    /**
     * 根据还款记录 ID 获取回款记录列表
     * @param lendReturnId 还款记录 ID
     * @return 回款记录列表
     */
    List<LendItemReturn> selectLendItemReturnList(Long lendReturnId);
}
