package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
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
}
