package com.alibaba.fastjson2.support;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObject1xTest {
    @Test
    public void test_0() {
        JSONObject object = new JSONObject();
        assertEquals("{}",
                JSON.toJSONString(object));

        assertEquals("{}",
                JSON.toJSONString(
                        JSONB.parse(
                                JSONB.toBytes(object))));
    }

    @Test
    public void test_1() {
        JSONArray array = new JSONArray();
        assertEquals("[]",
                JSON.toJSONString(array));

        assertEquals("[]",
                JSON.toJSONString(
                        JSONB.parse(
                                JSONB.toBytes(array))));
    }
}
