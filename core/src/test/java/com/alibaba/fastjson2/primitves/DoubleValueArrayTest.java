package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DoubleValueArrayTest {
    @Test
    public void test_parse_null() {
        double[] values = JSON.parseObject("null", double[].class);
        assertNull(values);
    }

    @Test
    public void test_parse_null_jsonb() {
        double[] values = JSONB.parseObject(JSONB.toBytes((Map) null), double[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        double[] bytes = JSON.parseObject("[101,102]", double[].class);
        assertEquals(2, bytes.length);
        assertEquals(101D, bytes[0]);
        assertEquals(102D, bytes[1]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{101, 102}));
        double[] array = JSONB.parseObject(jsonbBytes, double[].class);
        assertEquals(2, array.length);
        assertEquals(101D, array[0]);
        assertEquals(102D, array[1]);
    }

    @Test
    public void test_writeNotNull() {
        assertEquals("{}",
                JSON.toJSONString(new VO()));
        assertEquals("{}",
                new String(
                        JSON.toJSONBytes(new VO())));
    }

    @Test
    public void test_writeNull() {
        assertEquals("{\"values\":null}",
                JSON.toJSONString(new VO(), JSONWriter.Feature.WriteNulls));
        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO(), JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_writeNull2() {
        assertEquals("{\"values\":null}",
                JSON.toJSONString(new VO2(), JSONWriter.Feature.WriteNulls));
        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO2(), JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_autoType() {
        final JSONWriter.Feature[] jsonbWriteFeatures = {
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        };

        final JSONReader.Feature[] jsonbReaderFeatures = {
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.SupportArrayToBean
        };

        {
            double[] values = new double[]{1, 2, 3};
            byte[] bytes = JSONB.toBytes(values, jsonbWriteFeatures);
            double[] parsed = (double[]) JSONB.parseObject(bytes, Object.class, jsonbReaderFeatures);
            assertArrayEquals(values, parsed);
        }
        {
            VO vo = new VO();
            vo.values = new double[]{1.1, 1.2};
            byte[] bytes = JSONB.toBytes(vo, jsonbWriteFeatures);
            VO parsed = (VO) JSONB.parseObject(bytes, Object.class, jsonbReaderFeatures);
            assertArrayEquals(vo.values, parsed.values);
        }
    }

    public static class VO {
        public double[] values;
    }

    public static class VO2 {
        private double[] values;

        public double[] getValues() {
            return values;
        }

        public void setValues(double[] values) {
            this.values = values;
        }
    }
}
