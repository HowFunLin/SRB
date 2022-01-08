package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.vo.BorrowerVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface BorrowerService extends IService<Borrower> {
    /**
     * 根据当前登录的用户 ID 保存借款人提交的信息
     *
     * @param borrowerVO 借款人提交信息
     * @param userId     当前登录的用户 ID
     */
    void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId);

    /**
     * 根据当前登录的用户 ID 获取借款人状态
     *
     * @param userId 当前登录的用户
     * @return 借款人状态
     */
    Integer getStatusByUserId(Long userId);
}
