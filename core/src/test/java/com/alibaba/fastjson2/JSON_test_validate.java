package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSON_test_validate {
    @Test
    public void test0() {
        assertTrue(JSON.isValid("123"));
        assertFalse(JSON.isValid("1}"));
        assertTrue(JSON.isValid("\"aaa\""));
        assertFalse(JSON.isValid("\"aaa\"}"));
        assertTrue(JSON.isValid("{}"));
        assertFalse(JSON.isValid("{}]"));
    }

    @Test
    public void test0_bytes() {
        assertFalse(JSON.isValid((byte[]) null));
        assertFalse(JSON.isValid(new byte[0]));
        assertFalse(JSON.isValid((byte[]) null, 0, 0, StandardCharsets.UTF_8));
        assertFalse(JSON.isValid(new byte[0], 0, 0, StandardCharsets.UTF_8));

        assertTrue(JSON.isValid("123".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValid("1}.getBytes(StandardCharsets.UTF_8)"));
        assertTrue(JSON.isValid("\"aaa\"".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValid(
                "\"aaa\"}".getBytes(StandardCharsets.UTF_8)));
        assertTrue(JSON.isValid("{}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValid("{}]".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void test_isValidArray() {
        assertFalse(JSON.isValidArray("123"));
        assertFalse(JSON.isValidArray("1}"));
        assertFalse(JSON.isValidArray("\"aaa\""));
        assertFalse(JSON.isValidArray("\"aaa\"}"));
        assertFalse(JSON.isValidArray("{}"));
        assertFalse(JSON.isValidArray("{}]"));
        assertTrue(JSON.isValidArray("[]"));
        assertFalse(JSON.isValidArray("[]]"));
    }

    @Test
    public void test_isValidArray_bytes() {
        assertFalse(JSON.isValidArray((byte[]) null));
        assertFalse(JSON.isValidArray(new byte[0]));
        assertFalse(JSON.isValidArray("123".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray("1}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray("\"aaa\"".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray("\"aaa\"}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray("{}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray("{}]".getBytes(StandardCharsets.UTF_8)));
        assertTrue(JSON.isValidArray("[]".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray("[]]".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void test_isValidObject() {
        assertFalse(JSON.isValidObject((String) null));
        assertFalse(JSON.isValidObject(""));
        assertFalse(JSON.isValidObject("123"));
        assertFalse(JSON.isValidObject("1}"));
        assertFalse(JSON.isValidObject("\"aaa\""));
        assertFalse(JSON.isValidObject("\"aaa\"}"));
        assertTrue(JSON.isValidObject("{}"));
        assertFalse(JSON.isValidObject("{}]"));
        assertFalse(JSON.isValidObject("{]"));
        assertFalse(JSON.isValidObject("{"));
        assertFalse(JSON.isValidObject("[]"));
        assertFalse(JSON.isValidObject("[]]"));
    }

    @Test
    public void test_isValidObject_bytes() {
        assertFalse(JSON.isValidObject((byte[]) null));
        assertFalse(JSON.isValidObject(new byte[0]));

        assertFalse(JSON.isValidObject("123".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("1}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("\"aaa\"".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("\"aaa\"}".getBytes(StandardCharsets.UTF_8)));
        assertTrue(JSON.isValidObject("{}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("{}]".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("{]".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("{".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("[]".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("[]]".getBytes(StandardCharsets.UTF_8)));
    }
}
