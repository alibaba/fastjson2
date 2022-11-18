package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTest {
    @Test
    public void remove() {
        JSONObject object = new JSONObject().fluentPut("id", 123);
        assertTrue(JSONPath.remove(object, "$.id"));
        assertTrue(object.isEmpty());

        assertTrue(JSONPath.set(object, "$.id", 234));
        assertFalse(object.isEmpty());
        assertEquals(234, object.get("id"));
    }

    @Test
    public void compile() {
        JSONObject object = new JSONObject().fluentPut("id", 123);
        JSONPath path = JSONPath.compile("$.id");
        assertEquals(123, path.eval(object));
    }
}
