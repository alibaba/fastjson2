package com.alibaba.fastjson2.support.guava;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImmutableMapTest {
    @Test
    public void test_0() {
        A a = JSON.parseObject("{\"values\":{\"a\":1,\"b\":2}}", A.class);
        assertEquals(2, a.values.size());
        assertEquals(Integer.valueOf(1), a.values.get("a"));
        assertEquals(Integer.valueOf(2), a.values.get("b"));
    }

    @Test
    public void test_1() {
        B b = JSON.parseObject("{\"values\":{\"a\":1,\"b\":2}}", B.class);
        assertEquals(2, b.values.size());
        assertEquals("1", b.values.get("a"));
        assertEquals("2", b.values.get("b"));
    }

    private static class A {
        public ImmutableMap<String, Integer> values;
    }

    private static class B {
        public ImmutableMap<String, String> values;
    }
}
