package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest21 {
    @Test
    public void test_1() throws Exception {
        Item item = new Item(101);
        Bean1 bean = new Bean1();
        bean.item = item;

        bean.values = new LinkedHashMap<>();
        bean.values.put(item, "101");

        bean.values2 = new LinkedHashMap<>();
        bean.values2.put(item, "101");

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());

        assertEquals(bean2.item, bean2.values.keySet().iterator().next());
        assertEquals(bean2.item, bean2.values2.keySet().iterator().next());
    }

    public static class Bean1 {
        Item item;
        Map<Item, String> values;
        Map<Item, String> values2;

        public Bean1() {
        }
    }

    public static class Item {
        public int id;

        public Item(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Item item = (Item) o;
            return id == item.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @Test
    public void test_1_x() throws Exception {
        Item item = new Item(101);
        Bean1 bean = new Bean1();
        bean.item = item;

        bean.values = new HashMap<>();
        bean.values.put(item, "101");

        bean.values2 = new HashMap<>();
        bean.values2.put(item, null);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.WriteNameAsSymbol);

        JSONBDump.dump(bytes);

        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest21$Bean1#0\",\n" +
                "\t\"@value\":{\n" +
                "\t\t\"item#1\":{\n" +
                "\t\t\t\"id#2\":101\n" +
                "\t\t},\n" +
                "\t\t\"values#3\":{\n" +
                "\t\t\t\"@type\":\"M#4\",\n" +
                "\t\t\t\"@value\":{\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest21$Item#5\",\n" +
                "\t\t\t\t\t\"@value\":{\n" +
                "\t\t\t\t\t\t\"#2\":101\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}:\"101\"\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\"values2#6\":{\n" +
                "\t\t\t\"@type\":\"#4\",\n" +
                "\t\t\t\"@value\":{\n" +
                "\t\t\t\t{\"$ref\":\"$.item\"}:null\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}", new JSONBDump(bytes, true).toString());

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());

        assertEquals(bean2.item, bean2.values.keySet().iterator().next());
        assertEquals(bean2.item, bean2.values2.keySet().iterator().next());
    }

    public static class Bean2 {
        Item item;
        Map values;
        Map values2;
    }

    @Test
    public void test_2() throws Exception {
        Item item = new Item(101);
        Bean2 bean = new Bean2();
        bean.item = item;

        bean.values = new HashMap<>();
        bean.values.put(item, "101");

        bean.values2 = new HashMap<>();
        bean.values2.put(item, null);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteNulls);

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());

        assertEquals(bean2.item, bean2.values.keySet().iterator().next());
        assertEquals(bean2.item, ((Map.Entry) bean2.values2.entrySet().iterator().next()).getKey());
    }
}
