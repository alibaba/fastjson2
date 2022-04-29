package com.alibaba.fastjson2.issues;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue106 {
    @Test
    public void test_array() {
        String str = "[{\"name\":\"mask\"}]";
        com.alibaba.fastjson2.JSONArray array = com.alibaba.fastjson2.JSON.parseArray(str);
        Map object = array.getObject(0, Map.class);
        assertNotNull(object);
        assertEquals(1, object.size());
        assertEquals("mask", object.get("name"));
    }
}
