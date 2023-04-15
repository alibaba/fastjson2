package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConstLongTest {
    @Test
    public void test() {
        JSONSchema schema = JSONSchema.of(JSONObject.of("const", 123L));
        assertEquals(JSONSchema.Type.Integer, schema.getType());
        assertTrue(schema.isValid((Object) null));
        assertTrue(schema.validate((Object) null).isSuccess());
        assertTrue(schema.validate((Object) Long.valueOf(123)).isSuccess());
        assertTrue(schema.validate((Object) Float.valueOf(123)).isSuccess());
        assertFalse(schema.validate((Object) Float.valueOf(123.123F)).isSuccess());
        assertFalse(schema.validate("abc").isSuccess());
        assertTrue(schema.validate("123").isSuccess());
        assertFalse(schema.validate("-9223372036854775818").isSuccess());
        assertFalse(schema.validate("1234567890123456789012345678901234567890").isSuccess());
    }
}
