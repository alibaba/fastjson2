package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Differ;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AutoTypeTest36_SetLong {
    @Test
    public void test_1() throws Exception {
        Bean bean = new Bean();
        bean.values = new HashSet<>();
        bean.values.add(100L);
        bean.values1 = new HashSet<>();
        bean.values1.add(100L);

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
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.stream().findFirst().get(), bean2.values.stream().findFirst().get());
    }

    public static class Bean {
        public Set<Long> values;
        public Set<Long> values1;
    }

    @Test
    public void test_2() throws Exception {
        List list = new ArrayList<>();

        Bean2 bean = new Bean2();
        bean.values = new HashSet<>();
        bean.values.add(100L);

        list.add(bean);
        {
            Bean2 bean1 = new Bean2();
            bean1.values = new HashSet<>();
            bean1.values.add(200L);
            list.add(bean1);
        }
        {
            Bean2 bean1 = new Bean2();
            bean1.values = new HashSet<>();
            bean1.values.add(300L);
            list.add(bean1);
        }

        byte[] bytes = JSONB.toBytes(list,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        List list2 = (List) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        Bean2 bean2 = (Bean2) list2.get(0);

        assertEquals(bean.getClass(), bean2.getClass());
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.stream().findFirst().get(), bean2.values.stream().findFirst().get());
    }

    @Test
    public void test_2_integer() throws Exception {
        HashSet set = new HashSet<>();
        set.add(100);
        Bean2 bean = new Bean2();
        bean.values = set;

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.getClass(), bean2.getClass());
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        // assertEquals(bean.values.stream().findFirst().get() , bean2.values.stream().findFirst().get());
        Differ differ = new Differ(bean, bean2);
        assertTrue(differ.diff());
    }

    public static class Bean2 {
        public Set<Long> values;
    }
}
