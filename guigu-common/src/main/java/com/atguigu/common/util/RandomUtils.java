package com.atguigu.common.util;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * 生成四位和六位的随机数字
 */
public class RandomUtils {
    private static final Random RANDOM = new Random();

    private static final DecimalFormat FOUR_BIT = new DecimalFormat("0000");

    private static final DecimalFormat SIX_BIT = new DecimalFormat("000000");

    public static String getFourBitRandom() {
        return FOUR_BIT.format(RANDOM.nextInt(10000));
    }

    public static String getSixBitRandom() {
        return SIX_BIT.format(RANDOM.nextInt(1000000));
    }
}
