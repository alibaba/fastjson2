package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.f0 = true;
        Map<String, Object> paths = JSONPath.paths(bean);
        assertEquals(true, paths.get("$.f0"));
    }

    @Test
    public void test0() {
        assertEquals(Boolean.TRUE, JSONPath.paths(Boolean.TRUE).get("$"));
        assertEquals(BigDecimal.ONE, JSONPath.paths(BigDecimal.ONE).get("$"));
    }

    public static class Bean {
        public Boolean f0;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.f0 = UUID.randomUUID();
        bean.f1 = bean.f0;
        bean.f2 = new BigDecimal("123.45");
        bean.f3 = bean.f2;
        bean.f4 = Boolean.TRUE;
        bean.f5 = bean.f4;

        bean.c0 = Character.valueOf('A');
        bean.c1 = bean.c0;

        bean.str0 = "ABC";
        bean.str1 = bean.str0;

        bean.d0 = new Date();
        bean.d1 = bean.d0;

        bean.unit0 = TimeUnit.DAYS;
        bean.unit1 = bean.unit0;

        bean.item = new Item();

        Map<String, Object> paths = JSONPath.paths(bean);
        assertEquals(16, paths.size());
    }

    public static class Bean1 {
        public UUID f0;
        public UUID f1;
        public Number f2;
        public Number f3;
        public Boolean f4;
        public Boolean f5;

        public Character c0;
        public Character c1;

        public String str0;
        public String str1;

        public Date d0;
        public Date d1;

        public TimeUnit unit0;
        public TimeUnit unit1;

        public Item item;
    }

    public static class Item {
    }
}
