package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue435 {
    @Test
    public void test() {
        char ch = 17;
        String str =
                "{\"test\": \"Pixel 民" + ch + "\n XL\"}";
        Object object = com.alibaba.fastjson2.JSON.parse(str);
        assertEquals("{\"test\":\"Pixel 民\\u0011\\n XL\"}", JSON.toJSONString(object));
    }

    @Test
    public void testJSONBytes() {
        char ch = 17;
        String str =
                "{\"test\": \"Pixel 民" + ch + "\n XL\"}";
        Object object = com.alibaba.fastjson2.JSON.parse(str);
        byte[] jsonBytes = JSON.toJSONBytes(object);
        String jsonStr = new String(jsonBytes, 0, jsonBytes.length, StandardCharsets.UTF_8);
        assertEquals("{\"test\":\"Pixel 民\\u0011\\n XL\"}", jsonStr);
    }

    @Test
    public void test256() {
        for (int i = 0; i < 256; ++i) {
            char ch = (char) i;
            String str = new String(new char[]{ch});
            String json = JSON.toJSONString(str);
            assertEquals(str, JSON.parse(json), "fail " + i);
        }
    }

    @Test
    public void test256Bytes() {
        for (int i = 0; i < 256; ++i) {
            char ch = (char) i;
            String str = new String(new char[]{ch});
            byte[] jsonBytes = JSON.toJSONBytes(str);
            assertEquals(str, JSON.parse(jsonBytes), "fail " + i);
        }
    }

    @Test
    public void test256All() {
        for (int i = 0; i < 256; ++i) {
            char ch = (char) i;
            String str = new String(new char[]{ch});

            JSONWriter[] jsonWriters = TestUtils.createJSONWriters();
            for (JSONWriter jsonWriter : jsonWriters) {
                jsonWriter.writeString(str);
                String json = jsonWriter.toString();
                assertEquals(str, JSON.parse(json), "fail " + i);
            }
        }
    }
}
