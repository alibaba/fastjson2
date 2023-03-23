package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1255 {
    @Test
    public void test_01() throws Exception {
        Model2 model = new Model2();

        model.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(model, JSONWriter.Feature.WriteByteArrayAsBase64);

        assertEquals("{\"value\":\"QUJDREVH\"}", json);

        Model2 model1 = JSON.parseObject(json, Model2.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(model.value, model1.value);
    }

    private static class Model<T> {
        public T value;
    }

    private static class Model2
            extends Model<byte[]> {
    }
}
