package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2581 {
    @Test
    public void testEmpty() {
        HashMap<String, String> map = new HashMap<>();
        map.put("date", "2022");
        String jsonString = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);
        HashMap result = JSON.parseObject(jsonString, HashMap.class, JSONReader.Feature.SupportAutoType);
        assertEquals(1, result.size());
    }
}
