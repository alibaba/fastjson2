package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeepTest {
    @Test
    public void testDeepArray() {
        StringBuffer buf = new StringBuffer();
        final int level = 1000;
        for (int i = 0; i < level; i++) {
            buf.append('[');
        }
        for (int i = 0; i < level; i++) {
            buf.append(']');
        }

        String str = buf.toString();
        Object obj = JSON.parse(str);
        String str1 = JSON.toJSONString(obj);
        assertEquals(str, str1);
    }

    @Test
    public void testDeepObject() {
        StringBuffer buf = new StringBuffer();
        final int level = 1000;
        for (int i = 0; i < level; i++) {
            buf.append("{\"val\":");
        }

        buf.append("{}");

        for (int i = 0; i < level; i++) {
            buf.append("}");
        }
        String str = buf.toString();
        Object obj = JSON.parse(str);
        String str1 = JSON.toJSONString(obj);
        assertEquals(str, str1);
    }
}
