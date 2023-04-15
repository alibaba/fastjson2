package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConstStringTest {
    @Test
    public void test() {
        JSONSchema schema = JSONSchema.of(JSONObject.of("const", "abc"));
        assertEquals(JSONSchema.Type.String, schema.getType());
        assertTrue(schema.isValid((Object) null));
        assertTrue(schema.validate((Object) null).isSuccess());
        assertTrue(schema.validate("abc").isSuccess());
        assertFalse(schema.validate("aaa").isSuccess());
    }
}
