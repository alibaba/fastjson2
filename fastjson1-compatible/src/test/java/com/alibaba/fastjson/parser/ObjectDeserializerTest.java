package com.alibaba.fastjson.parser;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectDeserializerTest {
    @Test
    public void test() {
        ObjectDeserializer deserializer = new ObjectDeserializer() {
            @Override
            public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
                return null;
            }
        };

        assertEquals(0, deserializer.getFastMatchToken());
        assertNull(deserializer.deserialze(null, null, null));
    }
}
