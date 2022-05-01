package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Differ;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.jupiter.api.Assertions.*;

public class AutoTypeTest14 {
    @Test
    public void test_0() throws Exception {
        Bean bean = new Bean();

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNull(bean2.values);

        Differ.diff(bean, bean2);
    }

    public static class Bean {
        public List<String> values;
    }

    @Test
    public void test_1() throws Exception {
        Object[] array = new Object[]{
                new byte[0], new short[0], new int[0], new long[0], new float[0], new double[0], new boolean[0], new char[0],
                new Byte[0], new Short[0], new Integer[0], new Long[0], new Float[0], new Double[0], new Boolean[0], new Character[0]
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(array2);
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i].getClass(), array2[i].getClass());
        }
    }

    @Test
    public void test_2() throws Exception {
        Object[] array = new Object[]{
                (byte) 1, (short) 1, 1, 1L, 1F, 1D, true, 'A', BigInteger.ONE, BigDecimal.ONE
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(array2);
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i].getClass(), array2[i].getClass());
        }
    }

    @Test
    public void test_2_num() throws Exception {
        Object[] array = new Object[]{
                (byte) 1, (short) 1, 1, 1L, 1F, 1.1F, 1D, 1.2D, 5D, BigInteger.ONE, BigDecimal.ONE
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Number[] array2 = (Number[]) JSONB.parseObject(bytes, Number[].class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(array2);
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i].getClass(), array2[i].getClass());
        }
    }

    @Test
    public void test_3() throws Exception {
        Object[] array = new Object[]{
                new byte[0][], new short[0][], new int[0][], new long[0][], new float[0][], new double[0][], new boolean[0][], new char[0][],
                new Byte[0][], new Short[0][], new Integer[0][], new Long[0][], new Float[0][], new Double[0][], new Boolean[0][], new Character[0][]
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(array2);
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i].getClass(), array2[i].getClass());
        }
    }

    @Test
    public void test_4() throws Exception {
        Object[] array = new Object[]{
                new HashMap<>(), new LinkedHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentSkipListMap<>(), new JSONObject(), new com.alibaba.fastjson.JSONObject()
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased, JSONReader.Feature.UseNativeObject);
        assertNotNull(array2);
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i].getClass(), array2[i].getClass());
        }
    }

    @Test
    public void test_4_1() throws Exception {
        Object[] array = new Object[]{
                new HashMap<>(), new LinkedHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentSkipListMap<>(), new JSONObject(), new com.alibaba.fastjson.JSONObject()
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.NotWriteHashMapArrayListClassName);

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased, JSONReader.Feature.UseNativeObject);
        assertNotNull(array2);
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i].getClass(), array2[i].getClass());
        }
    }

    @Test
    public void test_5() throws Exception {
        Object[] array = new Object[]{
                new ArrayList<>(), new LinkedList<>(), new Vector<>(), new HashSet<>(), new TreeSet<>(), new JSONArray(), new com.alibaba.fastjson.JSONArray()
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased, JSONReader.Feature.UseNativeObject);
        assertNotNull(array2);
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i].getClass(), array2[i].getClass());
        }
    }

    @Test
    public void test_5_1() throws Exception {
        Object[] array = new Object[]{
                new ArrayList<>(), new LinkedList<>(), new Vector<>(), new HashSet<>(), new TreeSet<>(), new JSONArray(), new com.alibaba.fastjson.JSONArray()
        };

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.NotWriteHashMapArrayListClassName);

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased, JSONReader.Feature.UseNativeObject);
        assertNotNull(array2);
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i].getClass(), array2[i].getClass());
        }
    }
}
