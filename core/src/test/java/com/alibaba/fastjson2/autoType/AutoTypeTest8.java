package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest8 {
    @Test
    public void test_0() {
        A a = new A();
        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.WriteClassName);
//        System.out.println(JSON.toJSONString(JSONB.parse(bytes)));
        A a1 = (A) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(a1);
    }

    @Test
    public void test_1() {
        B b = new B();
        byte[] bytes = JSONB.toBytes(b, JSONWriter.Feature.WriteClassName);
//        System.out.println(JSON.toJSONString(JSONB.parse(bytes)));
        B b1 = (B) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(b1);
    }

    @Test
    public void test_2() {
        Object[] array = new Object[]{
                new A(), new B()
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName);
//        System.out.println(JSON.toJSONString(JSONB.parse(bytes)));
        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(array2);
        assertEquals(A.class, array2[0].getClass());
        assertEquals(B.class, array2[1].getClass());
    }

    @Test
    public void test_3() {
        Bean bean = new Bean();
        bean.values = new A1[]{new A1()};

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(A1[].class, bean2.values.getClass());
    }

    @Test
    public void test_4() {
        List list = new ArrayList<>();
        list.add(new A());

        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        System.out.println(JSON.toJSONString(JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)));

        ArrayList list2 = (ArrayList) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(A.class, list2.get(0).getClass());
    }

    @Test
    public void test_5() {
        List list = new LinkedList();
        list.add(new A());

        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        System.out.println(JSON.toJSONString(JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)));

        LinkedList list2 = (LinkedList) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(A.class, list2.get(0).getClass());
    }

    @Test
    public void test_6() {
        List list = new LinkedList();
        list.add(new A());

        ListBean bean = new ListBean();
        bean.values = list;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        ListBean bean2 = (ListBean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        LinkedList list2 = (LinkedList) bean2.values;
        assertEquals(A.class, list2.get(0).getClass());
    }

    @Test
    public void test_7() {
        com.alibaba.fastjson.JSONArray list = new com.alibaba.fastjson.JSONArray();
        list.add(new A());

        ListBean2 bean = new ListBean2();
        bean.values = list;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        ListBean2 bean2 = (ListBean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        com.alibaba.fastjson.JSONArray list2 = (com.alibaba.fastjson.JSONArray) bean2.values;
        assertEquals(A.class, list2.get(0).getClass());
    }

    public static class Bean {
        A[] values;
    }

    public static class ListBean {
        List values;
    }

    public static class ListBean2 {
        com.alibaba.fastjson.JSONArray values;
    }

    private static class A {
    }

    static class A1
            extends A {
    }

    public static class B {
    }
}
