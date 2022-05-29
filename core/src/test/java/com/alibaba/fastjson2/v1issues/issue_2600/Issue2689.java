package com.alibaba.fastjson2.v1issues.issue_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

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
    public void test_0_bytes() throws Exception {
        Exception error = null;
        try {
            JSON.parse("{\"val\":\"\\x~\"".getBytes(StandardCharsets.UTF_8));
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
    public void test_1_bytes() throws Exception {
        Exception error = null;
        try {
            JSON.parse("{\"val\":'\\x~'".getBytes(StandardCharsets.UTF_8));
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
    public void test_2_bytes() throws Exception {
        Exception error = null;
        try {
            JSON.parse("{\"val\":'\\x1'".getBytes(StandardCharsets.UTF_8));
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

    @Test
    public void test_3_bytes() throws Exception {
        Exception error = null;
        try {
            JSON.parse("{\"val\":'\\x'".getBytes(StandardCharsets.UTF_8));
        } catch (JSONException ex) {
            error = ex;
        }
        assertTrue(
                error.getMessage().startsWith("invalid escape character"));
    }

    @Test
    public void test_3_str() throws Exception {
        Exception error = null;
        try {
            TestUtils.createJSONReaderStr("{\"val\":'\\x'").readAny();
        } catch (JSONException ex) {
            error = ex;
        }
        assertTrue(
                error.getMessage().startsWith("invalid escape character"));
    }
}
