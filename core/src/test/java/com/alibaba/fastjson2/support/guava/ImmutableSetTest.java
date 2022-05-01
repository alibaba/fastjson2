package com.alibaba.fastjson2.support.guava;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImmutableSetTest {
    @Test
    public void test_0() {
        A a = JSON.parseObject("{\"values\":[1,2]}", A.class);
        assertEquals(2, a.values.size());
    }

    @Test
    public void test_1() {
        B b = JSON.parseObject("{\"values\":[1,2]}", B.class);
        assertEquals(2, b.values.size());
        assertEquals("1", b.values.iterator().next());
    }

    private static class A {
        public ImmutableSet<Integer> values;
    }

    private static class B {
        public ImmutableSet<String> values;
    }
}
