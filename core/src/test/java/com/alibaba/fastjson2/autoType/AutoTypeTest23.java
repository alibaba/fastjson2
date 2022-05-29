package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest23 {
    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();
        bean.items = new XItem[0];
        bean.items1 = bean.items;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.items);
        assertNotNull(bean2.items1);

        assertSame(bean2.items, bean2.items1);
    }

    public static class Bean1 {
        public Item[] items;
        public Item[] items1;
    }

    public abstract static class Item {
        public int id;
    }

    public static class XItem
            extends Item {
        public XItem(int id) {
            this.id = id;
        }
    }

    @Test
    public void test_2() throws Exception {
        Bean2 bean = new Bean2();
        bean.items = new String[]{"a", "b"};
        bean.items1 = bean.items;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.items);
        assertNotNull(bean2.items1);

        assertSame(bean2.items, bean2.items1);
    }

    public static class Bean2 {
        public String[] items;
        public String[] items1;
    }
}
