package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3449 {
    @Test
    public void testDoubleNegativeNaN() {
        assertEquals("null", JSON.toJSONString(Double.NaN));
        assertEquals("null", new String(JSON.toJSONBytes(Double.NaN)));

        double nan = Double.NaN;
        long bits = Double.doubleToLongBits(nan);
        long newBits = bits | 0x8000000000000000L;   // 转为负数
        double newNan = Double.longBitsToDouble(newBits);
        assertEquals("null", JSON.toJSONString(newNan));
        assertEquals("null", new String(JSON.toJSONBytes(newNan)));
    }

    @Test
    public void testFloatNegativeNan() {
        assertEquals("null", JSON.toJSONString(Float.NaN));
        assertEquals("null", new String(JSON.toJSONBytes(Float.NaN)));

        float nan = Float.NaN;
        int bits = Float.floatToIntBits(nan);
        int newBits = bits | 0x80000000;   // 转为负数
        float newNan = Float.intBitsToFloat(newBits);
        assertEquals("null", JSON.toJSONString(newNan));
        assertEquals("null", new String(JSON.toJSONBytes(newNan)));
    }
}
