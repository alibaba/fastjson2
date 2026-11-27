package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("util")
public class MutableBigIntegerTest {
    @Test
    public void divideKnuthLongFast() {
        Random random = new Random(1);
        for (int i = 0; i < 1_000_000; i++) {
            long intCompact = (random.nextLong() >>> 1) >>> random.nextInt(63);
            if (intCompact == 0) {
                continue;
            }
            int scale = 1 + random.nextInt(20);
            // same ql as TypeUtils.doubleValue
            int bitLength = 64 - Long.numberOfLeadingZeros(intCompact);
            int ql = bitLength - (int) Math.ceil(scale * 3.321928094887362) - 56;
            assertEquals(
                    MutableBigInteger.divideKnuthLong(intCompact, ql, scale),
                    MutableBigInteger.divideKnuthLongFast(intCompact, ql, scale),
                    intCompact + " " + ql + " " + scale);
        }
    }
}
