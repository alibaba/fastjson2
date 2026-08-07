package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayFieldTest_5_base64 {
    @Test
    public void test_0() throws Exception {
        Model model = new Model();

        model.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(model);

        assertEquals("{\"value\":\"QUJDREVH\"}", json);

        Model model1 = JSON.parseObject(json, Model.class);
        assertArrayEquals(model.value, model1.value);
    }

    private static class Model {
        @JSONField(format = "base64")
        public byte[] value;
    }
}
