package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

public class Issue571 {
    static final JSONWriter.Feature[] jsonbWriteFeaturesArrayMapping = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol,
            JSONWriter.Feature.BeanToArray
    };

    static final JSONReader.Feature[] jsonbReaderFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.SupportArrayToBean
    };

    @Test
    public void test() {
        Sample sample = new Sample();
        sample.populate(true);
        byte[] jsonbBytes = JSONB.toBytes(sample, jsonbWriteFeaturesArrayMapping);
        System.out.println(JSONB.toJSONString(jsonbBytes));
        JSONB.parseObject(jsonbBytes, Object.class, jsonbReaderFeatures);
    }

    public static class Sample
            implements Serializable {
        public int intValue;
        public long longValue;
        public float floatValue;
        public double doubleValue;
        public short shortValue;
        public char charValue;
        public boolean booleanValue;

        public Integer intValueBoxed;
        public Long longValueBoxed;
        public Float floatValueBoxed;
        public Double doubleValueBoxed;
        public Short shortValueBoxed;
        public Character charValueBoxed;
        public Boolean booleanValueBoxed;

        public int[] intArray;
        public long[] longArray;
        public float[] floatArray;
        public double[] doubleArray;
        public short[] shortArray;
        public char[] charArray;
        public boolean[] booleanArray;

        public String string; // Can be null.
        public Sample sample; // Can be null.

        public Sample() {
        }

        public Sample populate(boolean circularReference) {
            intValue = 123;
            longValue = 1230000;
            floatValue = 12.345f;
            doubleValue = 1.234567;
            shortValue = 12345;
            charValue = '!';
            booleanValue = true;

            intValueBoxed = 321;
            longValueBoxed = 3210000L;
            floatValueBoxed = 54.321f;
            doubleValueBoxed = 7.654321;
            shortValueBoxed = 32100;
            charValueBoxed = '$';
            booleanValueBoxed = Boolean.FALSE;

            intArray = new int[] {-1234, -123, -12, -1, 0, 1, 12, 123, 1234};
            longArray = new long[] {-123400, -12300, -1200, -100, 0, 100, 1200, 12300, 123400};
            floatArray = new float[] {-12.34f, -12.3f, -12, -1, 0, 1, 12, 12.3f, 12.34f};
            doubleArray = new double[] {-1.234, -1.23, -12, -1, 0, 1, 12, 1.23, 1.234};
            shortArray = new short[] {-1234, -123, -12, -1, 0, 1, 12, 123, 1234};
            charArray = "asdfASDF".toCharArray();
            booleanArray = new boolean[] {true, false, false, true};

            string = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            if (circularReference) {
                sample = this;
            }
            return this;
        }
    }
}
