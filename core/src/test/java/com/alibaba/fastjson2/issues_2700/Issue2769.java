package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2769 {
    @Test
    public void testWithSuffixB() {
        String str = "{\"k1\":123.456,\"k2\":12.34B}";
        test(JSON.parseObject(str));
        test(JSON.parseObject(str.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testWithSuffixS() {
        String str = "{\"k1\":123.456,\"k2\":12.34S}";
        test(JSON.parseObject(str));
        test(JSON.parseObject(str.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testWithSuffixL() {
        String str = "{\"k1\":123.456,\"k2\":12.34L}";
        test(JSON.parseObject(str));
        test(JSON.parseObject(str.getBytes(StandardCharsets.UTF_8)));
    }

    private void test(JSONObject jsonObject) {
        assertEquals((byte) 12, jsonObject.getByte("k2"));
        assertEquals((short) 12, jsonObject.getShort("k2"));
        assertEquals(12, jsonObject.getInteger("k2"));
        assertEquals(12, jsonObject.getLong("k2"));
        assertEquals(12.34F, jsonObject.getFloat("k2"));
        assertEquals(12.34, jsonObject.getDouble("k2"));
    }
}
