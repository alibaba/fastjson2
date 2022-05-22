package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONObjectTest_getDate {
    @Test
    public void test_get_empty() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", "");
        assertEquals("", obj.get("value"));
        assertNull(obj.getDate("value"));
    }
}
