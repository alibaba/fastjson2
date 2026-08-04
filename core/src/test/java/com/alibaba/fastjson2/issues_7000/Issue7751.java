package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7751 {
    static final byte[] HELLO_WORLD = "hello world".getBytes(StandardCharsets.UTF_8);
    static final String BASE64 = "aGVsbG8gd29ybGQ=";

    @Test
    public void testCompact() {
        String json = "{\"data\":\"" + BASE64 + "\"}";
        TestData data = JSON.parseObject(json, TestData.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.data);
    }

    @Test
    public void testTrailingNewline() {
        String json = "{\"data\":\"" + BASE64 + "\"\n}";
        TestData data = JSON.parseObject(json, TestData.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.data);
    }

    @Test
    public void testTrailingSpace() {
        String json = "{\"data\":\"" + BASE64 + "\" }";
        TestData data = JSON.parseObject(json, TestData.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.data);
    }

    @Test
    public void testTrailingTab() {
        String json = "{\"data\":\"" + BASE64 + "\"\t}";
        TestData data = JSON.parseObject(json, TestData.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.data);
    }

    @Test
    public void testPrettyJson() {
        String json = "{\n\t\"data\":\"" + BASE64 + "\"\n}";
        TestData data = JSON.parseObject(json, TestData.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.data);
    }

    @Test
    public void testPrettyJsonGetterSetter() {
        String json = "{\n\t\"data\":\"" + BASE64 + "\"\n}";
        Bean data = JSON.parseObject(json, Bean.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.getData());
    }

    @Test
    public void testGlobalConfig() {
        JSON.config(JSONReader.Feature.Base64StringAsByteArray);
        try {
            String json = "{\n\t\"data\":\"" + BASE64 + "\"\n}";
            Bean data = JSON.parseObject(json, Bean.class);
            assertArrayEquals(HELLO_WORLD, data.getData());
        } finally {
            JSON.config(JSONReader.Feature.Base64StringAsByteArray, false);
        }
    }

    @Test
    public void testPrettyFormatRoundTrip() {
        TestData origin = new TestData();
        origin.data = HELLO_WORLD;
        String json = JSON.toJSONString(origin,
                JSONWriter.Feature.WriteByteArrayAsBase64,
                JSONWriter.Feature.PrettyFormat);
        TestData data = JSON.parseObject(json, TestData.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.data);
        assertEquals(11, data.data.length);
    }

    @Test
    public void testUtf8Bytes() {
        String json = "{\n\t\"data\":\"" + BASE64 + "\"\n}";
        TestData data = JSON.parseObject(
                json.getBytes(StandardCharsets.UTF_8),
                TestData.class,
                JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.data);
    }

    @Test
    public void testLeadingWhitespaceOk() {
        String json = "{\n\t\"data\":\"" + BASE64 + "\"}";
        TestData data = JSON.parseObject(json, TestData.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(HELLO_WORLD, data.data);
    }

    public static class TestData {
        public byte[] data;
    }

    public static class Bean {
        private byte[] data;

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }
}
