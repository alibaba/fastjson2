package com.alibaba.fastjson2.arraymapping;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EishayTest {
    static final JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
//            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol,
            JSONWriter.Feature.BeanToArray
    };

    static final JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.SupportArrayToBean,
            JSONReader.Feature.FieldBased
    };

    Object object;

    @BeforeEach
    public void init() {
        URL url = this.getClass().getClassLoader().getResource("data/eishay.json");
        object = JSON.parseObject(url, MediaContent.class);
    }

    @Test
    public void test() {
        byte[] bytes = JSONB.toBytes(object, writerFeatures);
        JSONBDump.dump(bytes);
        Object object1 = JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(object, object1);
    }

    @Test
    public void test1() {
        Sample sample = new Sample();
        sample.intValue = 101;
        sample.longValue = 1001;
        sample.charArray = new char[]{'A', 'B'};
        sample.shortArray = new short[]{1, 2};
        sample.intArray = new int[]{1, 2};
        sample.longArray = new long[]{1, 2};
        sample.floatArray = new float[]{1, 2, 3};
        sample.doubleArray = new double[]{1, 2, 3};
        sample.booleanArray = new boolean[]{true, false};
        byte[] bytes = JSONB.toBytes(sample, writerFeatures);
        System.out.println(JSONB.toJSONString(bytes));
        Sample sample1 = (Sample) JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(sample, sample1);
    }

    public static class Sample {
        public int intValue;
        public long longValue;
        public float floatValue;
        public double doubleValue;
        public short shortValue;
        public char charValue;
        public boolean booleanValue;
        public int[] intArray;
        public long[] longArray;
        public float[] floatArray;
        public double[] doubleArray;
        public short[] shortArray;
        public char[] charArray;
        public boolean[] booleanArray;

        public String string; // Can be null.
        public Sample sample; // Can be null.

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Sample sample1 = (Sample) o;
            return intValue == sample1.intValue
                    && longValue == sample1.longValue
                    && Float.compare(sample1.floatValue, floatValue) == 0
                    && Double.compare(sample1.doubleValue, doubleValue) == 0
                    && shortValue == sample1.shortValue
                    && charValue == sample1.charValue
                    && booleanValue == sample1.booleanValue
                    && Arrays.equals(intArray, sample1.intArray)
                    && Arrays.equals(longArray, sample1.longArray)
                    && Arrays.equals(floatArray, sample1.floatArray)
                    && Arrays.equals(doubleArray, sample1.doubleArray)
                    && Arrays.equals(shortArray, sample1.shortArray)
                    && Arrays.equals(charArray, sample1.charArray)
                    && Arrays.equals(booleanArray, sample1.booleanArray)
                    && Objects.equals(string, sample1.string)
                    && Objects.equals(sample, sample1.sample);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(intValue, longValue, floatValue, doubleValue, shortValue, charValue, booleanValue, string, sample);
            result = 31 * result + Arrays.hashCode(intArray);
            result = 31 * result + Arrays.hashCode(longArray);
            result = 31 * result + Arrays.hashCode(floatArray);
            result = 31 * result + Arrays.hashCode(doubleArray);
            result = 31 * result + Arrays.hashCode(shortArray);
            result = 31 * result + Arrays.hashCode(charArray);
            result = 31 * result + Arrays.hashCode(booleanArray);
            return result;
        }
    }
}
