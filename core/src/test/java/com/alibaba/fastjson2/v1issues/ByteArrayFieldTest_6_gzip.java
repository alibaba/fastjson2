package com.alibaba.fastjson2.v1issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayFieldTest_6_gzip {

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

        assertEquals("{\"value\":\"H4sIAAAAAAAAAO3IsRGAIBAAsJVeUE5LBBXcfyC3sErKxJLyupX9iHq2ft3PmG8455xzzjnnnHPOOeecc84555xzzjnnnHPOOeecc84555xzzjnnnHPOOeecc84555z7/T6powiAIE4AAA==\"}", json);

        Model model1 = JSON.parseObject(json, Model.class);
        assertArrayEquals(model.value, model1.value);
    }

    public static class Model {
        @JSONField(format = "gzip,base64")
        public byte[] value;
    }
}
