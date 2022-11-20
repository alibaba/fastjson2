package com.alibaba.fastjson2.adapter.jackson.databind;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapperFeatureTest {
    @Test
    public void test() {
        assertEquals(
                MapperFeature.USE_ANNOTATIONS.getMask(),
                MapperFeature.USE_ANNOTATIONS.getLongMask()
        );

        assertTrue(MapperFeature.USE_ANNOTATIONS.enabledIn(MapperFeature.USE_ANNOTATIONS.getMask()));
        assertTrue(MapperFeature.USE_ANNOTATIONS.enabledIn(MapperFeature.USE_ANNOTATIONS.getLongMask()));
    }
}
