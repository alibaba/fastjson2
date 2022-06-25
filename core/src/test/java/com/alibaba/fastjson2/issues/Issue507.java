package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue507 {
    @Test
    public void test() {
        Map<Long, String> map = new HashMap<>();
        map.put(1L, "张三");
        map.put(2L, "张四");

        String str = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"@type\":\"java.util.HashMap\",1L:\"张三\",2L:\"张四\"}", str);

        Map map2 = (Map) JSON.parseObject(str, HashMap.class);
        assertEquals(1L, map2.keySet().iterator().next());
        assertEquals("张三", map2.get(1L));
        assertEquals("张四", map2.get(2L));
    }

    @Test
    public void test1() {
        Map<Long, String> map = new HashMap<>();
        map.put(1L, "张三");
        map.put(2L, "张四");

        String str = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"@type\":\"java.util.HashMap\",1L:\"张三\",2L:\"张四\"}", str);

        Map map2 = (Map) JSON.parseObject(str, Object.class);
        assertEquals("张三", map2.get(1L));
        assertEquals("张四", map2.get(2L));
    }

    @Test
    public void test2() {
        String str = "{1L:\"张三\",2L:\"张四\"}";

        Map object = (Map) JSON.parseObject(str, Object.class);
        assertEquals("张三", object.get(1L));
        assertEquals("张四", object.get(2L));
        assertEquals(2, object.size());

        Map map = JSON.parseObject(str, Map.class);
        assertEquals("张三", map.get(1L));
        assertEquals("张四", map.get(2L));
        assertEquals(2, map.size());

        Map map1 = JSON.parseObject(str, LinkedHashMap.class);
        assertEquals("张三", map1.get(1L));
        assertEquals("张四", map1.get(2L));
        assertEquals(2, map1.size());

        Map map2 = JSON.parseObject(str, HashMap.class);
        assertEquals("张三", map2.get(1L));
        assertEquals("张四", map2.get(2L));
        assertEquals(2, map2.size());
    }

    @Test
    public void test3() {
        String str = "{1L:\"张三\",2L:\"张四\"}";

        Map map = JSON.parseObject(str, new TypeReference<Map<Number, String>>(){});
        assertEquals("张三", map.get(1L));
        assertEquals("张四", map.get(2L));
        assertEquals(2, map.size());

        Map map1 = JSON.parseObject(str, new TypeReference<Map<Number, Object>>(){});
        assertEquals("张三", map1.get(1L));
        assertEquals("张四", map1.get(2L));
        assertEquals(2, map1.size());

        Map map2 = JSON.parseObject(str, new TypeReference<Map<Integer, Object>>(){});
        assertEquals("张三", map2.get(1));
        assertEquals("张四", map2.get(2));
        assertEquals(2, map2.size());
    }
}
