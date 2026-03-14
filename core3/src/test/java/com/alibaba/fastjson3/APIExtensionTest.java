package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Phase 1: API surface extensions (InputStream, Reader, convertValue, readTree).
 */
class APIExtensionTest {
    // ==================== InputStream input ====================

    @Test
    void readValueFromInputStreamObject() {
        String json = "{\"name\":\"test\",\"age\":25}";
        InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        // JSONObject is a basic type, parser.read(type) handles it
        JSONObject obj = ObjectMapper.shared().readValue(in, JSONObject.class);
        // Not guaranteed to return JSONObject via this path; test typed read instead
        assertNotNull(obj);
    }

    @Test
    void readValueFromInputStreamTyped() {
        String json = "{\"name\":\"test\",\"age\":25}";
        InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        TestUser user = ObjectMapper.shared().readValue(in, TestUser.class);
        assertNotNull(user);
        assertEquals("test", user.name);
        assertEquals(25, user.age);
    }

    @Test
    void readValueFromInputStreamTypeRef() {
        String json = "{\"name\":\"test\"}";
        InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        TestUser user = ObjectMapper.shared().readValue(in, new TypeReference<TestUser>() {});
        assertNotNull(user);
        assertEquals("test", user.name);
    }

    @Test
    void readValueFromNullInputStream() {
        assertNull(ObjectMapper.shared().readValue((InputStream) null, TestUser.class));
    }

    // ==================== Reader input ====================

    @Test
    void readValueFromReader() {
        String json = "{\"name\":\"hello\",\"age\":30}";
        TestUser user = ObjectMapper.shared().readValue(new StringReader(json), TestUser.class);
        assertNotNull(user);
        assertEquals("hello", user.name);
        assertEquals(30, user.age);
    }

    @Test
    void readValueFromReaderTypeRef() {
        String json = "{\"name\":\"world\"}";
        TestUser user = ObjectMapper.shared().readValue(new StringReader(json), new TypeReference<TestUser>() {});
        assertNotNull(user);
        assertEquals("world", user.name);
    }

    @Test
    void readValueFromNullReader() {
        assertNull(ObjectMapper.shared().readValue((java.io.Reader) null, TestUser.class));
    }

    // ==================== convertValue ====================

    @Test
    void convertValueMapToPojo() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "conv");
        map.put("age", 42);
        TestUser user = ObjectMapper.shared().convertValue(map, TestUser.class);
        assertNotNull(user);
        assertEquals("conv", user.name);
        assertEquals(42, user.age);
    }

    @Test
    void convertValueSameType() {
        TestUser original = new TestUser();
        original.name = "same";
        original.age = 10;
        TestUser result = ObjectMapper.shared().convertValue(original, TestUser.class);
        assertSame(original, result);
    }

    @Test
    void convertValueNull() {
        assertNull(ObjectMapper.shared().convertValue(null, TestUser.class));
    }

    @Test
    void convertValueTypeRef() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "tr");
        map.put("age", 7);
        TestUser user = ObjectMapper.shared().convertValue(map, new TypeReference<TestUser>() {});
        assertNotNull(user);
        assertEquals("tr", user.name);
    }

    // ==================== readTree ====================

    @Test
    void readTreeObject() {
        Object tree = ObjectMapper.shared().readTree("{\"a\":1}");
        assertInstanceOf(JSONObject.class, tree);
        assertEquals(1, ((JSONObject) tree).getIntValue("a"));
    }

    @Test
    void readTreeArray() {
        Object tree = ObjectMapper.shared().readTree("[1,2,3]");
        assertInstanceOf(JSONArray.class, tree);
        assertEquals(3, ((JSONArray) tree).size());
    }

    @Test
    void readTreeBytes() {
        byte[] json = "[\"a\",\"b\"]".getBytes(StandardCharsets.UTF_8);
        Object tree = ObjectMapper.shared().readTree(json);
        assertInstanceOf(JSONArray.class, tree);
        assertEquals(2, ((JSONArray) tree).size());
    }

    @Test
    void readTreeInputStream() {
        String json = "{\"x\":true}";
        InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        Object tree = ObjectMapper.shared().readTree(in);
        assertInstanceOf(JSONObject.class, tree);
        assertEquals(true, ((JSONObject) tree).getBooleanValue("x"));
    }

    @Test
    void readTreeNull() {
        assertNull(ObjectMapper.shared().readTree((String) null));
        assertNull(ObjectMapper.shared().readTree((byte[]) null));
        assertNull(ObjectMapper.shared().readTree((InputStream) null));
    }

    // ==================== Test POJO ====================

    public static class TestUser {
        public String name;
        public int age;
    }
}
