package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONSchemaTest5 {
    @Test
    public void testObject() {
        JSONSchema jsonSchema = JSONSchema.of(
                JSONObject.of("type", "object",
                        "properties", JSONObject.of(
                                "value",
                                JSONObject.of("type", "object", "encoded", true)
                        )
                )
        );
        assertTrue(
                jsonSchema.isValid(
                        JSON.parseObject("{\"value\":\"{}\"}")));
        assertFalse(
                jsonSchema.isValid(
                        JSON.parseObject("{\"value\":{}}")));
    }

    @Test
    public void testArray() {
        JSONSchema jsonSchema = JSONSchema.of(
                JSONObject.of("type", "object",
                        "properties", JSONObject.of(
                                "value",
                                JSONObject.of("type", "array", "encoded", true)
                        )
                )
        );
        assertTrue(
                jsonSchema.isValid(
                        JSON.parseObject("{\"value\":\"[]\"}")));
        assertFalse(
                jsonSchema.isValid(
                        JSON.parseObject("{\"value\":[]}")));
    }
}
