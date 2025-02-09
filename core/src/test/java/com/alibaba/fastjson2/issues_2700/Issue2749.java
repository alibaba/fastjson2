package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2_vo.Int1;
import com.alibaba.fastjson2_vo.Long1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2749 {
    final String str = "{\"v0000\":\"Alexander77\"}";

    @Test
    public void testInt() {
        try (JSONReader jsonReader = JSONReader.of(str)) {
            validateInt(jsonReader);
        }
        try (JSONReader jsonReader = JSONReader.of(str.toCharArray())) {
            validateInt(jsonReader);
        }

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            validateInt(jsonReader);
        }
        try (JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1)) {
            validateInt(jsonReader);
        }
    }

    @Test
    public void testLong() {
        try (JSONReader jsonReader = JSONReader.of(str)) {
            validateLong(jsonReader);
        }
        try (JSONReader jsonReader = JSONReader.of(str.toCharArray())) {
            validateLong(jsonReader);
        }

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            validateLong(jsonReader);
        }
        try (JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1)) {
            validateLong(jsonReader);
        }
    }

    @Test
    public void testDecimal() {
        try (JSONReader jsonReader = JSONReader.of(str)) {
            validateDecimal(jsonReader);
        }
        try (JSONReader jsonReader = JSONReader.of(str.toCharArray())) {
            validateDecimal(jsonReader);
        }

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            validateDecimal(jsonReader);
        }
        try (JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1)) {
            validateDecimal(jsonReader);
        }
    }

    private static void validateLong(JSONReader jsonReader) {
        JSONException error = null;
        try {
            jsonReader.read(Long1.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("parseLong error"));
        assertTrue(error.getMessage().contains("Alexander77"));
        assertTrue(error.getMessage().contains("line 1, column 23"));
    }

    private static void validateInt(JSONReader jsonReader) {
        JSONException error = null;
        try {
            jsonReader.read(Int1.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("parseInt error"));
        assertTrue(error.getMessage().contains("Alexander77"));
        assertTrue(error.getMessage().contains("line 1, column 23"));
    }

    private static void validateDecimal(JSONReader jsonReader) {
        JSONException error = null;
        try {
            jsonReader.read(BigDecimal1.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("Alexander77"));
        assertTrue(error.getMessage().contains("line 1, column 23"));
    }

    public static class BigDecimal1 {
        public BigDecimal v0000;
    }
}
