package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1503S {
    @Test
    public void test() {
        A1 a = new A1();
        a.id = 101;
        a.name = "abc";

        Filter AUTO_TYPE_FILTER = JSONReader.autoTypeFilter("com.alibaba.fastjson2.issues_1500");
        String json = JSON.toJSONString(a, JSONWriter.Feature.WriteClassName);
        A1 a1 = (A1) JSON.parseObject(json, A.class, AUTO_TYPE_FILTER);
        assertEquals("{\"id\":101,\"name\":\"abc\"}", JSON.toJSONString(a1));

        A a2 = JSON.parseObject(json, A.class);
        assertEquals(A.class, a2.getClass());
        assertEquals("{\"id\":101}", JSON.toJSONString(a2));

        A a3 = JSON.parseObject(json, A.class, JSONReader.Feature.SupportAutoType);
        assertEquals(A.class, a3.getClass());
        assertEquals("{\"id\":101}", JSON.toJSONString(a3));

        A a4 = JSON.parseObject(json).toJavaObject(A.class, JSONReader.Feature.SupportAutoType);
        assertEquals(A.class, a4.getClass());
        assertEquals("{\"id\":101}", JSON.toJSONString(a4));
    }

    public static class A {
        public int id;
    }

    public static class A1
            extends A {
        public String name;
    }

    @Test
    public void test1() {
        B1 b = new B1(101, "abc");

        Filter AUTO_TYPE_FILTER = JSONReader.autoTypeFilter("com.alibaba.fastjson2.issues_1500");
        String json = JSON.toJSONString(b, JSONWriter.Feature.WriteClassName);
        B1 b1 = (B1) JSON.parseObject(json, B.class, AUTO_TYPE_FILTER);
        assertEquals("{\"id\":101,\"name\":\"abc\"}", JSON.toJSONString(b1));

        B b2 = JSON.parseObject(json, B.class);
        assertEquals(B.class, b2.getClass());
        assertEquals("{\"id\":101}", JSON.toJSONString(b2));

        B b3 = JSON.parseObject(json, B.class, JSONReader.Feature.SupportAutoType);
        assertEquals(B.class, b3.getClass());
        assertEquals("{\"id\":101}", JSON.toJSONString(b3));

        B b4 = JSON.parseObject(json).toJavaObject(B.class, JSONReader.Feature.SupportAutoType);
        assertEquals(B.class, b4.getClass());
        assertEquals("{\"id\":101}", JSON.toJSONString(b4));
    }

    public static class B {
        private final int id;

        public B(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static class B1
            extends B {
        private final String name;

        public B1(int id, String name) {
            super(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
