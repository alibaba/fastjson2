package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest9 {
    @Test
    public void test_0() {
        Bean bean = new Bean();
        bean.values = new HashSet();

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
    }

    public static class Bean {
        Set values;
    }

    @Test
    public void test_2() {
        Bean2 bean = new Bean2();
        bean.values = new HashSet();
        bean.values.add(1L);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
    }

    public static class Bean2 {
        Set<Long> values;
    }

    @Test
    public void test_3() {
        Bean3 bean = new Bean3();
        bean.values = new com.alibaba.fastjson.JSONObject();
        bean.values.put("set", new HashSet());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        Bean3 bean2 = (Bean3) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.get("set").getClass(), bean2.values.get("set").getClass());
    }

    public static class Bean3 {
        Map values;
    }

    @Test
    public void test_4() {
        Bean4 bean = new Bean4();
        bean.values = new java.util.ArrayList<>();
        bean.values.add(new com.alibaba.fastjson.JSONObject());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        Bean4 bean2 = (Bean4) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
    }

    @Test
    public void test_4_1() {
        Bean4 bean = new Bean4();
        bean.values = new java.util.ArrayList<>();
        bean.values.add(new LinkedHashMap());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        Bean4 bean2 = (Bean4) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
    }

    public static class Bean4 {
        java.util.ArrayList values;
    }

    @Test
    public void test_5() {
        Bean5 bean = new Bean5();
        bean.values = new com.alibaba.fastjson.JSONObject();
        ArrayList<Object> list = new ArrayList<>();
        bean.values.put("data", list);
        list.add(new com.alibaba.fastjson.JSONObject());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        Bean5 bean2 = (Bean5) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.get("data").getClass(), bean2.values.get("data").getClass());
        assertEquals(
                ((ArrayList) bean.values.get("data")).get(0).getClass(),
                ((ArrayList) bean2.values.get("data")).get(0).getClass()
        );
    }

    public static class Bean5 {
        com.alibaba.fastjson.JSONObject values;
    }
}
