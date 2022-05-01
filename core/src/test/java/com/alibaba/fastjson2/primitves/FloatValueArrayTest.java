package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals("{\"values\":null}"
                , JSON.toJSONString(new VO(), JSONWriter.Feature.WriteNulls));
        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO(), JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_writeNull2() {
        assertEquals("{}"
                , JSON.toJSONString(new VO2()));

        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO2(), JSONWriter.Feature.WriteNulls)));
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
