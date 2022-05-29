package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONPathExtractTest {
    @Test
    public void test_extract_int64() {
        JSONPath path = JSONPath.of("$.id");
        assertEquals("$.id", path.toString());
        assertEquals(Long.valueOf(123),
                path.extractInt64(JSONReader.of("{\"id\":123}")));
    }

    @Test
    public void test_extract_int64Value() {
        JSONPath path = JSONPath.of("$.id");
        assertEquals(123,
                path.extractInt64Value(JSONReader.of("{\"id\":123}")));
    }

    @Test
    public void test_extract_int32() {
        JSONPath path = JSONPath.of("$.id");
        assertEquals(Integer.valueOf(123),
                path.extractInt32(JSONReader.of("{\"id\":123}")));
    }

    @Test
    public void test_extract_int32Value() {
        JSONPath path = JSONPath.of("$.id");
        assertEquals(123,
                path.extractInt32Value(JSONReader.of("{\"id\":123}")));
    }

    @Test
    public void test_extract_scalar() {
        JSONPath path = JSONPath.of("$.id");
        assertEquals("123",
                path.extractScalar(JSONReader.of("{\"id\":123}")));
    }

    @Test
    public void test_extract_null() {
        JSONPath path = JSONPath.of("$.id");
        String json = "{\"id\":null}";
        assertEquals(0,
                path.extractInt32Value(JSONReader.of(json)));
        assertEquals(0,
                path.extractInt64Value(JSONReader.of(json)));
        assertNull(path.extractInt32(JSONReader.of(json)));
        assertNull(path.extractInt64(JSONReader.of(json)));
        assertNull(path.extract(JSONReader.of(json)));
        assertEquals("null", path.extractScalar(JSONReader.of(json)));
    }

    @Test
    public void test_extract_true() {
        JSONPath path = JSONPath.of("$.id");
        String json = "{\"id\":true}";
        assertEquals(1,
                path.extractInt32Value(JSONReader.of(json)));
        assertEquals(1,
                path.extractInt64Value(JSONReader.of(json)));
        assertEquals(Integer.valueOf(1),
                path.extractInt32(JSONReader.of(json)));
        assertEquals(Long.valueOf(1),
                path.extractInt64(JSONReader.of(json)));
        assertEquals(Boolean.TRUE,
                path.extract(JSONReader.of(json)));
        assertEquals("true",
                path.extractScalar(JSONReader.of(json)));
    }

    @Test
    public void test_extract_false() {
        JSONPath path = JSONPath.of("$.id");
        String json = "{\"id\":false}";
        assertEquals(0,
                path.extractInt32Value(JSONReader.of(json)));
        assertEquals(0,
                path.extractInt64Value(JSONReader.of(json)));
        assertEquals(Integer.valueOf(0),
                path.extractInt32(JSONReader.of(json)));
        assertEquals(Long.valueOf(0),
                path.extractInt64(JSONReader.of(json)));
        assertEquals(Boolean.FALSE,
                path.extract(JSONReader.of(json)));
        assertEquals("false",
                path.extractScalar(JSONReader.of(json)));
    }

    @Test
    public void test_extract_str() {
        JSONPath path = JSONPath.of("$.id");
        String json = "{\"id\":\"abc\"}";
        assertEquals("\"abc\"",
                path.extractScalar(JSONReader.of(json)));
    }

    @Test
    public void test_extract_obj() {
        JSONPath path = JSONPath.of("$.id");
        String json = "{\"id\":{}}";
        assertEquals("{}",
                path.extractScalar(JSONReader.of(json)));
    }

    @Test
    public void test_extract_array() {
        JSONPath path = JSONPath.of("$.id");
        String json = "{\"id\":[]}";
        assertEquals("[]",
                path.extractScalar(JSONReader.of(json)));
    }

    @Test
    public void test_extract() {
        JSONPath path = JSONPath.of("$[4].*");
        String json = "[true,false,[],{},{\"id\":123,\"name\":\"DataWorks\"}]";
        Collection eval = (Collection) path.extract(
                JSONReader.of(json));
        assertEquals(2, eval.size());
    }

    @Test
    public void test_extract_2() {
        String json = "[\"0\",1,null,false,true,[],{}]";
        assertEquals(Boolean.TRUE,
                JSONPath.of("$[4]")
                        .extract(
                                JSONReader.of(json)));
        assertEquals(Boolean.FALSE,
                JSONPath.of("$[3]")
                        .extract(
                                JSONReader.of(json)));
        assertEquals(null,
                JSONPath.of("$[2]")
                        .extract(
                                JSONReader.of(json)));
        assertEquals("0",
                JSONPath.of("$[0]")
                        .extract(
                                JSONReader.of(json)));
        assertEquals(Integer.valueOf(1),
                JSONPath.of("$[1]")
                        .extract(
                                JSONReader.of(json)));
        assertEquals("[]",
                JSON.toJSONString(
                        JSONPath
                                .of("$[5]")
                                .extract(
                                        JSONReader.of(json))
                )
        );
        assertEquals("{}",
                JSON.toJSONString(
                        JSONPath
                                .of("$[6]")
                                .extract(
                                        JSONReader.of(json))
                )
        );
    }

    @Test
    public void test_extract_all() {
        String json = "{\"v0\":0,\"v1\":\"1\",\"v2\":true,\"v3\":false,\"v4\":null}";
        assertEquals("[0,\"1\",true,false,null]",
                JSON.toJSONString(
                        JSONPath
                                .of("$.*")
                                .extract(
                                        JSONReader.of(json))
                )
        );
    }

    @Test
    public void test_extract_all_2() {
        String json = "{\"obj\":{\"v0\":0,\"v1\":\"1\",\"v2\":true,\"v3\":false,\"v4\":null}}";
        assertEquals("[0]",
                JSON.toJSONString(
                        JSONPath
                                .of("$..v0")
                                .extract(
                                        JSONReader.of(json))
                )
        );
        assertEquals("[\"1\"]",
                JSON.toJSONString(
                        JSONPath
                                .of("$..v1")
                                .extract(
                                        JSONReader.of(json))
                )
        );
        assertEquals("[true]",
                JSON.toJSONString(
                        JSONPath
                                .of("$..v2")
                                .extract(
                                        JSONReader.of(json))
                )
        );
        assertEquals("[false]",
                JSON.toJSONString(
                        JSONPath
                                .of("$..v3")
                                .extract(
                                        JSONReader.of(json))
                )
        );
        assertEquals("[null]",
                JSON.toJSONString(
                        JSONPath
                                .of("$..v4")
                                .extract(
                                        JSONReader.of(json))
                )
        );
    }
}
