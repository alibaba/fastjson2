package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Issue1543_1544 {
    @Test
    public void testMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("t", map);
        try {
            JSON.toJSONString(map);
        } catch (Exception e) {
            assertTrue(e instanceof JSONException);
            assertEquals("level too large : 2048", e.getMessage());
        }
    }

    @Test
    public void testList() {
        ArrayList<Object> list = new ArrayList<>();
        list.add(list);
        try {
            JSON.toJSONString(list);
        } catch (Exception e) {
            assertTrue(e instanceof JSONException);
            assertEquals("level too large : 2048", e.getMessage());
        }
    }
}
