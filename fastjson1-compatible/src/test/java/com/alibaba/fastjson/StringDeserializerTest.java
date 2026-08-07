package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringDeserializerTest {
    @Test
    public void test_0() throws Exception {
        assertEquals("123", JSON.parseObject("123", String.class));
        assertEquals("true", JSON.parseObject("true", String.class));
        assertEquals(null, JSON.parseObject("null", String.class));
    }

    @Test
    public void test_StringBuffer() throws Exception {
        assertTrue(equals(new StringBuffer("123"), JSON.parseObject("123", StringBuffer.class)));
        assertTrue(equals(new StringBuffer("true"), JSON.parseObject("true", StringBuffer.class)));
        assertEquals(null, JSON.parseObject("null", StringBuffer.class));
    }

    @Test
    public void test_StringBuilder() throws Exception {
        assertTrue(equals(new StringBuilder("123"), JSON.parseObject("123", StringBuilder.class)));
        assertTrue(equals(new StringBuilder("true"), JSON.parseObject("true", StringBuilder.class)));
        assertEquals(null, JSON.parseObject("null", StringBuilder.class));
    }

    private boolean equals(StringBuffer sb1, StringBuffer sb2) {
        if (sb1 == null && sb2 == null) {
            return true;
        }
        if ((sb1 == null && sb2 != null) || (sb1 != null && sb2 == null)) {
            return false;
        }

        return sb1.toString().equals(sb2.toString());
    }

    private boolean equals(StringBuilder sb1, StringBuilder sb2) {
        if (sb1 == null && sb2 == null) {
            return true;
        }
        if ((sb1 == null && sb2 != null) || (sb1 != null && sb2 == null)) {
            return false;
        }

        return sb1.toString().equals(sb2.toString());
    }
}
