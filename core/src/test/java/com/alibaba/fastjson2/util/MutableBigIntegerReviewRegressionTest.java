package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableBigIntegerReviewRegressionTest {
    @Test
    public void oneWordQuotientDoesNotReadNegativeIndex() {
        long[] values = {10000000001L, 20000000000L, Long.MAX_VALUE};
        for (long value : values) {
            for (int scale = 10; scale < 19; scale++) {
                long expected = BigInteger.valueOf(value).divide(BigInteger.TEN.pow(scale)).longValue();
                assertEquals(expected, MutableBigInteger.divideKnuthLong(value, 0, scale));
            }
        }
    }
}
