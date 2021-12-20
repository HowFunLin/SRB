package com.atguigu.common.exception;

import com.atguigu.common.result.ResponseEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 自定义异常用于封装各类业务异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BusinessException extends RuntimeException /* 自定义异常必须为 Runtime Exception */ {
    //状态码
    private Integer code;

    //错误消息
    private String message;

    // ---------- 提供多种封装异常方式 ----------

    /**
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this.message = message;
    }

    /**
     * @param message 错误消息
     * @param code    错误码
     */
    public BusinessException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    /**
     * @param message 错误消息
     * @param code    错误码
     * @param cause   原始异常对象
     */
    public BusinessException(String message, Integer code, Throwable cause) {
        super(cause);
        this.message = message;
        this.code = code;
    }

    /**
     * @param resultCodeEnum 接收枚举类型
     */
    public BusinessException(ResponseEnum resultCodeEnum) {
        this.message = resultCodeEnum.getMessage();
        this.code = resultCodeEnum.getCode();
    }

    /**
     * @param resultCodeEnum 接收枚举类型
     * @param cause          原始异常对象
     */
    public BusinessException(ResponseEnum resultCodeEnum, Throwable cause) {
        super(cause);
        this.message = resultCodeEnum.getMessage();
        this.code = resultCodeEnum.getCode();
    }

}