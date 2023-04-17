package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class RyuFloatTest {
    @Test
    public void test() {
        assertEquals("NaN", RyuDouble.toString(Float.NaN));
        assertEquals("Infinity", RyuDouble.toString(Float.POSITIVE_INFINITY));
        assertEquals("-Infinity", RyuDouble.toString(Float.NEGATIVE_INFINITY));
        assertEquals("-0.0", RyuDouble.toString(-0.0F));
    }

    @Test
    public void test1() {
        assertEquals("NaN", toString(Float.NaN));
        assertEquals("Infinity", toString(Float.POSITIVE_INFINITY));
        assertEquals("-Infinity", toString(Float.NEGATIVE_INFINITY));
        assertEquals("-0.0", toString(-0.0F));
    }

    @Test
    public void eq() {
        Random r = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            float f = r.nextFloat();
            String s0 = Float.toString(f);
            String s1 = RyuDouble.toString(f);
            boolean eq = s0.equals(s1);
            if (!eq) {
                float f0 = Float.parseFloat(s0);
                float f1 = Float.parseFloat(s1);
                System.out.println(s0 + " -> " + s1);
                if (f0 == f1) {
                    eq = true;
                }
            }
            if (!eq) {
                fail(s0 + " -> " + s1);
            }
        }
    }

    static String toString(float value) {
        byte[] bytes = new byte[24];
        int len = RyuDouble.toString(value, bytes, 0);
        return new String(bytes, 0, len);
    }
}
