package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface LendService extends IService<Lend> {
    /**
     * 根据借款审批信息和借款信息创建标的
     *
     * @param borrowInfoApprovalVO 借款审批信息
     * @param borrowInfo           借款信息
     */
    void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo);

    /**
     * 查询标的列表
     *
     * @return 标的列表
     */
    List<Lend> selectList();

    /**
     * 根据标的 ID 获取标的详情
     *
     * @param id 标的 ID
     * @return 封装标的信息和借款详情 VO 的 Map
     */
    Map<String, Object> getLendDetail(Long id);

    /**
     * 获取投资收益
     *
     * @param invest       投资
     * @param yearRate     年利率
     * @param totalMonth   总月数
     * @param returnMethod 还款方式
     * @return 投资收益
     */
    BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalMonth, Integer returnMethod);

    /**
     * 放款
     *
     * @param id 标的 ID
     */
    void makeLoan(Long id);
}
