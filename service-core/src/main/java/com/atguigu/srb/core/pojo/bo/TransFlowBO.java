package com.atguigu.srb.core.pojo.bo;

import com.atguigu.srb.core.enums.TransTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 交易流水业务对象
 * （业务对象常用于不同层之间传输数据）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransFlowBO {
    private String agentBillNo;
    private String bindCode;
    private BigDecimal amount;
    private TransTypeEnum transTypeEnum;
    private String memo;
}