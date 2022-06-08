package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue273 {
    @Test
    public void test() {
        Map<Integer, Long> map0 = new HashMap<>();
        map0.put(1, 111L);
        map0.put(2, 222L);
        String json = JSON.toJSONString(map0);
        assertEquals("{1:111,2:222}", json);

        Type type1 = new TypeReference<HashMap<Integer, Long>>() {
        }.getType();
        Map<Integer, Long> map1 = JSON.parseObject(json, type1);
        assertEquals(111L, map1.get(1));
        assertEquals(222L, map1.get(2));

        Type type2 = new TypeReference<Map<Integer, String>>() {
        }.getType();
        Map<Integer, String> map2 = JSON.parseObject(json, type2);
        assertEquals("111", map2.get(1));
        assertEquals("222", map2.get(2));

        Type type3 = new TypeReference<TreeMap<Long, Short>>() {
        }.getType();
        TreeMap<Integer, String> map3 = JSON.parseObject(json, type3);
        assertEquals((short) 111, map3.get(1L));
        assertEquals((short) 222, map3.get(2L));
    }
}
