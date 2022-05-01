package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;

public class ParseSetTest {
    @Test
    public void test_Set() {
        Set set = JSON.parseObject("[]", Set.class);
        assertTrue(set.isEmpty());
    }

    @Test
    public void test_HashSet() {
        HashSet set = JSON.parseObject("[]", HashSet.class);
        assertTrue(set.isEmpty());
    }

    @Test
    public void test_LinkedHashSet() {
        LinkedHashSet set = JSON.parseObject("[]", LinkedHashSet.class);
        assertTrue(set.isEmpty());
    }

    @Test
    public void test_TreeSet() {
        TreeSet set = JSON.parseObject("[]", TreeSet.class);
        assertTrue(set.isEmpty());
    }

    @Test
    public void test_AbstractSet() {
        AbstractSet set = JSON.parseObject("[]", AbstractSet.class);
        assertTrue(set.isEmpty());
    }

    @Test
    public void test_NavigableSet() {
        NavigableSet set = JSON.parseObject("[]", NavigableSet.class);
        assertTrue(set.isEmpty());
    }

    @Test
    public void test_ConcurrentSkipListSet() {
        ConcurrentSkipListSet set = JSON.parseObject("[]", ConcurrentSkipListSet.class);
        assertTrue(set.isEmpty());
    }

    @Test
    public void test_emptySet() {
        Class<Set> clazz = (Class<Set>) Collections.emptySet().getClass();
        Set set = JSON.parseObject("[]", clazz);
        assertTrue(set.isEmpty());
        assertSame(Collections.emptySet(), set);
    }

    @Test
    public void test_emptySet_jsonb() {
        Class<Set> clazz = (Class<Set>) Collections.emptySet().getClass();
        byte[] jsonbBytes = JSONB.toBytes(Collections.emptySet());
        Set set = JSONB.parseObject(jsonbBytes, clazz);
        assertTrue(set.isEmpty());
        assertSame(Collections.emptySet(), set);
    }

    @Test
    public void test_emptyList() {
        Class clazz = Collections.emptyList().getClass();
        List list = (List) JSON.parseObject("[]", clazz);
        assertTrue(list.isEmpty());
        assertSame(Collections.emptyList(), list);
    }

    @Test
    public void test_emptyList_jsonb() {
        Class<List> clazz = (Class<List>) Collections.emptyList().getClass();
        byte[] jsonbBytes = JSONB.toBytes(Collections.emptySet());
        List list = JSONB.parseObject(jsonbBytes, clazz);
        assertTrue(list.isEmpty());
        assertSame(Collections.emptyList(), list);
    }

    @Test
    public void test_singleton() {
        Class clazz = Collections.singleton(1).getClass();
        Collection singleton = (Collection) JSON.parseObject("[101]", clazz);
        assertFalse(singleton.isEmpty());
        assertEquals(1, singleton.size());
        assertEquals(101, singleton.stream().findFirst().get());
    }

    @Test
    public void test_singleton_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(101));

        Class clazz = Collections.singleton(1).getClass();
        Collection<Number> singleton = (Collection<Number>) JSONB.parseObject(jsonbBytes, clazz);
        assertFalse(singleton.isEmpty());
        assertEquals(1, singleton.size());
        assertEquals(101, singleton.stream().findFirst().get().intValue());
    }
}
