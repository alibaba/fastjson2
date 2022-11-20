package com.alibaba.fastjson2.adapter.jackson.databind;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SerializationFeatureTest {
    @Test
    public void test() {
        assertTrue(SerializationFeature.values().length > 0);
    }
}
