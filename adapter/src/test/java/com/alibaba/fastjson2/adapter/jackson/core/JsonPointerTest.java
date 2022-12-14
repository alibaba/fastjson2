package com.alibaba.fastjson2.adapter.jackson.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonPointerTest {
    @Test
    public void test() {
        JsonPointer pointer = JsonPointer.compile("$.id");
        assertNotNull(pointer);
    }
}
