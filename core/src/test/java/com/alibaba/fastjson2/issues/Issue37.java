package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue37 {
    @Test
    public void test_for_issue() {
        String str = "{\"test\":\"123465\"}";
        JSONObject json = JSON.parseObject(str);
        assertEquals(1, json.toJavaObject(Map.class).size());
        assertEquals(1, JSON.toJavaObject(json, Map.class).size());
        assertEquals(1, JSON.toJavaObject(str, Map.class).size());

        {
            HashMap map = json.toJavaObject(HashMap.class);
            assertNotNull(map);
            assertEquals(1, map.size());
        }
        {
            LinkedHashMap map = json.toJavaObject(LinkedHashMap.class);
            assertNotNull(map);
            assertEquals(1, map.size());
        }
    }
}
