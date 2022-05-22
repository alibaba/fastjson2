package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseMapTest {
    @Test
    public void test_ConcurrentMap() {
        String str = "{}";
        ConcurrentMap map = JSON.parseObject(str, ConcurrentMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_ConcurrentHashMap() {
        String str = "{}";
        ConcurrentHashMap map = JSON.parseObject(str, ConcurrentHashMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_ConcurrentNavigableMap() {
        String str = "{}";
        ConcurrentNavigableMap map = JSON.parseObject(str, ConcurrentNavigableMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_ConcurrentSkipListMap() {
        String str = "{}";
        ConcurrentSkipListMap map = JSON.parseObject(str, ConcurrentSkipListMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_NavigableMap() {
        String str = "{}";
        NavigableMap map = JSON.parseObject(str, NavigableMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_SortedMap() {
        String str = "{}";
        SortedMap map = JSON.parseObject(str, SortedMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_TreeMap() {
        String str = "{}";
        TreeMap map = JSON.parseObject(str, TreeMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_HashMap() {
        String str = "{}";
        HashMap map = JSON.parseObject(str, HashMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_LinkedHashMap() {
        String str = "{}";
        LinkedHashMap map = JSON.parseObject(str, LinkedHashMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_IdentityHashMap() {
        String str = "{}";
        IdentityHashMap map = JSON.parseObject(str, IdentityHashMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_AbstractMap() {
        String str = "{}";
        AbstractMap map = JSON.parseObject(str, AbstractMap.class);
        assertEquals(0, map.size());
    }

    @Test
    public void test_MyMap() {
        String str = "{}";
        MyMap map = JSON.parseObject(str, MyMap.class);
        assertEquals(0, map.size());
    }

    public static class MyMap
            extends HashMap {
    }
}
