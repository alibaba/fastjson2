package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest16_enums {
    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();
        bean.values = new LinkedList<>();
        bean.values.add(TimeUnit.DAYS);
        bean.values.add(TimeUnit.SECONDS);
        bean.values.add(TimeUnit.DAYS);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0), bean2.values.get(0));
        assertEquals(bean.values.get(1), bean2.values.get(1));
    }

    public static class Bean1 {
        public List<TimeUnit> values;
    }

    @Test
    public void test_2() throws Exception {
        Bean2 bean = new Bean2();
        bean.values = new LinkedList<>();

        bean.values.add(new Item(new Child(11), new Child(12)));
        bean.values.add(new Item(new Child(21), new Child(22)));

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());

        JSONBDump.dump(bytes);
    }

    public static class Bean2 {
        public List<Item> values;
    }

    public static class Item {
        List<Child> children;

        public Item() {
        }

        public Item(Child child1, Child child2) {
            children = new ArrayList<>();
            children.add(child1);
            children.add(child2);
        }
    }

    public static class Child {
        public int id;

        public Child(int id) {
            this.id = id;
        }
    }
}
