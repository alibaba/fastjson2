package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GuavaSupportTest {
    @Test
    public void testList0() {
        ImmutableList<Integer> list = ImmutableList.of();
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list, o);
    }

    @Test
    public void testList1() {
        ImmutableList<Integer> list = ImmutableList.of(1);
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list, o);
    }

    @Test
    public void testList2() {
        ImmutableList<Integer> list = ImmutableList.of(1, 2);
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list, o);
    }

    @Test
    public void testSet0() {
        ImmutableSet<Integer> list = ImmutableSet.of();
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list, o);
    }

    @Test
    public void testSet1() {
        ImmutableSet<Integer> list = ImmutableSet.of(1);
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list, o);
    }

    @Test
    public void testSet2() {
        ImmutableSet<Integer> list = ImmutableSet.of(1, 2);
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list, o);
    }

    @Test
    public void testMap0() {
        ImmutableMap<String, Integer> map = ImmutableMap.of();
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map, o);
    }

    @Test
    public void testMap1() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("k1", 1);
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map, o);
    }

    @Test
    public void testMap1x() {
        HashMap hashMap = new HashMap();
        hashMap.put("k1", 1);
        ImmutableMap<String, Integer> map = ImmutableMap.copyOf(hashMap);
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map, o);
    }

    @Test
    public void testMap2() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("k1", 1, "k2", 2);
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map, o);
    }

    @Test
    public void testArrayMap() {
        ArrayListMultimap multimap = ArrayListMultimap.create();
        multimap.put("k1", 1);
        multimap.put("k1", 2);
        byte[] bytes = JSONB.toBytes(multimap, JSONWriter.Feature.WriteClassName);
        ArrayListMultimap multimap1 = (ArrayListMultimap) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(multimap, multimap1);
    }

    @Test
    public void testArrayMap1() {
        ArrayListMultimap multimap = ArrayListMultimap.create();
        multimap.put("k1", 1);
        byte[] bytes = JSONB.toBytes(multimap, JSONWriter.Feature.WriteClassName);
        ArrayListMultimap multimap1 = (ArrayListMultimap) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(multimap, multimap1);
    }

    @Test
    public void testArrayMapField() {
        ArrayListMultimap multimap = ArrayListMultimap.create();
        multimap.put("k1", 1);
        multimap.put("k1", 2);

        Bean bean = new Bean();
        bean.multimap = multimap;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);

        Bean bean1 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(bean.multimap, bean1.multimap);
    }

    public static class Bean {
        public ArrayListMultimap<String, Integer> multimap;
    }
}
