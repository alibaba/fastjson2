package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static org.junit.jupiter.api.Assertions.*;

public class PrettyFormatShortPrimitiveArrayTest {
    // ========== int[] ==========
    @Test
    public void testIntArrayShortInline() {
        int[] arr = {1, 2, 3};
        assertEquals("[1,2,3]", JSON.toJSONString(arr, PrettyFormat));
        assertEquals("[1,2,3]", JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void testIntArrayBoundaryInline() {
        int[] arr = {1, 2, 3, 4};
        assertEquals("[1,2,3,4]", JSON.toJSONString(arr, PrettyFormat));
        assertEquals("[1,2,3,4]", JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void testIntArrayLongMultiline() {
        int[] arr = {1, 2, 3, 4, 5};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertEquals("[\n\t1,\n\t2,\n\t3,\n\t4,\n\t5\n]", result);
        String resultUtf8 = JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii);
        assertEquals("[\n\t1,\n\t2,\n\t3,\n\t4,\n\t5\n]", resultUtf8);
    }

    @Test
    public void testIntArrayEmpty() {
        int[] arr = {};
        assertEquals("[]", JSON.toJSONString(arr, PrettyFormat));
        assertEquals("[]", JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void testIntArraySingleElement() {
        int[] arr = {42};
        assertEquals("[42]", JSON.toJSONString(arr, PrettyFormat));
        assertEquals("[42]", JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii));
    }

    // ========== byte[] ==========
    @Test
    public void testByteArrayShortInline() {
        byte[] arr = {1, 2, 3};
        assertEquals("[1,2,3]", JSON.toJSONString(arr, PrettyFormat));
        assertEquals("[1,2,3]", JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void testByteArrayLongMultiline() {
        byte[] arr = {1, 2, 3, 4, 5};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertEquals("[\n\t1,\n\t2,\n\t3,\n\t4,\n\t5\n]", result);
        String resultUtf8 = JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii);
        assertEquals("[\n\t1,\n\t2,\n\t3,\n\t4,\n\t5\n]", resultUtf8);
    }

    // ========== short[] ==========
    @Test
    public void testShortArrayShortInline() {
        short[] arr = {10, 20, 30};
        assertEquals("[10,20,30]", JSON.toJSONString(arr, PrettyFormat));
        assertEquals("[10,20,30]", JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void testShortArrayLongMultiline() {
        short[] arr = {10, 20, 30, 40, 50};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertEquals("[\n\t10,\n\t20,\n\t30,\n\t40,\n\t50\n]", result);
    }

    // ========== long[] ==========
    @Test
    public void testLongArrayShortInline() {
        long[] arr = {100L, 200L, 300L};
        assertEquals("[100,200,300]", JSON.toJSONString(arr, PrettyFormat));
        assertEquals("[100,200,300]", JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void testLongArrayLongMultiline() {
        long[] arr = {100L, 200L, 300L, 400L, 500L};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertEquals("[\n\t100,\n\t200,\n\t300,\n\t400,\n\t500\n]", result);
        String resultUtf8 = JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii);
        assertEquals("[\n\t100,\n\t200,\n\t300,\n\t400,\n\t500\n]", resultUtf8);
    }

    // ========== float[] ==========
    @Test
    public void testFloatArrayShortInline() {
        float[] arr = {1.0f, 2.5f, 3.0f};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertFalse(result.contains("\n"));
        String resultUtf8 = JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii);
        assertFalse(resultUtf8.contains("\n"));
    }

    @Test
    public void testFloatArrayLongMultiline() {
        float[] arr = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertTrue(result.contains("\n"));
        String resultUtf8 = JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii);
        assertTrue(resultUtf8.contains("\n"));
    }

    // ========== double[] ==========
    @Test
    public void testDoubleArrayShortInline() {
        double[] arr = {1.1, 2.2, 3.3};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertFalse(result.contains("\n"));
        String resultUtf8 = JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii);
        assertFalse(resultUtf8.contains("\n"));
    }

    @Test
    public void testDoubleArrayLongMultiline() {
        double[] arr = {1.1, 2.2, 3.3, 4.4, 5.5};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertTrue(result.contains("\n"));
        String resultUtf8 = JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii);
        assertTrue(resultUtf8.contains("\n"));
    }

    // ========== boolean[] ==========
    @Test
    public void testBooleanArrayShortInline() {
        boolean[] arr = {true, false, true};
        assertEquals("[true,false,true]", JSON.toJSONString(arr, PrettyFormat));
        assertEquals("[true,false,true]", JSON.toJSONString(arr, PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void testBooleanArrayLongMultiline() {
        boolean[] arr = {true, false, true, false, true};
        String result = JSON.toJSONString(arr, PrettyFormat);
        assertEquals("[\n\ttrue,\n\tfalse,\n\ttrue,\n\tfalse,\n\ttrue\n]", result);
    }

    // ========== Arrays inside objects ==========
    @Test
    public void testIntArrayInObjectShort() {
        JSONObject obj = new JSONObject();
        obj.put("coords", new int[]{1, 2, 3});
        String result = JSON.toJSONString(obj, PrettyFormat);
        assertEquals("{\n\t\"coords\":[1,2,3]\n}", result);
    }

    @Test
    public void testIntArrayInObjectLong() {
        JSONObject obj = new JSONObject();
        obj.put("data", new int[]{1, 2, 3, 4, 5});
        String result = JSON.toJSONString(obj, PrettyFormat);
        assertTrue(result.contains("\"data\":[\n"));
    }

    @Test
    public void testDoubleArrayInObjectShort() {
        JSONObject obj = new JSONObject();
        obj.put("pos", new double[]{1.0, 2.0});
        String result = JSON.toJSONString(obj, PrettyFormat);
        assertFalse(result.contains("[\n"));
    }

    // ========== PrettyFormatInlineArrays interaction ==========
    @Test
    public void testPrettyFormatInlineArraysOverridesLength() {
        // When PrettyFormatInlineArrays is enabled, ALL arrays stay inline regardless of length
        int[] longArr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        JSONWriter.Context context = new JSONWriter.Context(PrettyFormat);
        context.setPrettyFormatInlineArrays(true);
        String result = JSON.toJSONString(longArr, context);
        assertEquals("[1,2,3,4,5,6,7,8,9,10]", result);
    }

    @Test
    public void testPrettyFormatInlineArraysOverridesLengthUTF8() {
        int[] longArr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        JSONWriter.Context context = new JSONWriter.Context(PrettyFormat, OptimizedForAscii);
        context.setPrettyFormatInlineArrays(true);
        String result = JSON.toJSONString(longArr, context);
        assertEquals("[1,2,3,4,5,6,7,8,9,10]", result);
    }

    // ========== Pretty format variants ==========
    @Test
    public void testIntArrayMultilineWith2Space() {
        int[] arr = {1, 2, 3, 4, 5};
        String result = JSON.toJSONString(arr, PrettyFormatWith2Space);
        assertEquals("[\n  1,\n  2,\n  3,\n  4,\n  5\n]", result);
    }

    @Test
    public void testIntArrayMultilineWith4Space() {
        int[] arr = {1, 2, 3, 4, 5};
        String result = JSON.toJSONString(arr, PrettyFormatWith4Space);
        assertEquals("[\n    1,\n    2,\n    3,\n    4,\n    5\n]", result);
    }

    @Test
    public void testIntArrayInlineWith2Space() {
        int[] arr = {1, 2, 3};
        assertEquals("[1,2,3]", JSON.toJSONString(arr, PrettyFormatWith2Space));
    }

    @Test
    public void testIntArrayInlineWith4Space() {
        int[] arr = {1, 2, 3};
        assertEquals("[1,2,3]", JSON.toJSONString(arr, PrettyFormatWith4Space));
    }

    // ========== Non-pretty mode unchanged ==========
    @Test
    public void testNonPrettyUnchanged() {
        int[] arr = {1, 2, 3};
        assertEquals("[1,2,3]", JSON.toJSONString(arr));
        int[] longArr = {1, 2, 3, 4, 5};
        assertEquals("[1,2,3,4,5]", JSON.toJSONString(longArr));
    }

    // ========== Bean with primitive array fields ==========
    @Test
    public void testBeanWithIntArray() {
        BeanWithIntArray bean = new BeanWithIntArray();
        bean.values = new int[]{1, 2, 3};
        String result = JSON.toJSONString(bean, PrettyFormat);
        assertTrue(result.contains("\"values\":[1,2,3]"));
    }

    @Test
    public void testBeanWithLongIntArray() {
        BeanWithIntArray bean = new BeanWithIntArray();
        bean.values = new int[]{1, 2, 3, 4, 5};
        String result = JSON.toJSONString(bean, PrettyFormat);
        assertTrue(result.contains("\"values\":[\n"));
    }

    public static class BeanWithIntArray {
        public int[] values;
    }

    @Test
    public void testBeanWithBooleanArray() {
        BeanWithBooleanArray bean = new BeanWithBooleanArray();
        bean.flags = new boolean[]{true, false};
        String result = JSON.toJSONString(bean, PrettyFormat);
        assertTrue(result.contains("\"flags\":[true,false]"));
    }

    public static class BeanWithBooleanArray {
        public boolean[] flags;
    }

    @Test
    public void testBeanWithDoubleArray() {
        BeanWithDoubleArray bean = new BeanWithDoubleArray();
        bean.values = new double[]{1.1, 2.2};
        String result = JSON.toJSONString(bean, PrettyFormat);
        assertFalse(result.substring(result.indexOf("[")).startsWith("[\n"));
    }

    public static class BeanWithDoubleArray {
        public double[] values;
    }
}
