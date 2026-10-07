package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7783 {
    public static class Bean {
        public Number value;

        public Bean(Number value) {
            this.value = value;
        }
    }

    @Test
    public void test_double() {
        assertEquals("null", JSON.toJSONString(Double.NaN, JSONWriter.Feature.WriteClassName));
        assertEquals("null", JSON.toJSONString(Double.POSITIVE_INFINITY, JSONWriter.Feature.WriteClassName));
        assertEquals("null", JSON.toJSONString(Double.NEGATIVE_INFINITY, JSONWriter.Feature.WriteClassName));

        assertEquals("null", new String(JSON.toJSONBytes(Double.NaN, JSONWriter.Feature.WriteClassName)));
        assertEquals("null", new String(JSON.toJSONBytes(Double.POSITIVE_INFINITY, JSONWriter.Feature.WriteClassName)));
        assertEquals("null", new String(JSON.toJSONBytes(Double.NEGATIVE_INFINITY, JSONWriter.Feature.WriteClassName)));
    }

    @Test
    public void test_float() {
        assertEquals("null", JSON.toJSONString(Float.NaN, JSONWriter.Feature.WriteClassName));
        assertEquals("null", JSON.toJSONString(Float.POSITIVE_INFINITY, JSONWriter.Feature.WriteClassName));
        assertEquals("null", JSON.toJSONString(Float.NEGATIVE_INFINITY, JSONWriter.Feature.WriteClassName));

        assertEquals("null", new String(JSON.toJSONBytes(Float.NaN, JSONWriter.Feature.WriteClassName)));
        assertEquals("null", new String(JSON.toJSONBytes(Float.POSITIVE_INFINITY, JSONWriter.Feature.WriteClassName)));
        assertEquals("null", new String(JSON.toJSONBytes(Float.NEGATIVE_INFINITY, JSONWriter.Feature.WriteClassName)));
    }

    @Test
    public void test_field() {
        String json = JSON.toJSONString(
                new Bean(Double.NaN),
                JSONWriter.Feature.NotWriteRootClassName,
                JSONWriter.Feature.WriteClassName
        );
        assertEquals("{\"value\":null}", json);
        assertEquals(null, JSON.parseObject(json).get("value"));

        assertEquals(
                "{\"value\":null}",
                JSON.toJSONString(
                        new Bean(Float.NEGATIVE_INFINITY),
                        JSONWriter.Feature.NotWriteRootClassName,
                        JSONWriter.Feature.WriteClassName
                )
        );
    }

    @Test
    public void test_specialAsString() {
        assertEquals(
                "\"NaN\"",
                JSON.toJSONString(
                        Double.NaN,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.WriteFloatSpecialAsString
                )
        );
        assertEquals(
                "\"-Infinity\"",
                JSON.toJSONString(
                        Float.NEGATIVE_INFINITY,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.WriteFloatSpecialAsString
                )
        );
    }

    @Test
    public void test_finiteStillWritesSuffix() {
        assertEquals("1.0D", JSON.toJSONString(1D, JSONWriter.Feature.WriteClassName));
        assertEquals("1.0F", JSON.toJSONString(1F, JSONWriter.Feature.WriteClassName));
    }
}
