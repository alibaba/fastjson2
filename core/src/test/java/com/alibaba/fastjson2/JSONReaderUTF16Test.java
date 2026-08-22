package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONReaderUTF16Test {
    @Test
    public void truncatedEscape() {
        String[] inputs = {
                "\"\\",
                "{\"a\":\"\\",
                "[\"\\",
                "\"ab\\",
                "\"中\\"
        };

        for (String input : inputs) {
            assertThrows(JSONException.class, () -> JSON.parse(input));
            assertThrows(JSONException.class, () -> JSON.parse(input.toCharArray()));
        }
    }

    @Test
    public void truncatedEscapePublicPaths() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\\".toCharArray()));
        String[] numberInputs = {
                "{\"a\":\"\\",
                "{\"a\":\"\\u",
                "{\"a\":\"\\x"
        };
        for (String input : numberInputs) {
            assertThrows(JSONException.class, () -> JSON.parseObject(input.toCharArray(), Bean.class));
        }

        assertThrows(JSONException.class, () -> JSON.parseObject("{\"x\":\"\\".toCharArray(), Bean.class));
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject("{a\\".toCharArray(), Object.class, JSONReader.Feature.AllowUnQuotedFieldNames)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject("{a\\x".toCharArray(), Object.class, JSONReader.Feature.AllowUnQuotedFieldNames)
        );
        assertThrows(JSONException.class, () -> JSON.parseObject("\"ab\\".toCharArray(), Value.class));

        for (String input : new String[]{"\"a\\u", "\"a\\x"}) {
            try (JSONReader jsonReader = JSONReader.of(input.toCharArray())) {
                assertThrows(JSONException.class, jsonReader::skipName);
            }
        }
    }

    @Test
    public void truncatedEscapeInSubRange() {
        char[] chars = "\"ab\\\"".toCharArray();
        try (JSONReader jsonReader = JSONReader.of(chars, 0, 4)) {
            assertThrows(JSONException.class, jsonReader::readString);
        }

        char[] unquotedName = "{a\\x12:1}".toCharArray();
        try (JSONReader jsonReader = JSONReader.of(unquotedName, 0, 4)) {
            jsonReader.getContext().config(JSONReader.Feature.AllowUnQuotedFieldNames);
            assertThrows(JSONException.class, jsonReader::readObject);
        }
    }

    @Test
    public void truncatedEscapeUTF8() {
        assertThrows(JSONException.class, () -> JSON.parse("\"中\\".getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parse("{\"中\\".getBytes(StandardCharsets.UTF_8)));
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject("{\"a\":\"中\\".getBytes(StandardCharsets.UTF_8), Bean.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject("{\"x\":\"中\\".getBytes(StandardCharsets.UTF_8), Bean.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject("\"中\\".getBytes(StandardCharsets.UTF_8), Value.class)
        );

        byte[] bytes = "\"中\\\"".getBytes(StandardCharsets.UTF_8);
        try (JSONReader jsonReader = JSONReader.of(bytes, 0, 5)) {
            assertThrows(JSONException.class, jsonReader::readString);
        }
    }

    public static class Bean {
        public Number a;
    }

    public enum Value {
        A
    }
}
