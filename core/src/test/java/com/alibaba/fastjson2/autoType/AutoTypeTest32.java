package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest32 {
    @Test
    public void test_1() throws Exception {
        List list = new LinkedList();

        Bean bean = new Bean();
        bean.items = new LinkedList<>();
        bean.items.add(new Item());
        bean.items.add(new Item());
        bean.items.add(new Item());
        list.add(bean);
        list.add(new Bean());
        list.add(new Bean());

        byte[] bytes = JSONB.toBytes(list,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        List<Bean> list2 = (List) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(list2);

        Bean bean2 = list2.get(0);
        assertEquals(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
    }

    @Test
    public void test_2() throws Exception {
        List list = new LinkedList();

        Bean bean = new Bean();
        bean.items = new LinkedList<>();
        bean.items.add(new Item());
        bean.items.add(new Item());
        bean.items.add(new Item());
        list.add(bean);

        {
            Bean bean1 = new Bean();
            bean1.items = Collections.singletonList(new Item());
            list.add(bean1);
        }
        list.add(new Bean());

        byte[] bytes = JSONB.toBytes(list,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        List<Bean> list2 = (List) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(list2);

        {
            Bean bean2 = list2.get(0);
            assertEquals(bean.items.getClass(), bean2.items.getClass());
            assertEquals(bean.items.size(), bean2.items.size());
        }
        {
            Bean bean1 = (Bean) list.get(1);
            Bean bean2 = list2.get(1);
            assertEquals(bean1.items.getClass(), bean2.items.getClass());
            assertEquals(bean1.items.size(), bean2.items.size());
        }
    }

    public static class Bean {
        private List<Item> items;
    }

    public static class Item {
        public int id;
    }
}
