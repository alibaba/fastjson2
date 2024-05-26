package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2623 {
    @Test
    public void test() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1L);
        map.put("b", 2);
        map.put("c", 3F);
        map.put("d", new Date(System.currentTimeMillis()));
        map.put("e", new java.util.Date());

        String s = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);
        Map<String, Object> map2 = JSON.parseObject(s, new TypeReference<Map<String, Object>>() {
        }, JSONReader.Feature.SupportAutoType);
        assertEquals(map.get("d").toString(), map2.get("d").toString());
        assertEquals(map.get("e").toString(), map2.get("e").toString());
    }
}
