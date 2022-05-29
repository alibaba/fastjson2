package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest38_DupType {
    @Test
    public void test_1() throws Exception {
        Bean bean = new Bean();
        bean.first = new Item(101, "DataWorks");
        bean.second = new Item(102, "MaxCompute");
        bean.third = new Item(102, "EMR");

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.getClass(), bean2.getClass());
        assertEquals(bean.first.getClass(), bean2.first.getClass());
        assertEquals(bean.second.getClass(), bean2.second.getClass());
        assertEquals(bean.third.getClass(), bean2.third.getClass());

        assertEquals(((Item) bean.first).id, ((Item) bean2.first).id);
        assertEquals(((Item) bean.second).id, ((Item) bean2.second).id);
        assertEquals(((Item) bean.third).id, ((Item) bean2.third).id);

        assertEquals(((Item) bean.first).name, ((Item) bean2.first).name);
        assertEquals(((Item) bean.second).name, ((Item) bean2.second).name);
        assertEquals(((Item) bean.third).name, ((Item) bean2.third).name);
    }

    public static class Bean {
        public Object first;
        public Object second;
        public Object third;
    }

    public static class Item {
        public int id;
        public String name;

        public Item() {
        }

        public Item(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
