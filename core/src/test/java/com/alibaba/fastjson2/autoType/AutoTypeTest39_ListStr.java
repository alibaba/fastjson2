package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest39_ListStr {
    @Test
    public void test_1() throws Exception {
        String str = "abcde";
        Bean bean = new Bean();
        bean.items = new LinkedList<>();
        bean.items.add(str);
        bean.items.add(str);

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONB.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
        assertEquals(bean.items.stream().findFirst().get(), bean2.items.stream().findFirst().get());
    }

    @Test
    public void test_2() throws Exception {
        Bean bean = new Bean();
        bean.items = Collections.singletonList("abc");

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONB.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
        assertEquals(bean.items.stream().findFirst().get(), bean2.items.stream().findFirst().get());
    }

    @Test
    public void test_3() throws Exception {
        Bean bean = new Bean();
        bean.items = Collections.singletonList("abc");

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONB.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
        assertEquals(bean.items.stream().findFirst().get(), bean2.items.stream().findFirst().get());
    }

    @Test
    public void test_4() throws Exception {
        Bean bean = new Bean();
        bean.items = Arrays.asList("abc");

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONB.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
        assertEquals(bean.items.stream().findFirst().get(), bean2.items.stream().findFirst().get());
    }

    @Test
    public void test_5() throws Exception {
        List list = new LinkedList();
        list.add("abc");

        Bean bean = new Bean();
        bean.items = list;

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONB.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
        assertEquals(bean.items.stream().findFirst().get(), bean2.items.stream().findFirst().get());
    }

    public static class Bean {
        private List<String> items;
    }
}
