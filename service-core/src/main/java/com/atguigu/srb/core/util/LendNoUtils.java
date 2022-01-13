package com.atguigu.srb.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 标的编号工具类
 */
public class LendNoUtils {
    /**
     * 根据当前时间和三位随机数生成标的编号
     *
     * @return 标的编号
     */
    private static String getNo() {
        String strDate = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());

        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 3; i++)
            result.append(random.nextInt(10));

        return strDate + result;
    }

    public static String getLendNo() {
        return "LEND" + getNo();
    }

    public static String getLendItemNo() {
        return "INVEST" + getNo();
    }

    public static String getLoanNo() {
        return "LOAN" + getNo();
    }

    public static String getReturnNo() {
        return "RETURN" + getNo();
    }

    public static Object getWithdrawNo() {
        return "WITHDRAW" + getNo();
    }

    public static String getReturnItemNo() {
        return "RETURNITEM" + getNo();
    }

    public static String getChargeNo() {
        return "CHARGE" + getNo();
    }

    /**
     * 获取交易编码
     */
    public static String getTransNo() {
        return "TRANS" + getNo();
    }
}