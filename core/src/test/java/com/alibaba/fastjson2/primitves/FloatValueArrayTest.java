package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class FloatValueArrayTest {
    @Test
    public void test_parse_null() {
        float[] values = JSON.parseObject("null", float[].class);
        assertNull(values);
    }

    @Test
    public void test_parse_null_jsonb() {
        float[] values = JSONB.parseObject(JSONB.toBytes((Map) null), float[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        float[] bytes = JSON.parseObject("[101,102]", float[].class);
        assertEquals(2, bytes.length);
        assertEquals(101F, bytes[0]);
        assertEquals(102F, bytes[1]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{101, 102}));
        float[] array = JSONB.parseObject(jsonbBytes, float[].class);
        assertEquals(2, array.length);
        assertEquals(101F, array[0]);
        assertEquals(102F, array[1]);
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
        assertEquals("{}",
                JSON.toJSONString(new VO2()));

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
            float[] values = new float[]{1, 2, 3};
            byte[] bytes = JSONB.toBytes(values, jsonbWriteFeatures);
            float[] parsed = (float[]) JSONB.parseObject(bytes, Object.class, jsonbReaderFeatures);
            assertArrayEquals(values, parsed);
        }
        {
            VO vo = new VO();
            vo.values = new float[]{1.1F, 1.2F};
            byte[] bytes = JSONB.toBytes(vo, jsonbWriteFeatures);
            VO parsed = (VO) JSONB.parseObject(bytes, Object.class, jsonbReaderFeatures);
            assertArrayEquals(vo.values, parsed.values);
        }
    }

    public static class VO {
        public float[] values;
    }

    public static class VO2 {
        private float[] values;

        public float[] getValues() {
            return values;
        }

        public void setValues(float[] values) {
            this.values = values;
        }
    }
}
