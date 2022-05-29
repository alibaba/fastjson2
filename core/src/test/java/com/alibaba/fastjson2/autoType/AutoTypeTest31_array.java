package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest31_array {
    @Test
    public void test_1() throws Exception {
        String[] array = new String[]{"101"};

        byte[] bytes = JSONB.toBytes(array,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        String[] array2 = (String[]) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(array2);
        assertNotNull(array2[0]);

        assertEquals(array[0], array2[0]);
    }

    @Test
    public void test_LongArray() throws Exception {
        Long[] array = new Long[]{101L};

        byte[] bytes = JSONB.toBytes(array,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Long[] array2 = (Long[]) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(array2);
        assertNotNull(array2[0]);

        assertEquals(array[0], array2[0]);
    }

    @Test
    public void test_LongValueArray() throws Exception {
        long[] array = new long[]{101L};

        byte[] bytes = JSONB.toBytes(array,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        long[] array2 = (long[]) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(array2);
        assertNotNull(array2[0]);

        assertEquals(array[0], array2[0]);
    }

    @Test
    public void test_2() throws Exception {
        BigDecimal[] array = new BigDecimal[]{new BigDecimal("101")};

        byte[] bytes = JSONB.toBytes(array,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        BigDecimal[] array2 = (BigDecimal[]) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(array2);
        assertNotNull(array2[0]);

        assertEquals(array[0], array2[0]);
    }

    @Test
    public void test_bigint() throws Exception {
        BigInteger[] array = new BigInteger[]{new BigInteger("101")};

        byte[] bytes = JSONB.toBytes(array,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        BigInteger[] array2 = (BigInteger[]) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(array2);
        assertNotNull(array2[0]);

        assertEquals(array[0], array2[0]);
    }

    @Test
    public void test_uuid() throws Exception {
        UUID[] array = new UUID[]{UUID.randomUUID()};

        byte[] bytes = JSONB.toBytes(array,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        UUID[] array2 = (UUID[]) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(array2);
        assertNotNull(array2[0]);

        assertEquals(array[0], array2[0]);
    }

    @Test
    public void test_FieldLongArray() {
        BeanLong64Array bean = new BeanLong64Array();
        bean.values = new Long[]{101L};

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        BeanLong64Array bean2 = (BeanLong64Array) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(bean2);
        assertNotNull(bean2.values);

        assertEquals(bean.values[0], bean2.values[0]);
    }

    public static class BeanLong64Array {
        public Long[] values;
    }

    @Test
    public void test_FieldIntegerArray() {
        BeanInteger64Array bean = new BeanInteger64Array();
        bean.values = new Integer[]{101};

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        BeanInteger64Array bean2 = (BeanInteger64Array) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(bean2);
        assertNotNull(bean2.values);

        assertEquals(bean.values[0], bean2.values[0]);
    }

    public static class BeanInteger64Array {
        public Integer[] values;
    }
}
