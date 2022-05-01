package com.alibaba.fastjson2.support.guava;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImmutableListTest {
    @Test
    public void test_0() {
        A a = JSON.parseObject("{\"values\":[1,2]}", A.class);
        assertEquals(2, a.values.size());
    }

    @Test
    public void test_1() {
        B b = JSON.parseObject("{\"values\":[1,2]}", B.class);
        assertEquals(2, b.values.size());
        assertEquals("1", b.values.get(0));
        assertEquals("2", b.values.get(1));
    }

    private static class A {
        public ImmutableList<Integer> values;
    }

    private static class B {
        public ImmutableList<String> values;
    }
}
