package com.alibaba.fastjson2.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConstLongTest {
    @Test
    public void test() {
        ConstLong schema = new ConstLong(123L);
        assertEquals(JSONSchema.Type.Const, schema.getType());
        assertTrue(schema.isValid((Object) null));
        assertTrue(schema.validate((Object) null).isSuccess());
        assertTrue(schema.validate((Object) Long.valueOf(123)).isSuccess());
        assertTrue(schema.validate((Object) Float.valueOf(123)).isSuccess());
        assertFalse(schema.validate((Object) Float.valueOf(123.123F)).isSuccess());
        assertFalse(schema.validate((Object) "abc").isSuccess());
    }
}
