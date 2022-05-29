package com.alibaba.fastjson2.v1issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayFieldTest_1 {
    @Test
    public void test_array() throws Exception {
        assertEquals("\"AQ==\"", JSON.toJSONString(new byte[]{1}, "base64"));
    }

    @Test
    public void test_codec_null() throws Exception {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        assertEquals("{\"value\":[]}", text);
    }

    public static class V0 {
        private byte[] value;

        public byte[] getValue() {
            return value;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }
    }
}
