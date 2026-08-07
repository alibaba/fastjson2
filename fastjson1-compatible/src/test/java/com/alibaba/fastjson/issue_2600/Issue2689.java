package com.alibaba.fastjson.issue_2600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2689 {
    @Test
    public void test_0() throws Exception {
        Exception error = null;
        try {
            JSON.parse("{\"val\":\"\\x~\"");
        } catch (JSONException ex) {
            error = ex;
        }
        assertTrue(
                error.getMessage().startsWith("invalid escape character"));
    }

    @Test
    public void test_1() throws Exception {
        Exception error = null;
        try {
            JSON.parse("{\"val\":'\\x~'");
        } catch (JSONException ex) {
            error = ex;
        }
        assertTrue(
                error.getMessage().startsWith("invalid escape character"));
    }

    @Test
    public void test_2() throws Exception {
        Exception error = null;
        try {
            JSON.parse("{\"val\":'\\x1'");
        } catch (JSONException ex) {
            error = ex;
        }
        assertTrue(
                error.getMessage().startsWith("invalid escape character"));
    }

    @Test
    public void test_3() throws Exception {
        Exception error = null;
        try {
            JSON.parse("{\"val\":'\\x'");
        } catch (JSONException ex) {
            error = ex;
        }
        assertTrue(
                error.getMessage().startsWith("invalid escape character"));
    }
}
