package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest27 {
    @Test
    public void test_unmodifiableCollection() throws Exception {
        Collection collection = new ArrayList<>();
        Bean1 bean = new Bean1();
        bean.items = Collections.unmodifiableCollection(collection);

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
    }

    @Test
    public void test_unmodifiableSet() throws Exception {
        Set collection = new HashSet();
        Bean1 bean = new Bean1();
        bean.items = Collections.unmodifiableSet(collection);

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
    }

    @Test
    public void test_unmodifiableSortedSet() throws Exception {
        SortedSet collection = new TreeSet();
        collection.add("A");

        Bean1 bean = new Bean1();
        bean.items = Collections.unmodifiableSortedSet(collection);

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
    }

    @Test
    public void test_unmodifiableNavigableSet() throws Exception {
        TreeSet collection = new TreeSet();
        collection.add("A");
        Bean1 bean = new Bean1();
        bean.items = Collections.unmodifiableNavigableSet(collection);

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
    }

    @Test
    public void test_emptySet() throws Exception {
        Bean1 bean = new Bean1();
        bean.items = Collections.emptySet();

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
    }

    @Test
    public void test_singletonSet() throws Exception {
        Bean1 bean = new Bean1();
        bean.items = Collections.singleton("1");

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
    }

    @Test
    public void test_subList() throws Exception {
        ArrayList list = new ArrayList();
        list.add(0);
        list.add(1);
        list.add(2);
        Bean1 bean = new Bean1();
        bean.items = list.subList(0, 1);

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

        assertSame(bean.items.size(), bean2.items.size());
        assertSame(bean.items.stream().findFirst().get(), bean2.items.stream().findFirst().get());
    }

    @Test
    public void test_arrayList() throws Exception {
        Bean1 bean = new Bean1();
        bean.items = Arrays.asList(0, 1, 2, 3);

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
        assertSame(bean.items.stream().findFirst().get(), bean2.items.stream().findFirst().get());
    }

    public static class Bean1 {
        Collection items;
    }
}
