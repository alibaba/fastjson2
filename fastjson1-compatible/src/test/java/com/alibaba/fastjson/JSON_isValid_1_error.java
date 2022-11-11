package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class JSON_isValid_1_error {
    @Test
    public void test_for_isValid_0() throws Exception {
        assertFalse(JSON.isValid(null));
        assertFalse(JSON.isValid(""));
    }

    @Test
    public void test_for_isValid_value() throws Exception {
//        assertFalse(JSON.isValid("nul"));
//        assertFalse(JSON.isValid("tru"));
//        assertFalse(JSON.isValid("fals"));
        assertFalse(JSON.isValid("null,null"));
        assertFalse(JSON.isValid("123,"));
        assertFalse(JSON.isValid("123,123"));
        assertFalse(JSON.isValid("12.34,true"));
        assertFalse(JSON.isValid("12.34,123"));
        assertFalse(JSON.isValid("true,123"));
        assertFalse(JSON.isValid("false,123"));
//        assertFalse(JSON.isValid("\"abc"));
        assertFalse(JSON.isValid("\"abc\",123"));
    }

    @Test
    public void test_for_isValid_obj() throws Exception {
        assertFalse(JSON.isValid("{"));
        assertFalse(JSON.isValid("{\"id\":123,}}"));
        assertFalse(JSON.isValid("{\"id\":\"123}"));
        assertFalse(JSON.isValid("{\"id\":{]}"));
        assertFalse(JSON.isValid("{\"id\":{"));
    }

    @Test
    public void test_for_isValid_obj_1() throws Exception {
        assertFalse(JSON.isValidObject("{"));
        assertFalse(JSON.isValidObject("{\"id\":123,}}"));
        assertFalse(JSON.isValidObject("{\"id\":\"123}"));
        assertFalse(JSON.isValidObject("{\"id\":{]}"));
        assertFalse(JSON.isValidObject("{\"id\":{"));
    }

    @Test
    public void test_for_isValid_array() throws Exception {
        assertFalse(JSON.isValid("["));
        assertFalse(JSON.isValid("[[,[]]"));
        assertFalse(JSON.isValid("[{\"id\":123]"));
        assertFalse(JSON.isValid("[{\"id\":\"123\"}"));
        assertFalse(JSON.isValid("[{\"id\":true]"));
        assertFalse(JSON.isValid("[{\"id\":{}]"));
    }

    @Test
    public void test_for_isValid_array_1() throws Exception {
        assertFalse(JSON.isValidArray("["));
        assertFalse(JSON.isValidArray("[[,[]]"));
        assertFalse(JSON.isValidArray("[{\"id\":123]"));
        assertFalse(JSON.isValidArray("[{\"id\":\"123\"}"));
        assertFalse(JSON.isValidArray("[{\"id\":true]"));
        assertFalse(JSON.isValidArray("[{\"id\":{}]"));
    }
}
