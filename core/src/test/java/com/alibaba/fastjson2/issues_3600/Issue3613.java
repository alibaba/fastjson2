package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3613 {
    String str = "-0.0";
    char[] chars = str.toCharArray();
    byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
    @Test
    public void testFloat() {
        assertEquals(
                -0.0f,
                JSON.parseObject(str, float.class));
        assertEquals(
                -0.0f,
                JSON.parseObject(chars, float.class));
        assertEquals(
                -0.0f,
                JSON.parseObject(utf8, float.class));

        assertEquals(
                -0.0f,
                JSON.parseObject(str, Float.class));
        assertEquals(
                -0.0f,
                JSON.parseObject(chars, Float.class));
        assertEquals(
                -0.0f,
                JSON.parseObject(utf8, Float.class));

        assertEquals(
                -0.0F,
                TypeUtils.parseFloat(chars, 0, chars.length));
        assertEquals(
                -0.0F,
                TypeUtils.parseFloat(utf8, 0, utf8.length));
    }

    @Test
    public void testDouble() {
        assertEquals(
                -0.0D,
                JSON.parseObject(str, double.class));
        assertEquals(
                -0.0D,
                JSON.parseObject(chars, double.class));
        assertEquals(
                -0.0D,
                JSON.parseObject(utf8, double.class));

        assertEquals(
                -0.0D,
                JSON.parseObject(str, Double.class));
        assertEquals(
                -0.0D,
                JSON.parseObject(chars, Double.class));
        assertEquals(
                -0.0D,
                JSON.parseObject(utf8, Double.class));

        assertEquals(
                -0.0D,
                TypeUtils.parseDouble(chars, 0, chars.length));
        assertEquals(
                -0.0D,
                TypeUtils.parseDouble(utf8, 0, utf8.length));
    }
}
