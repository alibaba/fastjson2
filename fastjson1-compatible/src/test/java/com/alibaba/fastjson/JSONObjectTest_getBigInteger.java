package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class JSONObjectTest_getBigInteger {
    @Test
    public void test_get_float() {
        JSONObject obj = new JSONObject();
        obj.put("value", 123.45F);
        assertTrue(123.45F == ((Float) obj.get("value")).floatValue());
        assertEquals(new BigInteger("123"), obj.getBigInteger("value"));
    }

    @Test
    public void test_get_double() {
        JSONObject obj = new JSONObject();
        obj.put("value", 123.45D);
        assertTrue(123.45D == ((Double) obj.get("value")).doubleValue());
        assertEquals(new BigInteger("123"), obj.getBigInteger("value"));
    }

    @Test
    public void test_get_empty() {
        JSONObject obj = new JSONObject();
        obj.put("value", "");
        assertEquals("", obj.get("value"));
        assertNull(obj.getBigInteger("value"));
    }
}
