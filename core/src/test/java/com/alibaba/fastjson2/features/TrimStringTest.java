package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
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
                JSON.parseObject(
                        utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, JSONReader.Feature.TrimString
                ).getString("value")
        );
        assertEquals("a b",
                JSON.parseObject(
                        new ByteArrayInputStream(utf8Bytes),
                        StandardCharsets.US_ASCII,
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                ).getString("value")
        );
        assertEquals("a b",
                ((JSONObject) JSON.parse(
                        new ByteArrayInputStream(utf8Bytes),
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                )).getString("value")
        );
        assertEquals("a b",
                JSON.parseObject(
                        new ByteArrayInputStream(utf8Bytes),
                        StandardCharsets.US_ASCII,
                        Bean.class,
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                ).value
        );
        assertEquals("a b",
                ((Bean) JSON.parseObject(
                        new ByteArrayInputStream(utf8Bytes),
                        StandardCharsets.US_ASCII,
                        (Type) Bean.class,
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                )).value
        );

        assertEquals("a b",
                JSON.parseArray(
                        new ByteArrayInputStream("[\" a b \"]".getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.US_ASCII,
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                ).get(0)
        );
    }

    @Test
    public void testJSONB() {
        byte[] jsonbBytes = JSONObject.of("value", " a b ").toJSONBBytes();
        String result = "a b";
        assertEquals(
                result,
                JSONB.parseObject(
                        new ByteArrayInputStream(jsonbBytes),
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                ).getString("value")
        );
        assertEquals(
                result,
                ((JSONObject) JSONB.parse(
                        jsonbBytes,
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                )).getString("value")
        );
        assertEquals(
                result,
                JSONB.parseObject(
                        jsonbBytes,
                        JSONReader.Feature.TrimString
                ).getString("value")
        );
        assertEquals(
                result,
                ((JSONObject) JSONB.parse(
                        new ByteArrayInputStream(jsonbBytes),
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                )).getString("value")
        );

        assertEquals(
                result,
                JSONB.parseArray(
                        new ByteArrayInputStream(JSONArray.of(" a b ").toJSONBBytes()),
                        new JSONReader.Context(JSONReader.Feature.TrimString)
                ).getString(0)
        );
    }

    public static class Bean {
        public String value;
    }
}
