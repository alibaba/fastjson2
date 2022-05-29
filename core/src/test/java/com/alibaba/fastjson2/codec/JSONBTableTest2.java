package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class JSONBTableTest2 {
    @Test
    public void test_0() {
        A a = new A();
        a.list0 = new ArrayList();
        a.list1 = new JSONArray();
        a.list2 = new com.alibaba.fastjson.JSONArray();

        a.list0.add(Item.of(101));
        a.list0.add(Item.of(102));

        a.list1.add(Item.of(201));
        a.list1.add(Item.of(202));

        a.list2.add(Item.of(201));
        a.list2.add(Item.of(202));

        byte[] bytes = JSONB.toBytes(a);
        JSONBDump.dump(bytes);

        A a1 = JSONB.parseObject(bytes, A.class);
        assertEquals(JSON.toJSONString(a), JSON.toJSONString(a1));
    }

    @Test
    public void test_c() {
        C c = new C();
        c.list1 = new ArrayList<>();
        c.list2 = new com.alibaba.fastjson.JSONArray();

        c.list1.add(Item.of(201));
        c.list1.add(Item.of(202));

        c.list2.add(Item.of(301));
        c.list2.add(Item.of(302));

        byte[] bytes = JSONB.toBytes(c);
        C c1 = JSONB.parseObject(bytes, C.class);
        assertEquals(JSON.toJSONString(c), JSON.toJSONString(c1));

        assertTrue(c1.list1.get(0) instanceof Item);
        assertFalse(c1.list2.get(0) instanceof Item);
    }

    @Test
    public void test_c_wrteClassName() {
        C c = new C();
        c.list1 = new ArrayList<>();
        c.list2 = new com.alibaba.fastjson.JSONArray();

        c.list1.add(Item.of(201));
        c.list1.add(Item.of(202));

        c.list2.add(Item.of(301));
        c.list2.add(Item.of(302));

        byte[] bytes = JSONB.toBytes(c, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);

        JSONBDump.dump(bytes);

        C c1 = JSONB.parseObject(bytes, C.class, JSONReader.Feature.SupportAutoType);
        assertEquals(JSON.toJSONString(c), JSON.toJSONString(c1));

        assertTrue(c1.list1.get(0) instanceof Item);
        assertTrue(c1.list2.get(0) instanceof Item);
    }

    @Test
    public void test_b() {
        B b = new B();
        b.list2 = new JSONArray();
        b.list2.add(Item.of(201));
        b.list2.add(Item.of(201));

        byte[] bytes = JSONB.toBytes(b);
        B b1 = JSONB.parseObject(bytes, B.class);
        assertEquals(JSON.toJSONString(b), JSON.toJSONString(b1));
        assertFalse(b1.list2.get(0) instanceof Item);
    }

    @Test
    public void test_b_writeClassName() {
        B b = new B();
        b.list2 = new JSONArray();
        b.list2.add(Item.of(201));
        b.list2.add(Item.of(201));

        byte[] bytes = JSONB.toBytes(b, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);
        B b1 = JSONB.parseObject(bytes, B.class, JSONReader.Feature.SupportAutoType);
        assertEquals(JSON.toJSONString(b), JSON.toJSONString(b1));
        assertTrue(b1.list2.get(0) instanceof Item);
    }

    @Test
    public void test_d() {
        D d = new D();
        d.list2 = new com.alibaba.fastjson.JSONArray();
        d.list2.add(Item.of(201));
        d.list2.add(Item.of(201));

        byte[] bytes = JSONB.toBytes(d);
        D d1 = JSONB.parseObject(bytes, D.class);
        assertEquals(JSON.toJSONString(d), JSON.toJSONString(d1));
        assertFalse(d1.list2.get(0) instanceof Item);
    }

    public static class A {
        public ArrayList list0;
        public JSONArray list1;
        public com.alibaba.fastjson.JSONArray list2;
    }

    public static class Item {
        public int id;

        public static Item of(int id) {
            Item b = new Item();
            b.id = id;
            return b;
        }
    }

    public static class B {
        public JSONArray list2;
    }

    public static class C {
        public ArrayList<Item> list1;
        public com.alibaba.fastjson.JSONArray list2;
    }

    static class D {
        public com.alibaba.fastjson.JSONArray list2;
    }
}
