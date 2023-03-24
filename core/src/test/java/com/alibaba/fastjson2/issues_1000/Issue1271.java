package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1271 {
    private static class Model<T> {
        public T value;
    }

    private static class Model1
            extends Model<byte[]> {
    }

    @Test
    public void test_1() {
        Model1 model = new Model1();
        model.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(model, JSONWriter.Feature.WriteByteArrayAsBase64);
        assertEquals("{\"value\":\"QUJDREVH\"}", json); // correct

        json = JSON.toJSONString(model, "yyyy-MM-dd HH:mm:ss", JSONWriter.Feature.WriteByteArrayAsBase64);
        assertEquals("{\"value\":\"QUJDREVH\"}", json); // mistake
    }
}
