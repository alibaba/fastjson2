package com.alibaba.fastjson2.benchmark.fastcode;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecimalUtilsRegressionTest {
    @Test
    public void decimalLayouts() {
        long[] values = {0, 1, -1, 12, -12, 1001, -1001, 1000000001L,
                Long.MIN_VALUE, Long.MAX_VALUE};
        for (long value : values) {
            for (int scale = -20; scale <= 20; scale++) {
                BigDecimal decimal = BigDecimal.valueOf(value, scale);
                String label = value + ":" + scale;
                assertEquals(decimal.toPlainString(), DecimalUtils.toString(value, scale), label);
                assertEquals(decimal.toString(), DecimalUtils.layout(value, scale, true), label);
                assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(value, scale, false), label);
                BigInteger integer = BigInteger.valueOf(value);
                assertEquals(decimal.toPlainString(), DecimalUtils.toString(integer, scale), label);
                assertEquals(decimal.toString(), DecimalUtils.layout(integer, scale, true), label);
                assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(integer, scale, false), label);
            }
        }
    }
}
