package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest20 {
    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();
        bean.values = new LinkedList<>();
        bean.values.add(XItem.A);
        bean.values.add(XItem.B);
        bean.values.add(XItem.C);
        bean.values.add(XItem.D);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
        assertEquals(bean.values.get(1).getClass(), bean2.values.get(1).getClass());
        assertEquals(bean.values.get(2).getClass(), bean2.values.get(2).getClass());
        assertEquals(bean.values.get(3).getClass(), bean2.values.get(3).getClass());
    }

    public static class Bean1 {
        public List<Item> values;
    }

    public static interface Item {
    }

    public static enum XItem implements Item {
        A, B, C, D
    }

    @Test
    public void test_2() throws Exception {
        Bean2 bean = new Bean2();
        bean.values = new LinkedHashSet<>();
        bean.values.add(XItem.A);
        bean.values.add(XItem.B);
        bean.values.add(XItem.C);
        bean.values.add(XItem.D);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());

        Object[] array = bean.values.toArray();
        Object[] array2 = bean2.values.toArray();

        assertEquals(array[0].getClass(), array2[0].getClass());
        assertEquals(array[1].getClass(), array2[1].getClass());
        assertEquals(array[2].getClass(), array2[2].getClass());
        assertEquals(array[3].getClass(), array2[3].getClass());
    }

    @Test
    public void test_2_name() throws Exception {
        Bean2 bean = new Bean2();
        bean.values = new LinkedHashSet<>();
        bean.values.add(XItem.A);
        bean.values.add(XItem.B);
        bean.values.add(XItem.C);
        bean.values.add(XItem.D);

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.WriteEnumsUsingName,
                JSONWriter.Feature.ReferenceDetection
        );

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());

        Object[] array = bean.values.toArray();
        Object[] array2 = bean2.values.toArray();

        assertEquals(array[0].getClass(), array2[0].getClass());
        assertEquals(array[1].getClass(), array2[1].getClass());
        assertEquals(array[2].getClass(), array2[2].getClass());
        assertEquals(array[3].getClass(), array2[3].getClass());
    }

    public static class Bean2 {
        public Set<Item> values;
    }

    @Test
    public void test_3() throws Exception {
        Bean3 bean = new Bean3();
        bean.values = new HashSet();
        bean.values.add(XItem.A);
        bean.values.add(XItem.B);
        bean.values.add(XItem.C);
        bean.values.add(XItem.D);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean3 bean2 = (Bean3) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());

        Object[] array = bean.values.toArray();
        Object[] array2 = bean2.values.toArray();

        assertEquals(array[0].getClass(), array2[0].getClass());
        assertEquals(array[1].getClass(), array2[1].getClass());
        assertEquals(array[2].getClass(), array2[2].getClass());
        assertEquals(array[3].getClass(), array2[3].getClass());
    }

    public static class Bean3<T extends Item> {
        public Set<T> values;
    }
}
