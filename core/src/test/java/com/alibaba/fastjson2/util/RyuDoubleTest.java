package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RyuDoubleTest {
    @Test
    public void test1() {
        assertEquals("NaN", toString(Double.NaN));
        assertEquals("Infinity", toString(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", toString(Double.NEGATIVE_INFINITY));
        assertEquals("-0.0", toString(-0.0));
    }

    @Test
    public void eq() {
        Random r = new Random();

        byte[] bytes = new byte[24];
        for (int i = 0; i < 1_000_000; i++) {
            double d = r.nextDouble();
            String s0 = Double.toString(d);
            int size = DoubleToDecimal.toString(d, bytes, 0, false);
            String s1 = new String(bytes, 0, size, StandardCharsets.US_ASCII);
            boolean eq = s0.equals(s1);
            if (!eq) {
                double d0 = Double.parseDouble(s0);
                double d1 = Double.parseDouble(s1);
                System.out.println(s0 + " -> " + s1);
                if (d0 == d1) {
                    eq = true;
                }
            }
            if (!eq) {
                fail(s0 + " -> " + s1);
            }
        }
    }

    static String toString(double value) {
        byte[] bytes = new byte[24];
        int len = DoubleToDecimal.toString(value, bytes, 0, false);
        return new String(bytes, 0, len);
    }
}
