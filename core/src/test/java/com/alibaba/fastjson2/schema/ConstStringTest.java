package com.alibaba.fastjson2.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstStringTest {
    @Test
    public void test() {
        ConstString schema = new ConstString("abc");
        assertEquals(JSONSchema.Type.Const, schema.getType());
        assertTrue(schema.isValid((Object) null));
        assertTrue(schema.validate((Object) null).isSuccess());
    }
}
