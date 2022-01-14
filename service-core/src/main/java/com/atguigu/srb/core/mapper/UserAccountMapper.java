package com.atguigu.srb.core.mapper;

import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 用户账户 Mapper 接口
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface UserAccountMapper extends BaseMapper<UserAccount> {
    /**
     * 根据参数更新 user_account 表
     *
     * @param bindCode     绑定协议号 bind code
     * @param amount       充值金额 amount
     * @param freezeAmount 冻结金额 freeze amount
     */
    void updateAccount(
            // @Param 用于 XML 中 SQL 语句引用参数
            @Param("bindCode") String bindCode,
            @Param("amount") BigDecimal amount,
            @Param("freezeAmount") BigDecimal freezeAmount
    );
}
