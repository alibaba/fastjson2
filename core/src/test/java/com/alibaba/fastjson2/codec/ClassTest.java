package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassTest {
    @Test
    public void test_0() {
        assertEquals("\"int\"", JSON.toJSONString(int.class));
        assertEquals("\"java.lang.Integer\"", JSON.toJSONString(Integer.class));
    }
}
