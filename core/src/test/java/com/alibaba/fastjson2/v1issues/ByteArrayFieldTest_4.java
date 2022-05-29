package com.alibaba.fastjson2.v1issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayFieldTest_4 {
    @Test
    public void test_0() throws Exception {
        Model model = new Model();

        model.value = "ABCDEG".getBytes();

        String json = JSON.toJSONString(model);

        assertEquals("{\"value\":x'414243444547'}", json);

        Model model1 = JSON.parseObject(json, Model.class);
        assertArrayEquals(model.value, model1.value);
    }

    private static class Model {
        @JSONField(format = "hex")
        public byte[] value;
    }
}
