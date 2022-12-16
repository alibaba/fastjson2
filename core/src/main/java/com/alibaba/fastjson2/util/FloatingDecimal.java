package com.alibaba.fastjson2.util;

final class FloatingDecimal {
    static final int DOUBLE_SIGNIFICAND_WIDTH = 53;

    static final int DOUBLE_EXP_BIAS = 1023;
    static final long DOUBLE_SIGN_BIT_MASK = 0x8000000000000000L;
    static final long DOUBLE_EXP_BIT_MASK = 0x7FF0000000000000L;
    static final long DOUBLE_SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL;
    static final int FLOAT_SIGNIFICAND_WIDTH = 24;
    static final int FLOAT_EXP_BIAS = 127;
    static final int FLOAT_SIGNIF_BIT_MASK = 0x007FFFFF;
    static final int FLOAT_EXP_BIT_MASK = 0x7F800000;
    static final int FLOAT_SIGN_BIT_MASK = 0x80000000;

    static final int EXP_SHIFT = DOUBLE_SIGNIFICAND_WIDTH - 1;
    static final long FRACT_HOB = (1L << EXP_SHIFT); // assumed High-Order bit
    static final int MAX_DECIMAL_DIGITS = 15;
    static final int MAX_DECIMAL_EXPONENT = 308;
    static final int MIN_DECIMAL_EXPONENT = -324;
    static final int MAX_NDIGITS = 1100;

    static final int SINGLE_EXP_SHIFT = FLOAT_SIGNIFICAND_WIDTH - 1;
    static final int SINGLE_FRACT_HOB = 1 << SINGLE_EXP_SHIFT;
    static final int SINGLE_MAX_DECIMAL_DIGITS = 7;
    static final int SINGLE_MAX_DECIMAL_EXPONENT = 38;
    static final int SINGLE_MIN_DECIMAL_EXPONENT = -45;
    static final int SINGLE_MAX_NDIGITS = 200;

    static final int INT_DECIMAL_DIGITS = 9;

    static final int BIG_DECIMAL_EXPONENT = 324; // i.e. abs(MIN_DECIMAL_EXPONENT)

    /**
     * All the positive powers of 10 that can be
     * represented exactly in double/float.
     */
    static final double[] SMALL_10_POW = {
            1.0e0,
            1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5,
            1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10,
            1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15,
            1.0e16, 1.0e17, 1.0e18, 1.0e19, 1.0e20,
            1.0e21, 1.0e22
    };

    static final float[] SINGLE_SMALL_10_POW = {
            1.0e0f,
            1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
            1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };

    static final double[] BIG_10_POW = {
            1e16, 1e32, 1e64, 1e128, 1e256};
    static final double[] TINY_10_POW = {
            1e-16, 1e-32, 1e-64, 1e-128, 1e-256};

    static final int MAX_SMALL_TEN = SMALL_10_POW.length - 1;
    static final int SINGLE_MAX_SMALL_TEN = SINGLE_SMALL_10_POW.length - 1;
}
