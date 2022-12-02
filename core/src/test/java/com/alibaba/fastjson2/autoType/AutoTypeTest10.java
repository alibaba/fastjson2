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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest10 {
    @Test
    public void test_0() {
        List list = new ArrayList<>();
        list.add(new com.alibaba.fastjson.JSONObject().fluentPut("id", 101));
        list.add(new com.alibaba.fastjson.JSONObject().fluentPut("id", 102));

        Bean bean = new Bean();
        bean.values = list;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        System.out.println(JSON.toJSONString(JSONB.parse(bytes, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased)));

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        List list2 = bean2.values;

        assertNotNull(list2);
        assertEquals(list.getClass(), list2.getClass());
        assertEquals(list.get(0).getClass(), list2.get(0).getClass());
    }

    @Test
    public void test_0_ref() {
        List list = new ArrayList<>();
        list.add(new com.alibaba.fastjson.JSONObject().fluentPut("id", 101));
        list.add(list.get(0));
        list.add(list.get(0));
        list.add(list.get(0));

        Bean bean = new Bean();
        bean.values = list;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);
        System.out.println(JSON.toJSONString(
                JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)));

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        List list2 = bean2.values;

        assertEquals(list2.getClass(), list2.getClass());
        assertEquals(list2.get(0).getClass(), list2.get(0).getClass());
    }

    @Test
    public void test_1() {
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        List list = new ArrayList();
        list.add(new com.alibaba.fastjson.JSONObject());
        object.put("data", list);

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        JSONBDump.dump(bytes);

        com.alibaba.fastjson.JSONObject object2 = (com.alibaba.fastjson.JSONObject) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        ArrayList list2 = (ArrayList) object2.get("data");
        assertEquals(list.get(0).getClass(), list2.get(0).getClass());
    }

    @Test
    public void test_2() {
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        List list = new ArrayList();
        list.add(new com.alibaba.fastjson.JSONObject());
        object.put("data", list);

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        //System.out.println(JSON.toJSONString(JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)));

        com.alibaba.fastjson.JSONObject object2 = (com.alibaba.fastjson.JSONObject) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        ArrayList list2 = (ArrayList) object2.get("data");
        assertEquals(list.get(0).getClass(), list2.get(0).getClass());
    }

    public static class Bean {
        List values;
    }

    @Test
    public void test_2_ordered() {
        Object[] array = new Object[2];
        array[0] = 1L;
        array[1] = new com.alibaba.fastjson.JSONObject(true);

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        //System.out.println(JSON.toJSONString(JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)));

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(array[0].getClass(), array2[0].getClass());
        assertEquals(array[1].getClass(), array2[1].getClass());

        com.alibaba.fastjson.JSONObject object = (com.alibaba.fastjson.JSONObject) array[1];
        com.alibaba.fastjson.JSONObject object2 = (com.alibaba.fastjson.JSONObject) array2[1];
        assertEquals(object.getInnerMap().getClass(), object2.getInnerMap().getClass());
    }

    @Test
    public void test_3() {
        Bean2 bean = new Bean2();
        bean.value = 1L;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        //System.out.println(JSON.toJSONString(JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)));

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.value, bean2.value);
    }

    public static class Bean2 {
        Long value;
    }

    @Test
    public void test_3_ordered() {
        Bean3 bean = new Bean3();
        bean.params = new com.alibaba.fastjson.JSONObject(true);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);

        JSONBDump.dump(bytes);

        Bean3 bean2 = (Bean3) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.params.getClass(), bean2.params.getClass());

        com.alibaba.fastjson.JSONObject object = (com.alibaba.fastjson.JSONObject) bean.params;
        com.alibaba.fastjson.JSONObject object2 = (com.alibaba.fastjson.JSONObject) bean2.params;
        assertEquals(object.getInnerMap().getClass(), object2.getInnerMap().getClass());
    }

    public static class Bean3 {
        Map<String, Object> params;
    }

    @Test
    public void test_4() {
        Bean4 bean = new Bean4();
        bean.params = new LinkedList<>();
        bean.params.add(new Item4());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        //System.out.println(JSON.toJSONString(JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)));

        Bean4 bean2 = (Bean4) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.params.getClass(), bean2.params.getClass());
        assertEquals(bean.params.get(0).getClass(), bean2.params.get(0).getClass());
    }

    @Test
    public void test_4_1() {
        List list = new ArrayList();
        Bean4 bean = new Bean4();
        bean.params = new LinkedList<>();
        bean.params.add(new Item4());
        list.add(bean);
        list.add(new Bean4());

        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);
        JSONBDump.dump(bytes);

        List list2 = (List) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        Bean4 bean2 = (Bean4) list2.get(0);
        assertEquals(bean.params.getClass(), bean2.params.getClass());
        assertEquals(bean.params.get(0).getClass(), bean2.params.get(0).getClass());
    }

    public static class Bean4 {
        public List<Item4> params;
    }

    public static class Item4 {
    }
}
