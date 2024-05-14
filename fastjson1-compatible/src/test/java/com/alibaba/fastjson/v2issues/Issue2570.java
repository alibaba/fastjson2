package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2570 {
    @Test
    public void test() {
        String json = "{0:12,1:13,2:14,\"date\":\"2024-05-14\"}";
        Map<Object, Object> map = JSON.parseObject(json, Map.class);

        Integer date0 = (Integer) map.get(0);
        assertNotNull(date0);
        assertEquals(12, date0);

        Integer date1 = (Integer) map.get(1);
        assertNotNull(date1);
    }

    @Test
    public void test1() {
        String json = "{-1:12,1:13,2:14,\"date\":\"2024-05-14\"}";
        Map<Object, Object> map = JSON.parseObject(json, Map.class);

        Integer date0 = (Integer) map.get(-1);
        assertNotNull(date0);
        assertEquals(12, date0);

        Integer date1 = (Integer) map.get(1);
        assertNotNull(date1);
    }
}
