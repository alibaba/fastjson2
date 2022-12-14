package com.alibaba.fastjson2.adapter.jackson.core.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonReadFeatureTest {
    @Test
    public void test() {
        JsonReadFeature feature = JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS;
        assertNotNull(feature.mappedFeature());
    }
}
