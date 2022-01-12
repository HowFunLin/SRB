package com.atguigu.srb.core.mapper;

import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {
    /**
     * 联合 borrower 表查询填充 BorrowInfo 列表
     *
     * @return BorrowInfo 列表
     */
    List<BorrowInfo> selectBorrowInfoList();
}
