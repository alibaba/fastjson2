package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

public class BigDecimalToPlainStringTest {
    @Test
    public void test() {
    }

    public static String toString(long unscaledVal, int scale) {
        if (scale == 0) {
            return Long.toString(unscaledVal);
        }

        boolean negative = false;
        if (unscaledVal < 0) {
            unscaledVal = -unscaledVal;
            negative = true;
        }

        int size = IOUtils.stringSize(unscaledVal);
        int insertionPoint = size - scale;

        byte[] buf;
        if (insertionPoint == 0) {
            buf = new byte[size + 2];
        } else if (insertionPoint < 0) {
            buf = new byte[size + 2 - insertionPoint];
        } else {
            long power = POWER_TEN[scale - 1];
            long div = unscaledVal / power;
            long rem = unscaledVal - div * power;
            buf = new byte[IOUtils.stringSize(div) + 1 + scale];
        }

        return null;
    }

    static final long[] POWER_TEN = {
            10,
            100,
            1000,
            10000,
            100000,
            1000000,
            10000000,
            100000000,
            1000000000,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,
            10000000000000000L,
            100000000000000000L,
            1000000000000000000L,
    };
}
