package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONPathExtractTest2 {
    @Test
    public void test_extract_int64() {
        JSONPath path = JSONPath.of("$[0].id");
        assertEquals("$[0].id", path.toString());
        assertEquals(Long.valueOf(123),
                path.extractInt64(JSONReader.of("[{\"id\":123}]")));
    }

    @Test
    public void test_extract_int64Value() {
        JSONPath path = JSONPath.of("$[0].id");
        assertEquals(123,
                path.extractInt64Value(JSONReader.of("[{\"id\":123}]")));
    }

    @Test
    public void test_extract_int32() {
        JSONPath path = JSONPath.of("$[0].id");
        assertEquals(Integer.valueOf(123),
                path.extractInt32(JSONReader.of("[{\"id\":123}]")));
    }

    @Test
    public void test_extract_int32Value() {
        JSONPath path = JSONPath.of("$[0].id");
        assertEquals(123,
                path.extractInt32Value(JSONReader.of("[{\"id\":123}]")));
    }

    @Test
    public void test_extract_scalar() {
        JSONPath path = JSONPath.of("$[0].id");
        assertEquals("123",
                path.extractScalar(JSONReader.of("[{\"id\":123}]")));
    }

    @Test
    public void test_extract_null() {
        JSONPath path = JSONPath.of("$[0].id");
        String json = "[{\"id\":null}]";
        assertEquals(0,
                path.extractInt32Value(JSONReader.of(json)));
        assertEquals(0,
                path.extractInt64Value(JSONReader.of(json)));
        assertNull(path.extractInt32(JSONReader.of(json)));
        assertNull(
                path.extractInt64(
                        JSONReader.of(json)));
        assertNull(
                path.extract(
                        JSONReader.of(json)));
    }

    @Test
    public void test_extract_true() {
        JSONPath path = JSONPath.of("$[0].id");
        String json = "[{\"id\":true}]";
        assertEquals(1,
                path.extractInt32Value(
                        JSONReader.of(json)));
        assertEquals(1,
                path.extractInt64Value(
                        JSONReader.of(json)));
        assertEquals(Integer.valueOf(1),
                path.extractInt32(
                        JSONReader.of(json)));
        assertEquals(Long.valueOf(1),
                path.extractInt64(
                        JSONReader.of(json)));
    }

    @Test
    public void test_extract_false() {
        JSONPath path = JSONPath.of("$[0].id");
        String json = "[{\"id\":false}]";
        assertEquals(0,
                path.extractInt32Value(
                        JSONReader.of(json)));
        assertEquals(0,
                path.extractInt64Value(
                        JSONReader.of(json)));
        assertEquals(Integer.valueOf(0),
                path.extractInt32(
                        JSONReader.of(json)));
        assertEquals(Long.valueOf(0),
                path.extractInt64(
                        JSONReader.of(json)));
    }

    @Test
    public void test_extract_str_num() {
        JSONPath path = JSONPath.of("$[0].id");
        String json = "[{\"id\":\"1\"}]";
        assertEquals(1,
                path.extractInt32Value(
                        JSONReader.of(json)));
        assertEquals(1,
                path.extractInt64Value(
                        JSONReader.of(json)));
        assertEquals(Integer.valueOf(1),
                path.extractInt32(
                        JSONReader.of(json)));
        assertEquals(Long.valueOf(1),
                path.extractInt64(
                        JSONReader.of(json)));
    }

    @Test
    public void test_extract_str_num2() {
        JSONPath path = JSONPath.of("$[1].id");
        String json = "[{\"id\":\"0\"},{\"v0\":{},\"v1\":[],\"id\":\"1\"}]";
        assertEquals(1,
                path.extractInt32Value(
                        JSONReader.of(json)));
        assertEquals(1,
                path.extractInt64Value(
                        JSONReader.of(json)));
        assertEquals(Integer.valueOf(1),
                path.extractInt32(
                        JSONReader.of(json)));
        assertEquals(Long.valueOf(1),
                path.extractInt64(
                        JSONReader.of(json)));
    }

    @Test
    public void test_extract_str_num2_utf8() {
        JSONPath path = JSONPath.of("$[2].id");
        String json = "[{\"id\":\"0\"},1,{\"v0\":{},\"v1\":[],\"id\":\"1\"}]";
        byte[] utf8 = json.getBytes(StandardCharsets.UTF_8);
        assertEquals(1,
                path.extractInt32Value(
                        JSONReader.of(utf8)));
        assertEquals(1,
                path.extractInt64Value(
                        JSONReader.of(utf8)));
        assertEquals(Integer.valueOf(1),
                path.extractInt32(
                        JSONReader.of(utf8)));
        assertEquals(Long.valueOf(1),
                path.extractInt64(
                        JSONReader.of(utf8)));
    }
}
