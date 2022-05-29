package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest28_Short {
    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();
        bean.value = (short) 1;

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
        assertNotNull(bean2.value);

        assertSame(bean.value.getClass(), bean2.value.getClass());
        assertSame(bean.value, bean2.value);
    }

    public static class Bean1 {
        Short value;
    }

    @Test
    public void test_number_1() throws Exception {
        Bean2 bean = new Bean2();
        bean.value = (short) 1;

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue
        );

        JSONBDump.dump(bytes);

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.value);

        assertSame(bean.value.getClass(), bean2.value.getClass());
        assertSame(bean.value, bean2.value);
    }

    public static class Bean2 {
        Number value;
    }

    @Test
    public void test_number_2() throws Exception {
        Bean3 bean = new Bean3();
        bean.value = (short) 1;

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue
        );

        JSONBDump.dump(bytes);

        Bean3 bean2 = (Bean3) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.value);

        assertSame(bean.value.getClass(), bean2.value.getClass());
        assertSame(bean.value, bean2.value);
    }

    public static class Bean3 {
        Object value;
    }
}
