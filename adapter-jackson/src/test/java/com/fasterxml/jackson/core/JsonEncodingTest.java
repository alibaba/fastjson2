package com.fasterxml.jackson.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonEncodingTest {
    @Test
    public void test() {
        assertTrue(JsonEncoding.UTF16_BE.isBigEndian());
        assertTrue(JsonEncoding.UTF16_BE.bits() != 0);
    }
}
