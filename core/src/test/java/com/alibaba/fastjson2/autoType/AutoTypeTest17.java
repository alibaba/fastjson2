package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest17 {
    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();
        bean.values = new LinkedList<>();
        bean.values.add(new XItem());
        bean.values.add(new Item());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

//        JSONBDump.dum(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
        assertEquals(bean.values.get(1).getClass(), bean2.values.get(1).getClass());
    }

    public static class Bean1 {
        public List<Item> values;
    }

    public static class Item {
        public Item() {
        }
    }

    public static class XItem
            extends Item {
    }

    @Test
    public void test_2() throws Exception {
        Bean2 bean = new Bean2();
        bean.values = new Object[2];
        bean.values[0] = new XItem();
        bean.values[1] = new Item();

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.length, bean2.values.length);
        assertEquals(bean.values[0].getClass(), bean2.values[0].getClass());
        assertEquals(bean.values[1].getClass(), bean2.values[1].getClass());
    }

    public static class Bean2 {
        public Object[] values;
    }

    @Test
    public void test_3() throws Exception {
        Bean3 bean = new Bean3();
        bean.values = new Item[2];
        bean.values[0] = new XItem();
        bean.values[1] = new Item();

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean3 bean2 = (Bean3) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.length, bean2.values.length);
        assertEquals(bean.values[0].getClass(), bean2.values[0].getClass());
        assertEquals(bean.values[1].getClass(), bean2.values[1].getClass());
    }

    public static class Bean3 {
        public Item[] values;
    }

    @Test
    public void test_4() throws Exception {
        Bean4 bean = new Bean4();
        bean.values = new LinkedHashMap<>();
        bean.values.put("0", new XItem());
        bean.values.put("1", new Item());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean4 bean2 = (Bean4) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get("0").getClass(), bean2.values.get("0").getClass());
        assertEquals(bean.values.get("1").getClass(), bean2.values.get("1").getClass());
    }

    public static class Bean4 {
        public Map<String, Item> values;
    }

    @Test
    public void test_5() throws Exception {
        {
            Bean5 bean = new Bean5();
            bean.item = new Item();

            byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

            JSONBDump.dump(bytes);

            Bean5 bean2 = (Bean5) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
            assertNotNull(bean2);
            assertNotNull(bean2.item);
            assertEquals(bean.item.getClass(), bean2.item.getClass());
        }
        {
            Bean5 bean = new Bean5();
            bean.item = new XItem();

            byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

            JSONBDump.dump(bytes);

            Bean5 bean2 = (Bean5) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
            assertNotNull(bean2);
            assertNotNull(bean2.item);
            assertEquals(bean.item.getClass(), bean2.item.getClass());
        }
    }

    public static class Bean5 {
        public Item item;
    }

    @Test
    public void test_6() throws Exception {
        Bean6 bean = new Bean6();
        bean.values = new LinkedHashMap<>();
        bean.values.put("0", new XItem());
        bean.values.put("1", new Item());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean6 bean2 = (Bean6) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get("0").getClass(), bean2.values.get("0").getClass());
        assertEquals(bean.values.get("1").getClass(), bean2.values.get("1").getClass());
    }

    public static class Bean6 {
        public LinkedHashMap<String, Item> values;
    }

    @Test
    public void test_7() throws Exception {
        Bean7 bean = new Bean7();
        bean.values = new LinkedList<>();
        bean.values.add(new XItem());
        bean.values.add(new Item());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

//        JSONBDump.dum(bytes);

        Bean7 bean2 = (Bean7) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
        assertEquals(bean.values.get(1).getClass(), bean2.values.get(1).getClass());
    }

    public static class Bean7 {
        public LinkedList<Item> values;
    }

    @Test
    public void test_8() throws Exception {
        Bean8 bean = new Bean8();
        bean.values = new LinkedHashMap<>();
        bean.values.put(0, new XItem());
        bean.values.put(1, new Item());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean8 bean2 = (Bean8) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
        assertEquals(bean.values.get(1).getClass(), bean2.values.get(1).getClass());
    }

    public static class Bean8 {
        public Map<Object, Item> values;
    }
}
