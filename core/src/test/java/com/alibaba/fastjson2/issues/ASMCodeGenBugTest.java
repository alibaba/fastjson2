package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ASM bytecode generation bugs fixed in ObjectWriterCreatorASM and ObjectReaderCreatorASM.
 */
public class ASMCodeGenBugTest {
    // ========== BUG-7: hash32 collision in genMethodGetFieldReaderLCase ==========
    // When >6 fields exist and two LCase field name hashes collide at 32-bit level,
    // the second field was not found via getFieldReaderLCase (smart-match).
    // "fieldName4244" and "itemCount6627" have the same hash32 for their LCase hash64.

    public static class SmartMatchBean {
        public int id;
        public String value1;
        public String value2;
        public String value3;
        public String value4;
        public String value5;
        public String value6;
        public String fieldName4244; // hash32 collision pair
        public String itemCount6627; // hash32 collision pair
    }

    @Test
    public void testSmartMatchHash32Collision() {
        // Verify these two fields actually collide at hash32
        long h1 = Fnv.hashCode64LCase("fieldName4244");
        long h2 = Fnv.hashCode64LCase("itemCount6627");
        assertEquals((int) (h1 ^ (h1 >>> 32)), (int) (h2 ^ (h2 >>> 32)),
                "Test precondition: fieldName4244 and itemCount6627 must have same hash32 LCase");

        // Smart-match: use different casing in JSON to trigger getFieldReaderLCase
        String json = "{\"ID\":1,\"Value1\":\"a\",\"Value2\":\"b\",\"Value3\":\"c\","
                + "\"Value4\":\"d\",\"Value5\":\"e\",\"Value6\":\"f\","
                + "\"FIELDNAME4244\":\"collision1\",\"ITEMCOUNT6627\":\"collision2\"}";

        SmartMatchBean bean = JSON.parseObject(json, SmartMatchBean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(1, bean.id);
        assertEquals("collision1", bean.fieldName4244);
        assertEquals("collision2", bean.itemCount6627);
    }

    @Test
    public void testSmartMatchHash32CollisionUTF8() {
        String json = "{\"ID\":1,\"Value1\":\"a\",\"Value2\":\"b\",\"Value3\":\"c\","
                + "\"Value4\":\"d\",\"Value5\":\"e\",\"Value6\":\"f\","
                + "\"FIELDNAME4244\":\"collision1\",\"ITEMCOUNT6627\":\"collision2\"}";

        SmartMatchBean bean = JSON.parseObject(json.getBytes(), SmartMatchBean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(1, bean.id);
        assertEquals("collision1", bean.fieldName4244);
        assertEquals("collision2", bean.itemCount6627);
    }

    // ========== BUG-1: wrong method descriptor in gwFloat JSONB path ==========
    // Used "(D)V" (double descriptor) instead of "(F)V" for writeFloat in JSONB.

    public static class FloatBean {
        public float value;
    }

    @Test
    public void testFloatFieldJSONB() {
        FloatBean bean = new FloatBean();
        bean.value = 3.14f;
        byte[] jsonb = JSONB.toBytes(bean);
        FloatBean parsed = JSONB.parseObject(jsonb, FloatBean.class);
        assertEquals(3.14f, parsed.value);
    }

    @Test
    public void testFloatFieldJSONBWithReferenceDetect() {
        FloatBean bean = new FloatBean();
        bean.value = 2.718f;
        byte[] jsonb = JSONB.toBytes(bean, JSONWriter.Feature.ReferenceDetection);
        FloatBean parsed = JSONB.parseObject(jsonb, FloatBean.class);
        assertEquals(2.718f, parsed.value);
    }

    // ========== BUG-2: Float.class used as itemClass for Double[] ==========

    public static class DoubleArrayBean {
        public Double[] values;
    }

    @Test
    public void testDoubleArraySerialization() {
        DoubleArrayBean bean = new DoubleArrayBean();
        bean.values = new Double[]{1.1, 2.2, 3.3};
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("1.1"));
        assertTrue(json.contains("2.2"));
        assertTrue(json.contains("3.3"));

        DoubleArrayBean parsed = JSON.parseObject(json, DoubleArrayBean.class);
        assertArrayEquals(bean.values, parsed.values);
    }

    @Test
    public void testDoubleArrayJSONB() {
        DoubleArrayBean bean = new DoubleArrayBean();
        bean.values = new Double[]{1.1, 2.2, 3.3};
        byte[] jsonb = JSONB.toBytes(bean);
        DoubleArrayBean parsed = JSONB.parseObject(jsonb, DoubleArrayBean.class);
        assertArrayEquals(bean.values, parsed.values);
    }

    // ========== BUG-4/6: popPath0 leak with NotWriteEmptyArray and ReferenceDetection ==========
    // When ReferenceDetection is enabled and the list/collection is empty,
    // NotWriteEmptyArray exit path bypassed popPath0, leaking a reference stack entry.

    public static class RefDetectBean {
        public String name;
        public List<String> emptyList;
        public List<String> normalList;
    }

    @Test
    public void testNotWriteEmptyArrayWithReferenceDetection() {
        RefDetectBean bean = new RefDetectBean();
        bean.name = "test";
        bean.emptyList = new ArrayList<>();
        bean.normalList = new ArrayList<>();
        bean.normalList.add("item1");

        // This combination previously caused popPath0 leak
        String json = JSON.toJSONString(bean,
                JSONWriter.Feature.NotWriteEmptyArray,
                JSONWriter.Feature.ReferenceDetection);

        assertFalse(json.contains("emptyList"), "empty list should be omitted");
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("item1"));

        // Verify it round-trips correctly
        RefDetectBean parsed = JSON.parseObject(json, RefDetectBean.class);
        assertEquals("test", parsed.name);
        assertNull(parsed.emptyList);
        assertEquals(1, parsed.normalList.size());
    }

    @Test
    public void testNotWriteEmptyArrayWithReferenceDetectionJSONB() {
        RefDetectBean bean = new RefDetectBean();
        bean.name = "test";
        bean.emptyList = new ArrayList<>();
        bean.normalList = new ArrayList<>(Collections.singletonList("item1"));

        byte[] jsonb = JSONB.toBytes(bean,
                JSONWriter.Feature.NotWriteEmptyArray,
                JSONWriter.Feature.ReferenceDetection);

        RefDetectBean parsed = JSONB.parseObject(jsonb, RefDetectBean.class);
        assertEquals("test", parsed.name);
        assertEquals(1, parsed.normalList.size());
        assertEquals("item1", parsed.normalList.get(0));
    }

    public static class RefDetectObjectBean {
        public String name;
        public List<String> emptyCollection;
        public Object nextObj;
    }

    @Test
    public void testNotWriteEmptyCollectionObjectWithReferenceDetection() {
        // BUG-4: popPath0 leak in gwFieldValueObject for empty collections
        RefDetectObjectBean bean = new RefDetectObjectBean();
        bean.name = "test";
        bean.emptyCollection = new ArrayList<>();
        bean.nextObj = "afterEmpty";

        String json = JSON.toJSONString(bean,
                JSONWriter.Feature.NotWriteEmptyArray,
                JSONWriter.Feature.ReferenceDetection);

        assertFalse(json.contains("emptyCollection"));
        assertTrue(json.contains("\"nextObj\":\"afterEmpty\""));
    }
}
