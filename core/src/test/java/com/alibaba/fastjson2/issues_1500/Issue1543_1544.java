package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1543_1544 {
    @Test
    public void testMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("t", map);

        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.setMaxLevel(2049);
        assertEquals(2049, context.getMaxLevel());

        try {
            JSON.toJSONString(map, context);
        } catch (Exception e) {
            assertTrue(e instanceof JSONException);
            assertEquals("level too large : " + (context.getMaxLevel() + 1), e.getMessage());
        }
    }

    @Test
    public void testList() {
        ArrayList<Object> list = new ArrayList<>();
        list.add(list);

        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.setMaxLevel(2049);
        assertEquals(2049, context.getMaxLevel());
        try {
            JSON.toJSONString(list, context);
        } catch (Exception e) {
            assertTrue(e instanceof JSONException);
            assertEquals("level too large : " + (context.getMaxLevel() + 1), e.getMessage());
        }
    }
}
