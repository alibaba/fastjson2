package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSONObjectTest7 {

    @Test
    public void test() throws Exception {
        JSONObject jsonObject = JSON.parseObject("{\"test\":null,\"a\":\"cc\"}");
        assertEquals(2, jsonObject.entrySet().size());
        assertTrue(jsonObject.containsKey("test"));
        assertTrue(jsonObject.containsValue("cc"));
        assertFalse(jsonObject.isEmpty());
    }

}
