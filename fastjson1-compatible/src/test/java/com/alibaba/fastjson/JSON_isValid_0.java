package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSON_isValid_0 {
    @Test
    public void test_for_isValid_0() throws Exception {
        assertFalse(JSON.isValid(null));
        assertFalse(JSON.isValid(""));
    }

    @Test
    public void test_for_isValid_value() throws Exception {
        assertTrue(JSON.isValid("null"));
        assertTrue(JSON.isValid("123"));
        assertTrue(JSON.isValid("12.34"));
        assertTrue(JSON.isValid("true"));
        assertTrue(JSON.isValid("false"));
        assertTrue(JSON.isValid("\"abc\""));
    }

    @Test
    public void test_for_isValid_obj() throws Exception {
        assertTrue(JSON.isValid("{}"));
        assertTrue(JSON.isValid("{\"id\":123}"));
        assertTrue(JSON.isValid("{\"id\":\"123\"}"));
        assertTrue(JSON.isValid("{\"id\":true}"));
        assertTrue(JSON.isValid("{\"id\":{}}"));
    }

    @Test
    public void test_for_isValid_obj_1() throws Exception {
        assertTrue(JSON.isValidObject("{}"));
        assertTrue(JSON.isValidObject("{\"id\":123}"));
        assertTrue(JSON.isValidObject("{\"id\":\"123\"}"));
        assertTrue(JSON.isValidObject("{\"id\":true}"));
        assertTrue(JSON.isValidObject("{\"id\":{}}"));
    }

    @Test
    public void test_for_isValid_array() throws Exception {
        assertTrue(JSON.isValid("[]"));
        assertTrue(JSON.isValid("[[],[]]"));
        assertTrue(JSON.isValid("[{\"id\":123}]"));
        assertTrue(JSON.isValid("[{\"id\":\"123\"}]"));
        assertTrue(JSON.isValid("[{\"id\":true}]"));
        assertTrue(JSON.isValid("[{\"id\":{}}]"));
    }

    @Test
    public void test_for_isValid_array_1() throws Exception {
        assertTrue(JSON.isValidArray("[]"));
        assertTrue(JSON.isValidArray("[[],[]]"));
        assertTrue(JSON.isValidArray("[{\"id\":123}]"));
        assertTrue(JSON.isValidArray("[{\"id\":\"123\"}]"));
        assertTrue(JSON.isValidArray("[{\"id\":true}]"));
        assertTrue(JSON.isValidArray("[{\"id\":{}}]"));
    }
}
