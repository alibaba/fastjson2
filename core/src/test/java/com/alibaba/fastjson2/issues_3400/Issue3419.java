package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3419 {
    @Test
    public void test() {
        Map<String, Object> map = new HashMap<>();
        map.put("str", new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f});

        com.alibaba.fastjson.JSONObject jo1 = new com.alibaba.fastjson.JSONObject();
        com.alibaba.fastjson.JSONArray ja1 = new com.alibaba.fastjson.JSONArray();
        ja1.addAll(map.entrySet());
        jo1.put("content", ja1);

        JSONObject jo2 = new JSONObject();
        JSONArray ja2 = new JSONArray();
        ja2.addAll(map.entrySet());
        jo2.put("content", ja2);

        assertEquals(jo1.getJSONArray("content").getJSONObject(0), jo2.getJSONArray("content").getJSONObject(0));
    }
}
