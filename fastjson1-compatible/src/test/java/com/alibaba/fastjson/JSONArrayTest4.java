package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONArrayTest4 {
    @Test
    public void test0() {
        JSONObject object = JSON.parseObject("{\"value\":[]}");
        JSONArray array = (JSONArray) object.get("value");
        assertEquals(0, array.size());
        assertEquals(array, array.clone());
    }
}
