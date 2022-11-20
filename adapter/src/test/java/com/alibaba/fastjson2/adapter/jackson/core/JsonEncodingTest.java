package com.alibaba.fastjson2.adapter.jackson.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonEncodingTest {
    @Test
    public void test() {
        assertTrue(JsonEncoding.UTF16_BE.isBigEndian());
        assertTrue(JsonEncoding.UTF16_BE.bits() != 0);
    }
}
