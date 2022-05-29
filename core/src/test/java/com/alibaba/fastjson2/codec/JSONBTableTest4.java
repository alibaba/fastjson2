package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONBTableTest4 {
    @Test
    public void test_0() {
        A a = new A();
        a.items.add(Item.of(101, "DataWorks"));
        a.items.add(Item.of(102, "MaxCompute"));

        byte[] bytes = JSONB.toBytes(a);
        A a1 = JSONB.parseObject(bytes, A.class);
        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    @Test
    public void test_1() {
        JSONObject object = JSONObject
                .of("items",
                        JSONArray.of(
                                JSONObject.of("id", 101).fluentPut("name", "DataWorks").fluentPut("x1", "A"),
                                JSONObject.of("id", 102).fluentPut("name", "MaxCompute").fluentPut("x1", "A")
                        )
                );

        byte[] bytes = JSONB.toBytes(object);
        A a1 = JSONB.parseObject(bytes, A.class);
//        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    @Test
    public void test_b() {
        B a = new B();
        a.items.add(Item.of(101, "DataWorks"));
        a.items.add(Item.of(102, "MaxCompute"));

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);

        System.out.println(Arrays.toString(bytes));

        B a1 = JSONB.parseObject(bytes, B.class, JSONReader.Feature.SupportAutoType);
        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    @Test
    public void test_b_1() {
        JSONObject object = JSONObject
                .of("items",
                        JSONArray.of(
                                JSONObject.of("id", 101).fluentPut("name", "DataWorks").fluentPut("x1", "A"),
                                JSONObject.of("id", 102).fluentPut("name", "MaxCompute").fluentPut("x1", "A")
                        )
                );

        byte[] bytes = JSONB.toBytes(object);

        //System.out.println(Arrays.toString(bytes));

        B a1 = JSONB.parseObject(bytes, B.class, JSONReader.Feature.SupportAutoType);
        assertEquals(2, a1.items.size());
//        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    @Test
    public void test_d() {
        JSONObject object = JSONObject
                .of("items",
                        JSONArray.of(
                                JSONObject.of("id", 101).fluentPut("name", "DataWorks").fluentPut("x1", "A"),
                                JSONObject.of("id", 102).fluentPut("name", "MaxCompute").fluentPut("x1", "A")
                        )
                );

        byte[] bytes = JSONB.toBytes(object);

        //System.out.println(Arrays.toString(bytes));

        D a1 = JSONB.parseObject(bytes, D.class, JSONReader.Feature.SupportAutoType);
        assertEquals(2, a1.items.size());
//        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    @Test
    public void test_b10() {
        B10 a = new B10();
        a.items.add(Item10.of(101, "DataWorks"));
        a.items.add(Item10.of(102, "MaxCompute"));

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);

        JSONBDump.dump(bytes);

        B10 a1 = JSONB.parseObject(bytes, B10.class, JSONReader.Feature.SupportAutoType);
        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    @Test
    public void test_b10_1() {
        JSONObject object = JSONObject
                .of("items",
                        JSONArray.of(
                                JSONObject.of("id", 101).fluentPut("x", "x1"),
                                JSONObject.of("id", 102).fluentPut("x", "x2")
                        )
                );
        B10 a = object.toJavaObject(B10.class);
        assertTrue(a.items.get(0) instanceof Item10);

        byte[] bytes = JSONB.toBytes(object);

//        System.out.println(Arrays.toString(bytes));

        B10 a1 = JSONB.parseObject(bytes, B10.class, JSONReader.Feature.SupportAutoType);
//        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    @Test
    public void test_c() {
        C a = new C();
        a.items.add(Item.of(101, "DataWorks"));
        a.items.add(Item.of(102, "MaxCompute"));

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);

        //System.out.println(Arrays.toString(bytes));

        C a1 = JSONB.parseObject(bytes, C.class, JSONReader.Feature.SupportAutoType);
        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    public static class A {
        private final List<Item> items = new ArrayList<>();

        public List<Item> getItems() {
            return items;
        }
    }

    static class B {
        private final JSONArray items = new JSONArray();

        public JSONArray getItems() {
            return items;
        }
    }

    public static class C {
        private final JSONArray items = new JSONArray();

        public JSONArray getItems() {
            return items;
        }
    }

    static class B10 {
        private final List<Item10> items = new ArrayList<>();
        public Object o0;
        public Object o1;
        public Object o2;
        public Object o3;
        public Object o4;
        public Object o5;
        public Object o6;
        public Object o7;
        public Object o8;
        public Object o9;

        public List<Item10> getItems() {
            return items;
        }
    }

    public static class D {
        private final List<Item1> items = new ArrayList<>();

        public List<Item1> getItems() {
            return items;
        }
    }

    public static class Item {
        public int id;
        public String name;

        public static Item of(int id, String name) {
            Item item = new Item();
            item.id = id;
            item.name = name;
            return item;
        }
    }

    static class Item1 {
        public int id;
        public String name;

        public static Item of(int id, String name) {
            Item item = new Item();
            item.id = id;
            item.name = name;
            return item;
        }
    }

    static class Item10 {
        public int id;
        public String name;
        public Object o0;
        public Object o1;
        public Object o2;
        public Object o3;
        public Object o4;
        public Object o5;
        public Object o6;
        public Object o7;
        public Object o8;
        public Object o9;

        public static Item10 of(int id, String name) {
            Item10 item = new Item10();
            item.id = id;
            item.name = name;
            return item;
        }
    }
}
