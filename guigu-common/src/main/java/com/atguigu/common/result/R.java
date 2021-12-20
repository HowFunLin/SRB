package com.atguigu.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@ApiModel(value = "R对象",description = "统一封装响应信息")
@Data
public class R {
    @ApiModelProperty("响应码")
    private Integer code;

    @ApiModelProperty("响应信息")
    private String message;

    @ApiModelProperty("响应数据")
    private Map<String, Object> data = new HashMap<>();

    /**
     * 构造函数私有化
     */
    private R() {
    }

    // ---------- 静态方法供外部调用生成对象 ----------

    /**
     * 返回成功结果
     */
    public static R ok() {
        R r = new R();
        r.setCode(ResponseEnum.SUCCESS.getCode());
        r.setMessage(ResponseEnum.SUCCESS.getMessage());
        return r;
    }

    /**
     * 返回失败结果
     */
    public static R error() {
        R r = new R();
        r.setCode(ResponseEnum.ERROR.getCode());
        r.setMessage(ResponseEnum.ERROR.getMessage());
        return r;
    }

    /**
     * 设置特定的结果
     */
    public static R setResult(ResponseEnum responseEnum) {
        R r = new R();
        r.setCode(responseEnum.getCode());
        r.setMessage(responseEnum.getMessage());
        return r;
    }

    // ---------- 设置响应码、响应信息、数据等 ----------

    public R data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public R data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }

    /**
     * 设置特定的响应消息
     */
    public R message(String message) {
        this.setMessage(message);
        return this;
    }

    /**
     * 设置特定的响应码
     */
    public R code(Integer code) {
        this.setCode(code);
        return this;
    }
}
