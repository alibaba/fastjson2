package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Dubbo12209 {
    static final JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ErrorOnNoneSerializable,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    static final JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.ErrorOnNoneSerializable,
            JSONReader.Feature.IgnoreAutoTypeNotMatch,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };

    @Test
    public void testByteArray() {
        Byte[] array = {123};
        Byte[] values = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), Byte[].class, readerFeatures);
        assertArrayEquals(array, values);
    }

    @Test
    public void testByteValueArray() {
        byte[] array = {123};
        byte[] values = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), byte[].class, readerFeatures);
        assertArrayEquals(array, values);
    }

    @Test
    public void testByteValueArray1() {
        Byte[] shorts = {123};
        byte[] array = JSONB.parseObject(JSONB.toBytes(shorts, writerFeatures), byte[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals((byte) 123, array[0]);
    }

    @Test
    public void testShortArray() {
        Short[] array = {123};
        Short[] values = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), Short[].class, readerFeatures);
        assertArrayEquals(array, values);
    }

    @Test
    public void testShortArray1() {
        short[] shorts = {123};
        Short[] array = JSONB.parseObject(JSONB.toBytes(shorts, writerFeatures), Short[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals((short) 123, array[0]);
    }

    @Test
    public void testShortValueArray() {
        short[] array = {123};
        short[] longs = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), short[].class, readerFeatures);
        assertArrayEquals(array, longs);
    }

    @Test
    public void testShortValueArray1() {
        Short[] shorts = {123};
        short[] array = JSONB.parseObject(JSONB.toBytes(shorts, writerFeatures), short[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals((short) 123, array[0]);
    }

    @Test
    public void testIntegerArray() {
        Integer[] array = {123};
        Integer[] longs = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), Integer[].class, readerFeatures);
        assertArrayEquals(array, longs);
    }

    @Test
    public void testIntegerArray1() {
        Integer[] array = JSONB.parseObject(
                JSONB.toBytes(new int[]{123}, writerFeatures), Integer[].class, readerFeatures
        );

        assertEquals(1, array.length);
        assertEquals(123, array[0]);
    }

    @Test
    public void testIntegerValueArray() {
        int[] array = JSONB.parseObject(JSONB.toBytes(new int[]{123}, writerFeatures), int[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals(123, array[0]);
    }

    @Test
    public void testIntegerValueArray1() {
        int[] array = JSONB.parseObject(JSONB.toBytes(new Integer[]{123}, writerFeatures), int[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals(123, array[0]);
    }

    @Test
    public void testLongArray() {
        Long[] array = {123L};
        Long[] longs = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), Long[].class, readerFeatures);
        assertArrayEquals(array, longs);
    }

    @Test
    public void testLongArray1() {
        Long[] longs = JSONB.parseObject(
                JSONB.toBytes(new long[]{123L}, writerFeatures), Long[].class, readerFeatures
        );

        assertEquals(1, longs.length);
        assertEquals(123L, longs[0]);
    }

    @Test
    public void testLongArray2() {
        Long[] longs = JSONB.parseObject(
                JSONB.toBytes(new int[]{123}, writerFeatures), Long[].class, readerFeatures
        );
        assertEquals(1, longs.length);
        assertEquals(123L, longs[0]);
    }

    @Test
    public void testLongArray3() {
        Long[] longs = JSONB.parseObject(
                JSONB.toBytes(new Integer[]{123}, writerFeatures), Long[].class, readerFeatures
        );
        assertEquals(1, longs.length);
        assertEquals(123L, longs[0]);
    }

    @Test
    public void testLongValueArray() {
        long[] array = JSONB.parseObject(JSONB.toBytes(new long[]{123}, writerFeatures), long[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals(123, array[0]);
    }

    @Test
    public void testLongValueArray1() {
        long[] array = JSONB.parseObject(JSONB.toBytes(new Long[]{123L}, writerFeatures), long[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals(123, array[0]);
    }

    @Test
    public void testLongValueArray2() {
        long[] array = JSONB.parseObject(JSONB.toBytes(new Integer[]{123}, writerFeatures), long[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals(123, array[0]);
    }

    @Test
    public void testLongValueArray3() {
        long[] array = JSONB.parseObject(JSONB.toBytes(new int[]{123}, writerFeatures), long[].class, readerFeatures);
        assertEquals(1, array.length);
        assertEquals(123, array[0]);
    }

    @Test
    public void testFloatArray() {
        Float[] array = {123F};
        Float[] values = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), Float[].class, readerFeatures);
        assertArrayEquals(array, values);
    }

    @Test
    public void testFloatValueArray() {
        float[] array = {123F};
        float[] values = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), float[].class, readerFeatures);
        assertArrayEquals(array, values);
    }

    @Test
    public void testDoubleArray() {
        Double[] array = {123D};
        Double[] values = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), Double[].class, readerFeatures);
        assertArrayEquals(array, values);
    }

    @Test
    public void testDoubleValueArray() {
        double[] array = {123D};
        double[] values = JSONB.parseObject(JSONB.toBytes(array, writerFeatures), double[].class, readerFeatures);
        assertArrayEquals(array, values);
    }

    @Test
    public void testMap() {
        HashMap obj = new HashMap<String, Object>() {
            {
                put("a", "a");
            }
        };
        Map parsed = JSONB.parseObject(JSONB.toBytes(obj, writerFeatures), HashMap.class, readerFeatures);
        assertEquals(obj.size(), parsed.size());
        assertEquals(obj.get("a"), parsed.get("a"));
    }
}
