package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest41_dupRef {
    @Test
    public void test_1() throws Exception {
        Item item = new Item(1001);

        Bean bean = new Bean();
        bean.item0 = item;
        bean.item1 = item;
        bean.item2 = item;
        bean.item3 = item;

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        );

//        JSONBDump.dump(bytes);

        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest41_dupRef$Bean#0\",\n" +
                "\t\"@value\":{\n" +
                "\t\t\"item0#1\":{\n" +
                "\t\t\t\"id#2\":1001\n" +
                "\t\t},\n" +
                "\t\t\"item1#3\":{\"$ref\":\"$.item0\"},\n" +
                "\t\t\"item2#4\":{\"$ref\":\"#-1\"},\n" +
                "\t\t\"item3#5\":{\"$ref\":\"#-1\"}\n" +
                "\t}\n" +
                "}", new JSONBDump(bytes, true).toString());

        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest41_dupRef$Bean\",\n" +
                "\t\"item0\":{\n" +
                "\t\t\"id\":1001\n" +
                "\t},\n" +
                "\t\"item1\":{\"$ref\":\"$.item0\"},\n" +
                "\t\"item2\":{\"$ref\":\"#-1\"},\n" +
                "\t\"item3\":{\"$ref\":\"#-1\"}\n" +
                "}", new JSONBDump(bytes, false).toString());

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.item0.getClass(), bean2.item0.getClass());
        assertSame(bean2.item0, bean2.item1);
        assertSame(bean2.item0, bean2.item2);
        assertSame(bean2.item0, bean2.item3);
    }

    public static class Bean {
        private Item item0;
        private Item item1;
        private Item item2;
        private Item item3;
    }

    public static class Item {
        public int id;

        public Item(int id) {
            this.id = id;
        }
    }
}
