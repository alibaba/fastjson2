package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ByteArrayFieldTest_7_gzip_hex {
    @Test
    public void test_0() throws Exception {
        Model model = new Model();

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 1000; ++i) {
            buf.append("0123456890");
            buf.append("ABCDEFGHIJ");
        }

        model.value = buf.toString().getBytes();

        String json = JSON.toJSONString(model);

        Model model1 = JSON.parseObject(json, Model.class);
        assertArrayEquals(model.value, model1.value);
    }

    private static class Model {
        @JSONField(format = "gzip,base64")
        public byte[] value;
    }
}
