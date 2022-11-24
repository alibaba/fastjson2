package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatingDecimalTest {
    @Test
    public void test0() {
        String s0 = "A123.456789000B";
        String s1 = s0.substring(1, s0.length() - 1);
        char[] chars = s0.toCharArray();
        float f0 = TypeUtils.parseFloat(chars, 1, chars.length - 2);
        float f1 = Float.parseFloat(s1);
        assertEquals(f1, f0);

        double d0 = TypeUtils.parseDouble(chars, 1, chars.length - 2);
        double d1 = Double.parseDouble(s1);
        assertEquals(d1, d0);
    }
}
