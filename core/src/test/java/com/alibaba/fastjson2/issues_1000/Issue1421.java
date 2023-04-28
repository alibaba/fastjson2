package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1421 {
    @Test
    public void testList() {
        List list = Collections.synchronizedList(Arrays.asList(1, 2, 3));
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        List list2 = (List) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list.size(), list2.size());
        assertEquals(list.getClass(), list2.getClass());
    }

    @Test
    public void testCollection() {
        Collection list = Collections.synchronizedCollection(Arrays.asList(1, 2, 3));
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Collection list2 = (Collection) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list.size(), list2.size());
        assertEquals(list.getClass(), list2.getClass());
    }

    @Test
    public void testSet() {
        Set list = Collections.synchronizedSet(new HashSet<>(Arrays.asList(1, 2, 3)));
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Set list2 = (Set) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list.size(), list2.size());
        assertEquals(list.getClass(), list2.getClass());
    }

    @Test
    public void testSet2() {
        Set list = Collections.synchronizedSortedSet(new TreeSet<>(Arrays.asList(1, 2, 3)));
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Set list2 = (Set) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list.size(), list2.size());
        assertEquals(list.getClass(), list2.getClass());
    }

    @Test
    public void testSet3() {
        Set list = Collections.synchronizedNavigableSet(new TreeSet<>(Arrays.asList(1, 2, 3)));
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Set list2 = (Set) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list.size(), list2.size());
        assertEquals(list.getClass(), list2.getClass());
    }

    @Test
    public void testMap() {
        Map map = Collections.synchronizedMap(new TreeMap<>());
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Map map2 = (Map) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map.size(), map2.size());
        assertEquals(map.getClass(), map2.getClass());
    }

    @Test
    public void testMap2() {
        Map map = Collections.synchronizedNavigableMap(new TreeMap<>());
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Map map2 = (Map) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map.size(), map2.size());
        assertEquals(map.getClass(), map2.getClass());
    }

    @Test
    public void testMap3() {
        Map map = Collections.synchronizedSortedMap(new TreeMap<>());
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Map map2 = (Map) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map.size(), map2.size());
        assertEquals(map.getClass(), map2.getClass());
    }
}
