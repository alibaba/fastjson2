package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTypedTest {
    @Test
    public void test() {
        JSONPathTyped jsonPath = (JSONPathTyped) JSONPath.of("$.id", Integer.class);
        assertTrue(jsonPath.isRef());
        assertEquals(Integer.class, jsonPath.getType());

        JSONObject object = JSONObject.of("id", 123);
        assertTrue(jsonPath.contains(object));

        assertEquals(
                "123",
                jsonPath.extractScalar(JSONReader.of(object.toString()))
        );

        Object id2 = 234;
        jsonPath.set(object, id2);
        assertEquals(id2, object.get("id"));

        Object id3 = 234;
        jsonPath.set(object, id3, JSONReader.Feature.ErrorOnEnumNotMatch);
        assertEquals(id3, object.get("id"));

        jsonPath.setInt(object, 101);
        assertEquals(101, object.get("id"));

        jsonPath.setLong(object, 102);
        assertEquals(102L, object.get("id"));

        jsonPath.setCallback(object, (obj, value) -> 103);
        assertEquals(103, object.get("id"));

        jsonPath.remove(object);
        assertEquals(0, object.size());
    }

    @Test
    public void testOf() {
        assertSame(JSONPath.RootPath.INSTANCE, JSONPath.of("$", (Type) null));
        assertSame(JSONPath.RootPath.INSTANCE, JSONPath.of("$", Object.class));

        JSONPathTyped jsonPath = (JSONPathTyped) JSONPath.of("$.id", Integer.class);
        assertSame(jsonPath, JSONPathTyped.of(jsonPath, Integer.class));
        JSONPathTyped jsonPath1 = (JSONPathTyped) JSONPathTyped.of(jsonPath, Long.class);
        assertEquals(Long.class, jsonPath1.getType());
    }
}
