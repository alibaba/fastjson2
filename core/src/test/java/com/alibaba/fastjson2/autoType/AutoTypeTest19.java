package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest19 {
    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();
        bean.id = 123;
        bean.values = new LinkedList<>();
        bean.values.add(new Item("101", 1, null));
        bean.values.add(new Item("102", 2L, new Date()));
        bean.values.add(new Item("103", "3", new Date()));

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
        assertEquals(bean.values.get(1).getClass(), bean2.values.get(1).getClass());
    }

    public static class Bean1 {
        public Integer id;
        public boolean f1;
        public Item item;
        public long v1;
        public List<Item> values;
    }

    public static class Item {
        private Date gmtCreate;
        private String key;
        public Object value;

        public Item(String key, Object value, Date gmtCreate) {
            this.key = key;
            this.value = value;
            this.gmtCreate = gmtCreate;
        }
    }

    @Test
    public void test_2() throws Exception {
        Bean2 bean = new Bean2();
        bean.values = new LinkedList<>();
        bean.values.add(new XItem2());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
    }

    @Test
    public void test_2_1() throws Exception {
        Bean2 bean = new Bean2();
        bean.values = new LinkedList<>();
        bean.values.add(new XItem2());
        bean.values.add(new XItem2());
        bean.values.add(new XItem2());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
        assertEquals(bean.values.get(1).getClass(), bean2.values.get(1).getClass());
        assertEquals(bean.values.get(2).getClass(), bean2.values.get(2).getClass());
    }

    public static class Bean2 {
        public List<Item2> values;
    }

    abstract static class Item2 {
        public int id;
        public int value;
    }

    static class XItem2
            extends Item2 {
    }
}
