package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A field level {@code @JSONField(serializeFeatures = ...)} must be honoured by the ASM generated
 * ObjectWriter exactly as it is by the reflect one, in text JSON and in JSONB, with and without
 * filters. Before the fix the ASM generator only looked at the writer context, so:
 * <ul>
 *     <li>{@code WriteNulls} was ignored for primitive arrays and for enum fields</li>
 *     <li>{@code NotWriteDefaultValue} was ignored for primitive value fields</li>
 *     <li>the JSONB generator never looked at the field features at all</li>
 *     <li>the filtered write path pre-skipped null fields with the context features only</li>
 * </ul>
 * Both are compile time constants of the generated writer, so the two creators must agree.
 */
public class Issue7773 {
    public enum MyEnum {
        A, B
    }

    public static class ForcedNulls {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public int[] intArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public long[] longArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public boolean[] boolArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public short[] shortArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public byte[] byteArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public char[] charArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public double[] doubleArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public float[] floatArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public MyEnum enumField;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public String strField;
    }

    public static class PlainNulls {
        public int[] intArr;
        public long[] longArr;
        public boolean[] boolArr;
        public short[] shortArr;
        public byte[] byteArr;
        public char[] charArr;
        public double[] doubleArr;
        public float[] floatArr;
        public MyEnum enumField;
        public String strField;
    }

    /**
     * Fields annotated with {@code NullAsDefaultValue}. The reflect creator merges the field
     * features into the context (see {@code FieldWriterObject#write}), so a null array is written
     * as {@code []} while a null enum stays omitted; the ASM generator must produce the same.
     */
    public static class NullAsDefaults {
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        public int[] intArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        public long[] longArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        public byte[] byteArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        public MyEnum enumField;
    }

    public static class NullListsAsEmpty {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullListAsEmpty)
        public int[] intArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullListAsEmpty)
        public long[] longArr;
    }

    /**
     * {@code NullAsDefaultValue} on a {@code Date} is dropped by the reflect creator (the Date
     * null gate is {@code WriteNulls} only), so the ASM generator must drop it too; the
     * {@code WriteNulls} Date and the {@code NullAsDefaultValue} array must both be written.
     */
    public static class DatesAndArrays {
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        public Date dateField;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        public long[] longArr;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public Date dateField2;
    }

    /**
     * Under a context {@code NotWriteDefaultValue} the reflect creator still writes the
     * {@code WriteNulls} array and boxed number but drops the {@code WriteNulls} String; the ASM
     * generator must make the same per-type choice (both creators agree either way).
     */
    public static class BoxedUnderNotWriteDefaultValue {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public byte[] bin;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public Double dbl;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public String str;
    }

    public static class Defaults {
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public short duration;
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public byte b;
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public char c;
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public int count;
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public long size;
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public boolean flag;
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public float f;
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public double d;
        public int control = 7;
    }

    private static final String[] NULL_FIELDS = {
            "intArr", "longArr", "boolArr", "shortArr", "byteArr",
            "charArr", "doubleArr", "floatArr", "enumField", "strField"
    };

    private static final String[] DEFAULT_FIELDS = {
            "duration", "b", "c", "count", "size", "flag", "f", "d"
    };

    @Test
    public void fieldLevelWriteNullsOnPrimitiveArraysAndEnum() {
        JSONObject object = JSON.parseObject(JSON.toJSONString(new ForcedNulls()));
        for (String name : NULL_FIELDS) {
            assertTrue(object.containsKey(name), "missing field : " + name);
            assertNull(object.get(name), "unexpected value for field : " + name);
        }
        assertEquals(NULL_FIELDS.length, object.size());
    }

    @Test
    public void unannotatedNullsRemainOmitted() {
        assertEquals("{}", JSON.toJSONString(new PlainNulls()));
    }

    @Test
    public void contextWriteNullsStillWorks() {
        JSONObject object = JSON.parseObject(JSON.toJSONString(new PlainNulls(), JSONWriter.Feature.WriteNulls));
        for (String name : NULL_FIELDS) {
            assertTrue(object.containsKey(name), "missing field : " + name);
        }
        assertEquals(NULL_FIELDS.length, object.size());
    }

    @Test
    public void fieldLevelNotWriteDefaultValueOnPrimitives() {
        JSONObject object = JSON.parseObject(JSON.toJSONString(new Defaults()));
        for (String name : DEFAULT_FIELDS) {
            assertFalse(object.containsKey(name), "default value written for field : " + name);
        }
        assertEquals(1, object.size());
        assertEquals(7, object.getIntValue("control"));
    }

    @Test
    public void nonDefaultValuesAreStillWritten() {
        Defaults bean = new Defaults();
        bean.duration = 5;
        bean.b = 6;
        bean.c = 'x';
        bean.count = 3;
        bean.size = 4L;
        bean.flag = true;
        bean.f = 1.5F;
        bean.d = 2.5D;

        JSONObject object = JSON.parseObject(JSON.toJSONString(bean));
        assertEquals(5, object.getIntValue("duration"));
        assertEquals(6, object.getIntValue("b"));
        assertEquals("x", object.getString("c"));
        assertEquals(3, object.getIntValue("count"));
        assertEquals(4L, object.getLongValue("size"));
        assertTrue(object.getBooleanValue("flag"));
        assertEquals(1.5F, object.getFloatValue("f"));
        assertEquals(2.5D, object.getDoubleValue("d"));
        assertEquals(7, object.getIntValue("control"));
    }

    @Test
    public void asmAndReflectCreatorsAgree() {
        assertEquals(
                writeWith(ObjectWriterCreator.INSTANCE, new ForcedNulls()),
                writeWith(ObjectWriterCreatorASM.INSTANCE, new ForcedNulls()));
        assertEquals(
                writeWith(ObjectWriterCreator.INSTANCE, new Defaults()),
                writeWith(ObjectWriterCreatorASM.INSTANCE, new Defaults()));
        assertEquals(
                writeWith(ObjectWriterCreator.INSTANCE, new NullAsDefaults()),
                writeWith(ObjectWriterCreatorASM.INSTANCE, new NullAsDefaults()));
        assertEquals(
                writeWith(ObjectWriterCreator.INSTANCE, new NullListsAsEmpty()),
                writeWith(ObjectWriterCreatorASM.INSTANCE, new NullListsAsEmpty()));
        assertEquals(
                writeWith(ObjectWriterCreator.INSTANCE, new DatesAndArrays()),
                writeWith(ObjectWriterCreatorASM.INSTANCE, new DatesAndArrays()));
        assertEquals(
                writeWith(ObjectWriterCreator.INSTANCE, new BoxedUnderNotWriteDefaultValue(), JSONWriter.Feature.NotWriteDefaultValue),
                writeWith(ObjectWriterCreatorASM.INSTANCE, new BoxedUnderNotWriteDefaultValue(), JSONWriter.Feature.NotWriteDefaultValue));
    }

    @Test
    public void nullAsDefaultValueWritesEmptyArraysOnBothCreators() {
        // the reflect creator merges the field features into the context, so a null array is
        // written as [] and a null enum stays omitted; the ASM gate must agree field by field
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE, ObjectWriterCreatorASM.INSTANCE}) {
            JSONObject object = JSON.parseObject(writeWith(creator, new NullAsDefaults()));
            assertTrue(object.containsKey("intArr"), "intArr missing : " + creator);
            assertTrue(object.containsKey("longArr"), "longArr missing : " + creator);
            assertTrue(object.containsKey("byteArr"), "byteArr missing : " + creator);
            for (String name : new String[]{"intArr", "longArr", "byteArr"}) {
                assertEquals(0, ((java.util.List<?>) object.get(name)).size(), name + " not empty : " + creator);
            }
            assertFalse(object.containsKey("enumField"), "enumField written : " + creator);
            assertEquals(3, object.size(), creator.getClass().getSimpleName());
        }
    }

    @Test
    public void writeNullListAsEmptyWritesEmptyArraysOnBothCreators() {
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE, ObjectWriterCreatorASM.INSTANCE}) {
            JSONObject object = JSON.parseObject(writeWith(creator, new NullListsAsEmpty()));
            assertEquals(0, ((java.util.List<?>) object.get("intArr")).size());
            assertEquals(0, ((java.util.List<?>) object.get("longArr")).size());
            assertEquals(2, object.size(), creator.getClass().getSimpleName());
        }
    }

    @Test
    public void dateFieldNullGateStaysInSyncAcrossCreators() {
        // the Date null gate is WriteNulls only on both sides: the NullAsDefaultValue Date is
        // dropped, the WriteNulls Date is written, and the NullAsDefaultValue array is []
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE, ObjectWriterCreatorASM.INSTANCE}) {
            JSONObject object = JSON.parseObject(writeWith(creator, new DatesAndArrays()));
            assertFalse(object.containsKey("dateField"), "dateField written : " + creator);
            assertTrue(object.containsKey("dateField2"), "dateField2 missing : " + creator);
            assertNull(object.get("dateField2"));
            assertEquals(0, ((java.util.List<?>) object.get("longArr")).size());
            assertEquals(2, object.size(), creator.getClass().getSimpleName());
        }
    }

    @Test
    public void fieldLevelFeaturesWorkInJsonb() {
        // Defaults: the zero primitives are omitted in JSONB as they are in text JSON
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE, ObjectWriterCreatorASM.INSTANCE}) {
            JSONObject object = JSON.parseObject(jsonbWith(creator, new Defaults()));
            for (String name : DEFAULT_FIELDS) {
                assertFalse(object.containsKey(name), "default value written in JSONB : " + name);
            }
            assertEquals(7, object.getIntValue("control"));
        }

        // ForcedNulls: every annotated null field is present in JSONB as it is in text JSON
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE, ObjectWriterCreatorASM.INSTANCE}) {
            JSONObject object = JSON.parseObject(jsonbWith(creator, new ForcedNulls()));
            for (String name : NULL_FIELDS) {
                assertTrue(object.containsKey(name), "missing field in JSONB : " + name);
                assertNull(object.get(name), "unexpected value in JSONB for field : " + name);
            }
            assertEquals(NULL_FIELDS.length, object.size(), creator.getClass().getSimpleName());
        }

        // NullAsDefaults: the arrays are [] in JSONB, the enum stays omitted
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE, ObjectWriterCreatorASM.INSTANCE}) {
            JSONObject object = JSON.parseObject(jsonbWith(creator, new NullAsDefaults()));
            assertFalse(object.containsKey("enumField"), "enumField written in JSONB : " + creator);
            assertEquals(3, object.size(), creator.getClass().getSimpleName());
            assertEquals(0, ((java.util.List<?>) object.get("intArr")).size());
        }
    }

    @Test
    public void jsonbCreatorsAgree() {
        assertEquals(
                jsonbWith(ObjectWriterCreator.INSTANCE, new ForcedNulls()),
                jsonbWith(ObjectWriterCreatorASM.INSTANCE, new ForcedNulls()));
        assertEquals(
                jsonbWith(ObjectWriterCreator.INSTANCE, new Defaults()),
                jsonbWith(ObjectWriterCreatorASM.INSTANCE, new Defaults()));
        assertEquals(
                jsonbWith(ObjectWriterCreator.INSTANCE, new NullAsDefaults()),
                jsonbWith(ObjectWriterCreatorASM.INSTANCE, new NullAsDefaults()));
        assertEquals(
                jsonbWith(ObjectWriterCreator.INSTANCE, new NullListsAsEmpty()),
                jsonbWith(ObjectWriterCreatorASM.INSTANCE, new NullListsAsEmpty()));
    }

    @Test
    public void fieldLevelWriteNullsSurvivesFilters() {
        // the filtered write path used to pre-skip null fields with the context features only,
        // so attaching a no-op filter silently dropped every annotated null field
        PropertyFilter passThrough = (object, name, value) -> true;
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE, ObjectWriterCreatorASM.INSTANCE}) {
            JSONObject object = JSON.parseObject(writeFilteredWith(creator, new ForcedNulls(), passThrough));
            for (String name : NULL_FIELDS) {
                assertTrue(object.containsKey(name), "missing field with filter : " + name);
            }
            assertEquals(NULL_FIELDS.length, object.size(), creator.getClass().getSimpleName());
        }

        // without the annotation the filter path keeps omitting nulls
        JSONObject plain = JSON.parseObject(writeFilteredWith(ObjectWriterCreator.INSTANCE, new PlainNulls(), passThrough));
        assertEquals(0, plain.size());
    }

    private static String writeWith(ObjectWriterCreator creator, Object bean, JSONWriter.Feature... features) {
        ObjectWriter<Object> objectWriter = creator.createObjectWriter(bean.getClass());
        return objectWriter.toJSONString(bean, features);
    }

    private static String writeFilteredWith(ObjectWriterCreator creator, Object bean, PropertyFilter filter) {
        ObjectWriter<Object> objectWriter = creator.createObjectWriter(bean.getClass());
        JSONWriter.Context context = new JSONWriter.Context(
                com.alibaba.fastjson2.JSONFactory.getDefaultObjectWriterProvider());
        context.configFilter(filter);
        try (JSONWriter writer = JSONWriter.of(context)) {
            objectWriter.write(writer, bean, null, null, 0);
            return writer.toString();
        }
    }

    private static String jsonbWith(ObjectWriterCreator creator, Object bean) {
        ObjectWriter<Object> objectWriter = creator.createObjectWriter(bean.getClass());
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        objectWriter.write(jsonWriter, bean);
        // the pretty printer spreads the JSON over lines; normalize it for the assertions
        return JSONB.toJSONString(jsonWriter.getBytes()).replaceAll("\\s+", "");
    }
}
