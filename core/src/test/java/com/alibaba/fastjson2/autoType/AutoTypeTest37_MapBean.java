package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest37_MapBean {
    @Test
    public void test_1() throws Exception {
        Bean bean = new Bean();
        bean.items = new HashMap<>();
        bean.items.put("a", new Item(101));
        bean.items.put("b", new Item(102));
        bean.items.put("c", new Item(103));
        bean.items.put("d", new Item(104));
        bean.items.put("e", new Item(105));
        bean.items.put("f", new Item(107));

        Item item = new Item(110);
        item.child = new Child();
        bean.items.put("z", item);

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        String str = JSONB.toJSONString(bytes, true);
        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest37_MapBean$Bean#0\",\n" +
                "\t\"@value\":{\n" +
                "\t\t\"items\":{\n" +
                "\t\t\t\"a\":{\n" +
                "\t\t\t\t\"child\":null,\n" +
                "\t\t\t\t\"id\":101\n" +
                "\t\t\t},\n" +
                "\t\t\t\"b\":{\n" +
                "\t\t\t\t\"child\":null,\n" +
                "\t\t\t\t\"id\":102\n" +
                "\t\t\t},\n" +
                "\t\t\t\"c\":{\n" +
                "\t\t\t\t\"child\":null,\n" +
                "\t\t\t\t\"id\":103\n" +
                "\t\t\t},\n" +
                "\t\t\t\"d\":{\n" +
                "\t\t\t\t\"child\":null,\n" +
                "\t\t\t\t\"id\":104\n" +
                "\t\t\t},\n" +
                "\t\t\t\"e\":{\n" +
                "\t\t\t\t\"child\":null,\n" +
                "\t\t\t\t\"id\":105\n" +
                "\t\t\t},\n" +
                "\t\t\t\"f\":{\n" +
                "\t\t\t\t\"child\":null,\n" +
                "\t\t\t\t\"id\":107\n" +
                "\t\t\t},\n" +
                "\t\t\t\"z\":{\n" +
                "\t\t\t\t\"child\":{},\n" +
                "\t\t\t\t\"id\":110\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}", str);

        assertEquals(186, bytes.length);
        // 142 200 202 216 141 151 144

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.getClass(), bean2.getClass());
        assertEquals(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
        assertEquals(bean.items.values().stream().findFirst().get().getClass(), bean2.items.values().stream().findFirst().get().getClass());
    }

    @Test
    public void test_2() throws Exception {
        HashMap map = new HashMap<>();
        map.put("a", new Item(101));
        map.put("b", new Item(102));

        Item item = new Item(103);
        item.child = new Child();
        map.put("c", item);

        byte[] bytes = JSONB.toBytes(map,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONB.dump(bytes);

        Map map2 = (Map) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(map.getClass(), map2.getClass());
        assertEquals(map.values().stream().findFirst().get().getClass(), map2.values().stream().findFirst().get().getClass());
    }

    public static class Bean {
        public Map<String, Item> items;
    }

    public static class Item {
        public int id;
        public Child child;

        public Item(int id) {
            this.id = id;
        }
    }

    public static class Child {
        public String name;
    }
}
