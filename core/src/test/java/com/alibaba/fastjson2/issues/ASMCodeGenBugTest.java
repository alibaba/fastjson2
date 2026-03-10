package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    // gwFloat is called for Float.class (boxed) fields. The JSONB path used "(D)V"
    // instead of "(F)V" for writeFloat, causing VerifyError at class load time.

    public static class BoxedFloatBean {
        public Float value;
    }

    @Test
    public void testBoxedFloatFieldJSONB() {
        BoxedFloatBean bean = new BoxedFloatBean();
        bean.value = 3.14f;
        byte[] jsonb = JSONB.toBytes(bean);
        BoxedFloatBean parsed = JSONB.parseObject(jsonb, BoxedFloatBean.class);
        assertEquals(3.14f, parsed.value);
    }

    @Test
    public void testBoxedFloatFieldJSONBMultipleValues() {
        // Test with edge values to exercise the JSONB float write path
        for (float val : new float[]{0f, -1f, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN}) {
            BoxedFloatBean bean = new BoxedFloatBean();
            bean.value = val;
            byte[] jsonb = JSONB.toBytes(bean);
            BoxedFloatBean parsed = JSONB.parseObject(jsonb, BoxedFloatBean.class);
            assertEquals(val, parsed.value);
        }
    }

    // ========== BUG-2: Float.class used as itemClass for Double[] ==========
    // FieldWriterObjectArray was created with Float.class instead of Double.class
    // as itemClass for Double[] fields, causing wrong type info in JSONB.

    public static class DoubleArrayBean {
        public Double[] values;
    }

    @Test
    public void testDoubleArrayJSONBRoundTrip() {
        DoubleArrayBean bean = new DoubleArrayBean();
        bean.values = new Double[]{1.1, 2.2, 3.3};
        byte[] jsonb = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        DoubleArrayBean parsed = JSONB.parseObject(jsonb, DoubleArrayBean.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(parsed.values);
        assertArrayEquals(bean.values, parsed.values);
    }

    @Test
    public void testDoubleArrayJSONBWithNulls() {
        DoubleArrayBean bean = new DoubleArrayBean();
        bean.values = new Double[]{1.1, null, 3.3};
        byte[] jsonb = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        DoubleArrayBean parsed = JSONB.parseObject(jsonb, DoubleArrayBean.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(parsed.values);
        assertEquals(1.1, parsed.values[0]);
        assertNull(parsed.values[1]);
        assertEquals(3.3, parsed.values[2]);
    }

    // ========== BUG-4/6: popPath0 leak with NotWriteEmptyArray + ReferenceDetection ==========
    // When ReferenceDetection is enabled and NotWriteEmptyArray causes an empty list to be
    // skipped, popPath0 was not called, corrupting the path stack. This causes subsequent
    // shared object references to use wrong $ref paths.

    public static class RefLeakBean {
        public List<String> emptyItems;
        public List<String> list1;
        public List<String> list2; // same instance as list1
    }

    @Test
    public void testPopPath0LeakInGwFieldValueList() {
        // The key: emptyItems leaks path, then list1/list2 share the same instance.
        // With the bug, the $ref for list2 would point to "$.emptyItems.list1" (wrong path).
        // With the fix, the $ref for list2 points to "$.list1" (correct path).
        RefLeakBean bean = new RefLeakBean();
        bean.emptyItems = new ArrayList<>(); // empty, triggers NotWriteEmptyArray skip
        List<String> shared = new ArrayList<>(Arrays.asList("a", "b"));
        bean.list1 = shared;
        bean.list2 = shared; // same reference

        String json = JSON.toJSONString(bean,
                JSONWriter.Feature.NotWriteEmptyArray,
                JSONWriter.Feature.ReferenceDetection);

        // emptyItems should be omitted
        assertFalse(json.contains("emptyItems"));
        // list2 should reference list1 via correct path $.list1
        assertTrue(json.contains("\"list1\""), "list1 should be present");

        // The $ref must point to $.list1, not $.emptyItems.list1
        assertFalse(json.contains("$.emptyItems"), "$ref must not contain leaked path prefix");

        // Round-trip: deserialize and verify list2 has same content
        RefLeakBean parsed = JSON.parseObject(json, RefLeakBean.class);
        assertNotNull(parsed.list1);
        assertEquals(Arrays.asList("a", "b"), parsed.list1);
    }

    public static class RefLeakObjectBean {
        public Collection<String> emptyCollection; // Collection (not List) → goes through gwFieldValueObject
        public List<String> list1;
        public List<String> list2; // same instance as list1
    }

    @Test
    public void testPopPath0LeakInGwFieldValueObject() {
        // BUG-4: same pattern but through gwFieldValueObject path
        // Collection<String> (not List) routes through gwFieldValueObject which had
        // the separate popPath0 leak for empty collections.
        RefLeakObjectBean bean = new RefLeakObjectBean();
        bean.emptyCollection = new ArrayList<>(); // empty Collection
        List<String> shared = new ArrayList<>(Arrays.asList("x", "y"));
        bean.list1 = shared;
        bean.list2 = shared;

        String json = JSON.toJSONString(bean,
                JSONWriter.Feature.NotWriteEmptyArray,
                JSONWriter.Feature.ReferenceDetection);

        assertFalse(json.contains("emptyCollection"));
        assertFalse(json.contains("$.emptyCollection"), "$ref must not contain leaked path prefix");

        RefLeakObjectBean parsed = JSON.parseObject(json, RefLeakObjectBean.class);
        assertNotNull(parsed.list1);
        assertEquals(Arrays.asList("x", "y"), parsed.list1);
    }
}
