package com.alibaba.fastjson.serializer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SerializerFeatureTest {
    @Test
    public void of() {
        assertEquals(0, SerializerFeature.of(new SerializerFeature[0]));
        assertEquals(0, SerializerFeature.of(null));
        assertEquals(SerializerFeature.WriteClassName.mask,
                SerializerFeature.of(new SerializerFeature[] {SerializerFeature.WriteClassName})
        );
    }

    @Test
    public void config() {
        int features = 0;
        features = SerializerFeature.config(features, SerializerFeature.WriteClassName, true);
        assertTrue(SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName));
        assertTrue(SerializerFeature.isEnabled(features, 0, SerializerFeature.WriteClassName));

        features = SerializerFeature.config(features, SerializerFeature.WriteClassName, false);
        assertFalse(SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName));
    }
}
