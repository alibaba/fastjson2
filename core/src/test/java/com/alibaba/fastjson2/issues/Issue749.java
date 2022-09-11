package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue749 {
    @Test
    public void test() {
        String s = "a.a.a.a";
        String[] split = s.split("\\.");
        byte[] bytes = JSONB.toBytes(split, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(
                bytes, Object.class,
                JSONReader.autoTypeFilter(String.class)
        );
        assertTrue(o instanceof String[]);
        assertArrayEquals(split, (String[]) o);
    }

    @Test
    public void test1() {
        String s = "a.a.a.a";
        String[] split = s.split("\\.");
        byte[] bytes = JSONB.toBytes(split, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(
                bytes,
                Object.class,
                new ContextAutoTypeBeforeHandler(String.class)
        );
        System.out.println(o);
        System.out.println(o instanceof String[]);
        assertArrayEquals(split, (String[]) o);
    }

    @Test
    public void test2() {
        ContextAutoTypeBeforeHandler filter = new ContextAutoTypeBeforeHandler(String.class);

        String s = "a.a.a.a";
        String[][] split = new String[][]{s.split("\\.")};
        byte[] bytes = JSONB.toBytes(split, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(
                bytes,
                Object.class,
                filter
        );
        System.out.println(o);
        System.out.println(o instanceof String[][]);
        assertArrayEquals(split, (String[][]) o);

        Object o1 = JSONB.parseObject(
                bytes,
                String[][].class,
                filter
        );
        assertArrayEquals(split, (String[][]) o1);
    }

    @Test
    public void test3() {
        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter(String.class.getName());

        String s = "a.a.a.a";
        String[][] split = new String[][]{s.split("\\.")};
        byte[] bytes = JSONB.toBytes(split, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(
                bytes,
                Object.class,
                filter
        );
        System.out.println(o);
        System.out.println(o instanceof String[][]);
        assertArrayEquals(split, (String[][]) o);

        Object o1 = JSONB.parseObject(
                bytes,
                String[][].class,
                filter
        );
        assertArrayEquals(split, (String[][]) o1);
    }

    @Test
    public void test4() {
        ContextAutoTypeBeforeHandler filter = new ContextAutoTypeBeforeHandler(String.class.getTypeName());

        String s = "a.a.a.a";
        String[][] split = new String[][]{s.split("\\.")};
        byte[] bytes = JSONB.toBytes(split, JSONWriter.Feature.WriteClassName);
        Object o = JSONB.parseObject(
                bytes,
                Object.class,
                filter
        );
        System.out.println(o);
        System.out.println(o instanceof String[][]);
        assertArrayEquals(split, (String[][]) o);

        Object o1 = JSONB.parseObject(
                bytes,
                String[][].class,
                filter
        );
        assertArrayEquals(split, (String[][]) o1);
    }
}
