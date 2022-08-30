package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue702 {
    @Test
    public void test_0() throws Exception {
        Model model = new Model();

        model.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(model, JSONWriter.Feature.WriteByteArrayAsBase64);

        assertEquals("{\"value\":\"QUJDREVH\"}", json);

        Model model1 = JSON.parseObject(json, Model.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(model.value, model1.value);
    }

    private static class Model {
        public byte[] value;
    }

    @Test
    public void test_1() throws Exception {
        Bean1 bean = new Bean1();

        bean.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(bean);

        assertEquals("{\"value\":\"QUJDREVH\"}", json);

        Bean1 bean1 = JSON.parseObject(json, Bean1.class);
        assertArrayEquals(bean.value, bean1.value);
    }

    private static class Bean1 {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteByteArrayAsBase64, deserializeFeatures = JSONReader.Feature.Base64StringAsByteArray)
        public byte[] value;
    }

    @Test
    public void test_2() throws Exception {
        Bean2 bean = new Bean2();

        bean.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(bean);

        assertEquals("{\"value\":\"QUJDREVH\"}", json);

        Bean2 bean1 = JSON.parseObject(json, Bean2.class);
        assertArrayEquals(bean.value, bean1.value);
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.WriteByteArrayAsBase64, deserializeFeatures = JSONReader.Feature.Base64StringAsByteArray)
    private static class Bean2 {
        public byte[] value;
    }

    @Test
    public void test_3() throws Exception {
        Bean3 bean = new Bean3();

        bean.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(bean);

        assertEquals("{\"value\":\"QUJDREVH\"}", json);

        Bean3 bean1 = JSON.parseObject(json, Bean3.class);
        assertArrayEquals(bean.value, bean1.value);
    }

    public static class Bean3 {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteByteArrayAsBase64, deserializeFeatures = JSONReader.Feature.Base64StringAsByteArray)
        public byte[] value;
    }

    @Test
    public void test_4() throws Exception {
        Bean4 bean = new Bean4();

        bean.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(bean);

        assertEquals("{\"value\":\"QUJDREVH\"}", json);

        Bean4 bean1 = JSON.parseObject(json, Bean4.class);
        assertArrayEquals(bean.value, bean1.value);
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.WriteByteArrayAsBase64, deserializeFeatures = JSONReader.Feature.Base64StringAsByteArray)
    public static class Bean4 {
        public byte[] value;
    }
}
