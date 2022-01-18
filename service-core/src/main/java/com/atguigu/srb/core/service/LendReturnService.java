package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.LendReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

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

    /**
     * 提交还款
     *
     * @param lendReturnId 还款计划 ID
     * @param userId       当前登录用户 ID
     * @return 自动提交表单字符串
     */
    String commitReturn(Long lendReturnId, Long userId);

    /**
     * 汇付宝 还款回调
     *
     * @param paramMap 回调参数
     */
    void notify(Map<String, Object> paramMap);
}
