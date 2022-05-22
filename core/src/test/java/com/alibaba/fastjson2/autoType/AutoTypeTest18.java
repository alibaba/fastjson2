package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest18 {
    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();
        bean.values = new LinkedList<>();
        bean.values.add(new XItem());
        bean.values.add(new YItem());

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.values);
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get(0).getClass(), bean2.values.get(0).getClass());
        assertEquals(bean.values.get(1).getClass(), bean2.values.get(1).getClass());
    }

    public static class Bean1 {
        public List<Item> values;
    }

    public static interface Item {
    }

    public static class XItem
            implements Item {
        public XItem() {
        }
    }

    public static class YItem
            implements Item {
        public YItem() {
        }
    }

    public static enum ZEnum
            implements Item {
        A
    }

    @Test
    public void test_2() throws Exception {
        Bean2 bean = new Bean2();
        bean.values = new LinkedHashSet<>();
        bean.values.add(new XItem());
        bean.values.add(new YItem());
        bean.values.add(ZEnum.A);

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
    }

    public static class Bean2 {
        public Set<Item> values;
    }
}
