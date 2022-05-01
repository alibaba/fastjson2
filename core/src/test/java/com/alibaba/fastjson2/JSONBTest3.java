package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBTest3 {
    @Test
    public void test_map() {
        JSONObject object = new JSONObject();
        for (int i = 0; i < 100; ++i) {
            object.put("i" + i, i);
        }
        byte[] bytes = JSONB.toBytes(object);
        JSONObject parsed = (JSONObject) JSONB.parseObject(bytes, Object.class);
        assertEquals(object, parsed);
    }

    @Test
    public void test_array() {
        JSONArray object = new JSONArray();
        for (int i = 0; i < 100; ++i) {
            object.add(i);
        }
        byte[] bytes = JSONB.toBytes(object);
        JSONArray parsed = (JSONArray) JSONB.parseObject(bytes, Object.class);
        assertEquals(object, parsed);
    }
}
