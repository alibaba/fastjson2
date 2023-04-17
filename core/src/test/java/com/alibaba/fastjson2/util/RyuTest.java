package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RyuTest {
    @Test
    public void test_toString() {
        assertEquals("1.0", RyuDouble.toString(1F));
        assertEquals("3.4028235E38", RyuDouble.toString(Float.MAX_VALUE));
        assertEquals("1.4E-45", RyuDouble.toString(Float.MIN_VALUE));
        assertEquals("1.0", RyuDouble.toString(1D));
        assertEquals("1.7976931348623157E308", RyuDouble.toString(Double.MAX_VALUE));
        assertEquals("4.9E-324", RyuDouble.toString(Double.MIN_VALUE));
    }
}
