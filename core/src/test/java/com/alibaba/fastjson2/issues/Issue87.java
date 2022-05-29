package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONReaderUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue87 {
    @Test
    public void valid() {
        String errorJson = "[{\"a\":1}{\"b\":2}null undefined 676]";
        assertFalse(
                JSON.isValid(errorJson));
        assertFalse(
                JSON.isValidArray(errorJson)
        );
    }

    @Test
    public void valid_1() {
        String errorJson = "[\"a\",\"b\"]";
        assertTrue(
                JSON.isValid(errorJson));
    }

    @Test
    public void valid_raw_str() {
        String errorJson = "[{\"a\":1}{\"b\":2}null undefined 676]";

        boolean valid = true;
        JSONReader jsonReader = JSONReaderUtils.createJSONReader(errorJson);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            valid = false;
        }

        assertFalse(valid);
    }

    @Test
    public void valid_raw_str_1() {
        String errorJson = "[\"a\",\"b\"]";

        boolean valid = true;
        JSONReader jsonReader = JSONReaderUtils.createJSONReader(errorJson);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            valid = false;
        }

        assertTrue(valid);
    }

    @Test
    public void valid_utf8() {
        byte[] errorJson = "[{\"a\":1}{\"b\":2}null undefined 676]".getBytes(StandardCharsets.UTF_8);
        assertFalse(
                JSON.isValid(errorJson));
        assertFalse(
                JSON.isValidArray(errorJson)
        );
    }

    @Test
    public void valid_utf8_1() {
        byte[] errorJson = "[\"a\",\"b\"]".getBytes(StandardCharsets.UTF_8);
        assertTrue(
                JSON.isValid(errorJson));
    }

    @Test
    public void valid_ascii() {
        byte[] errorJson = "[{\"a\":1}{\"b\":2}null undefined 676]".getBytes(StandardCharsets.UTF_8);
        assertFalse(
                JSON.isValid(errorJson, 0, errorJson.length, StandardCharsets.US_ASCII));
        assertFalse(
                JSON.isValidArray(errorJson)
        );
    }

    @Test
    public void valid_ascii_1() {
        byte[] errorJson = "[\"a\",\"b\"]".getBytes(StandardCharsets.UTF_8);
        assertTrue(
                JSON.isValid(errorJson, 0, errorJson.length, StandardCharsets.US_ASCII));
    }
}
