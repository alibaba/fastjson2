package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrimStringTest {
    @Test
    public void test() {
        String json = "{\"value\":\" a b \"}";
        byte[] utf8Bytes = json.getBytes(StandardCharsets.UTF_8);

        assertEquals("a b",
                JSON.parseObject(json, JSONReader.Feature.TrimString)
                        .getString("value"));

        assertEquals("a b",
                JSON.parseObject(utf8Bytes, JSONReader.Feature.TrimString)
                        .getString("value"));

        assertEquals("a b",
                JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, JSONReader.Feature.TrimString)
                        .getString("value"));
    }

    @Test
    public void testJSONB() {
        byte[] jsonbBytes = JSONObject.of("value", " a b ").toJSONBBytes();
        assertEquals("a b", JSONB.parseObject(jsonbBytes, JSONReader.Feature.TrimString).getString("value"));
    }
}
