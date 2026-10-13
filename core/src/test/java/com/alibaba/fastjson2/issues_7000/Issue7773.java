package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A field level {@code @JSONField(serializeFeatures = ...)} must be honoured by the ASM generated
 * ObjectWriter exactly as it is by the reflect one. Before the fix the ASM generator only looked at
 * the writer context, so:
 * <ul>
 *     <li>{@code WriteNulls} was ignored for primitive arrays and for enum fields</li>
 *     <li>{@code NotWriteDefaultValue} was ignored for primitive value fields</li>
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
    }

    private static String writeWith(ObjectWriterCreator creator, Object bean) {
        ObjectWriter<Object> objectWriter = creator.createObjectWriter(bean.getClass());
        return objectWriter.toJSONString(bean);
    }
}
