package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteNonFiniteAsString {
    @Test
    public void test_utf16() {
        Bean bean = new Bean(Double.NaN, Double.POSITIVE_INFINITY, Float.NaN, Float.NEGATIVE_INFINITY);
        String json = JSON.toJSONString(bean);
        assertEquals("{\"d1\":null,\"d2\":null,\"f1\":null,\"f2\":null}", json);

        Bean bean_NaN = new Bean(Double.NaN, Double.NaN, Float.NaN, Float.NaN);
        String json1 = JSON.toJSONString(bean_NaN, JSONWriter.Feature.WriteNonFiniteAsString);
        assertEquals("{\"d1\":\"NaN\",\"d2\":\"NaN\",\"f1\":\"NaN\",\"f2\":\"NaN\"}", json1);

        Bean bean_POSITIVE_INFINITY = new Bean(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        String json2 = JSON.toJSONString(bean_POSITIVE_INFINITY, JSONWriter.Feature.WriteNonFiniteAsString);
        assertEquals("{\"d1\":\"Infinity\",\"d2\":\"Infinity\",\"f1\":\"Infinity\",\"f2\":\"Infinity\"}", json2);

        Bean bean_NEGATIVE_INFINITY = new Bean(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        String json3 = JSON.toJSONString(bean_NEGATIVE_INFINITY, JSONWriter.Feature.WriteNonFiniteAsString);
        assertEquals("{\"d1\":\"-Infinity\",\"d2\":\"-Infinity\",\"f1\":\"-Infinity\",\"f2\":\"-Infinity\"}", json3);
    }

    @Test
    public void test_utf8() {
        Bean bean = new Bean(Double.NaN, Double.POSITIVE_INFINITY, Float.NaN, Float.NEGATIVE_INFINITY);

        String json = new String(JSON.toJSONBytes(bean), StandardCharsets.UTF_8);
        assertEquals("{\"d1\":null,\"d2\":null,\"f1\":null,\"f2\":null}", json);

        String json2 = new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteNonFiniteAsString), StandardCharsets.UTF_8);
        assertEquals("{\"d1\":\"NaN\",\"d2\":\"Infinity\",\"f1\":\"NaN\",\"f2\":\"-Infinity\"}", json2);
    }

    @Test
    public void test_reflect() {
        Bean bean = new Bean(Double.NaN, Double.POSITIVE_INFINITY, Float.NaN, Float.NEGATIVE_INFINITY);
        ObjectWriter writer = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);
        String json = writer.toJSONString(bean, JSONWriter.Feature.WriteNonFiniteAsString);
        assertEquals("{\"d1\":\"NaN\",\"d2\":\"Infinity\",\"f1\":\"NaN\",\"f2\":\"-Infinity\"}", json);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Bean {
        public double d1;
        public Double d2;
        public float f1;
        public Float f2;
    }
}
