package com.alibaba.fastjson.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureTest {
    @Test
    public void of() {
        assertEquals(0, Feature.of(new Feature[0]));
        assertEquals(0, Feature.of(null));
        assertEquals(Feature.SupportAutoType.getMask(),
                Feature.of(new Feature[]{Feature.SupportAutoType})
        );
    }

    @Test
    public void config() {
        int features = 0;
        features = Feature.config(features, Feature.SupportAutoType, true);
        assertTrue(Feature.isEnabled(features, Feature.SupportAutoType));

        features = Feature.config(features, Feature.SupportAutoType, false);
        assertFalse(Feature.isEnabled(features, Feature.SupportAutoType));
    }
}
