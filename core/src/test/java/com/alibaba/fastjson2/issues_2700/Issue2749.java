package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2_vo.Long1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2749 {
    @Test
    public void test() {
        String str = "{\"v0000\":\"Alexander77\"}";
        try (JSONReader jsonReader = JSONReader.of(str)) {
            validate(jsonReader);
        }
        try (JSONReader jsonReader = JSONReader.of(str.toCharArray())) {
            validate(jsonReader);
        }

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            validate(jsonReader);
        }
        try (JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1)) {
            validate(jsonReader);
        }
    }

    private static void validate(JSONReader jsonReader) {
        JSONException error = null;
        try {
            jsonReader.read(Long1.class);
        } catch (JSONException e) {
            error = e;
        }
        assertTrue(error.getMessage().contains("parseLong error"));
        assertTrue(error.getMessage().contains("Alexander77"));
    }
}
