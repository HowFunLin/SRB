package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.vo.BorrowerApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.pojo.vo.BorrowerVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 获取借款人分页列表
     *
     * @param pageParam 分页参数
     * @param keyword   查询关键字
     * @return 分页参数对应的借款人分页列表
     */
    IPage<Borrower> listPage(Page<Borrower> pageParam, String keyword);

    /**
     * 根据借款人 ID 获取借款人详细信息并封装 VO
     *
     * @param id 借款人 ID
     * @return 借款人详细信息 VO
     */
    BorrowerDetailVO getBorrowerDetailVOById(Long id);

    /**
     * 根据审批结果 VO 更新审批后的借款人信息和积分
     *
     * @param borrowerApprovalVO 审批结果 VO
     */
    void approval(BorrowerApprovalVO borrowerApprovalVO);
}
