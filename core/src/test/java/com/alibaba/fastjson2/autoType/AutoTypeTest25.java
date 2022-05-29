package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest25 {
    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();
        bean.items = new TreeMap<>();
        bean.items.put("a", new Item());

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue
        );

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.items);

        assertSame(bean.items.getClass(), bean2.items.getClass());
        assertSame(bean.items.size(), bean2.items.size());
        assertSame(bean.items.get("a").getClass(), bean2.items.get("a").getClass());
        assertSame(bean.items.get("a").id, bean2.items.get("a").id);
    }

    public static class Bean1 {
        Map<String, Item> items;
    }

    public static class Item {
        public int id;
        public long v1;
        public boolean val;
        public Integer value1;
        public Long value2;
        public String value3;
        public Boolean value4;
        Child child;
        List<Child> chilren;
        Child[] a1;
    }

    public static class Child {
    }
}
