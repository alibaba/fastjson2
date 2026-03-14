package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JSONParserTest {
    // ==================== readAny ====================

    @Test
    void readAnyObject() {
        Object result = JSON.parse("{\"a\":1}");
        assertInstanceOf(JSONObject.class, result);
        assertEquals(1, ((JSONObject) result).getIntValue("a"));
    }

    @Test
    void readAnyArray() {
        Object result = JSON.parse("[1,2,3]");
        assertInstanceOf(JSONArray.class, result);
        assertEquals(3, ((JSONArray) result).size());
    }

    @Test
    void readAnyString() {
        Object result = JSON.parse("\"hello\"");
        assertEquals("hello", result);
    }

    @Test
    void readAnyNumber() {
        assertEquals(42, JSON.parse("42"));
        assertEquals(3.14, JSON.parse("3.14"));
        assertEquals(true, JSON.parse("true"));
        assertEquals(false, JSON.parse("false"));
        assertNull(JSON.parse("null"));
    }

    // ==================== readObject ====================

    @Test
    void readEmptyObject() {
        JSONObject obj = JSON.parseObject("{}");
        assertNotNull(obj);
        assertTrue(obj.isEmpty());
    }

    @Test
    void readSimpleObject() {
        JSONObject obj = JSON.parseObject("{\"name\":\"test\",\"age\":25}");
        assertEquals("test", obj.getString("name"));
        assertEquals(25, obj.getIntValue("age"));
    }

    @Test
    void readNestedObject() {
        JSONObject obj = JSON.parseObject("{\"user\":{\"name\":\"Alice\"},\"scores\":[90,95]}");
        assertEquals("Alice", obj.getJSONObject("user").getString("name"));
        assertEquals(2, obj.getJSONArray("scores").size());
    }

    @Test
    void readObjectWithWhitespace() {
        JSONObject obj = JSON.parseObject("  { \"a\" : 1 , \"b\" : 2 }  ");
        assertEquals(1, obj.getIntValue("a"));
        assertEquals(2, obj.getIntValue("b"));
    }

    // ==================== readArray ====================

    @Test
    void readEmptyArray() {
        JSONArray arr = JSON.parseArray("[]");
        assertNotNull(arr);
        assertTrue(arr.isEmpty());
    }

    @Test
    void readSimpleArray() {
        JSONArray arr = JSON.parseArray("[1,\"two\",true,null]");
        assertEquals(4, arr.size());
        assertEquals(1, arr.getIntValue(0));
        assertEquals("two", arr.getString(1));
        assertTrue(arr.getBooleanValue(2));
        assertNull(arr.get(3));
    }

    @Test
    void readNestedArray() {
        JSONArray arr = JSON.parseArray("[[1,2],[3,4]]");
        assertEquals(2, arr.size());
        assertEquals(2, arr.getJSONArray(0).size());
    }

    // ==================== readString ====================

    @Test
    void readSimpleString() {
        try (JSONParser parser = JSONParser.of("\"hello world\"")) {
            assertEquals("hello world", parser.readString());
        }
    }

    @Test
    void readStringWithEscapes() {
        try (JSONParser parser = JSONParser.of("\"line1\\nline2\\ttab\\\"quote\\\\backslash\"")) {
            String result = parser.readString();
            assertEquals("line1\nline2\ttab\"quote\\backslash", result);
        }
    }

    @Test
    void readStringWithUnicode() {
        try (JSONParser parser = JSONParser.of("\"\\u4e2d\\u6587\"")) {
            assertEquals("\u4e2d\u6587", parser.readString());
        }
    }

    @Test
    void readEmptyString() {
        try (JSONParser parser = JSONParser.of("\"\"")) {
            assertEquals("", parser.readString());
        }
    }

    // ==================== Numbers ====================

    @Test
    void readIntegers() {
        assertEquals(0, JSON.parse("0"));
        assertEquals(1, JSON.parse("1"));
        assertEquals(-1, JSON.parse("-1"));
        assertEquals(42, JSON.parse("42"));
        assertEquals(Integer.MAX_VALUE, JSON.parse(String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    void readLongs() {
        long bigNum = 3000000000L;
        Object result = JSON.parse(String.valueOf(bigNum));
        assertInstanceOf(Long.class, result);
        assertEquals(bigNum, result);
    }

    @Test
    void readBigIntegers() {
        String huge = "99999999999999999999";
        Object result = JSON.parse(huge);
        assertInstanceOf(BigInteger.class, result);
        assertEquals(new BigInteger(huge), result);
    }

    @Test
    void readDoubles() {
        assertEquals(3.14, JSON.parse("3.14"));
        assertEquals(1.0e10, JSON.parse("1e10"));
        assertEquals(1.5e-3, JSON.parse("1.5E-3"));
        assertEquals(-0.5, JSON.parse("-0.5"));
    }

    @Test
    void readDoublesAsBigDecimal() {
        Object result = JSON.parse("3.14", ReadFeature.UseBigDecimalForDoubles);
        assertInstanceOf(BigDecimal.class, result);
        assertEquals(new BigDecimal("3.14"), result);
    }

    // ==================== Boolean/Null ====================

    @Test
    void readBooleans() {
        try (JSONParser parser = JSONParser.of("true")) {
            assertTrue(parser.readBoolean());
        }
        try (JSONParser parser = JSONParser.of("false")) {
            assertFalse(parser.readBoolean());
        }
    }

    @Test
    void readNullLiteral() {
        try (JSONParser parser = JSONParser.of("null")) {
            assertTrue(parser.readNull());
        }
    }

    @Test
    void readNullNotPresent() {
        try (JSONParser parser = JSONParser.of("42")) {
            assertFalse(parser.readNull());
        }
    }

    // ==================== isEnd ====================

    @Test
    void isEndAfterRead() {
        try (JSONParser parser = JSONParser.of("42")) {
            parser.readAny();
            assertTrue(parser.isEnd());
        }
    }

    @Test
    void isEndWithTrailingWhitespace() {
        try (JSONParser parser = JSONParser.of("42  \n  ")) {
            parser.readAny();
            assertTrue(parser.isEnd());
        }
    }

    // ==================== isValid ====================

    @Test
    void isValidTrue() {
        assertTrue(JSON.isValid("{}"));
        assertTrue(JSON.isValid("[]"));
        assertTrue(JSON.isValid("\"hello\""));
        assertTrue(JSON.isValid("42"));
        assertTrue(JSON.isValid("true"));
        assertTrue(JSON.isValid("null"));
    }

    @Test
    void isValidFalse() {
        assertFalse(JSON.isValid(""));
        assertFalse(JSON.isValid((String) null));
        assertFalse(JSON.isValid("{"));
        assertFalse(JSON.isValid("[1,]")); // trailing comma
    }

    // ==================== skipValue ====================

    @Test
    void skipValueObject() {
        try (JSONParser parser = JSONParser.of("[{\"a\":{\"b\":1}},42]")) {
            JSONArray arr = parser.readArray();
            assertEquals(2, arr.size());
        }
    }

    // ==================== UTF-8 byte[] input ====================

    @Test
    void readFromBytes() {
        byte[] bytes = "{\"name\":\"test\"}".getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals("test", obj.getString("name"));
    }

    @Test
    void readUtf8StringFromBytes() {
        String json = "{\"text\":\"\\u4e2d\\u6587\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals("\u4e2d\u6587", obj.getString("text"));
    }

    @Test
    void readUtf8DirectFromBytes() {
        String json = "{\"text\":\"\u4e2d\u6587\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals("\u4e2d\u6587", obj.getString("text"));
    }

    // ==================== char[] input ====================

    @Test
    void readFromCharArray() {
        char[] chars = "{\"a\":1}".toCharArray();
        try (JSONParser parser = JSONParser.of(chars, 0, chars.length)) {
            JSONObject obj = parser.readObject();
            assertEquals(1, obj.getIntValue("a"));
        }
    }

    // ==================== Error cases ====================

    @Test
    void errorOnInvalidInput() {
        assertThrows(JSONException.class, () -> JSON.parse("xyz"));
    }

    @Test
    void errorOnUnterminatedString() {
        assertThrows(JSONException.class, () -> JSON.parse("\"unterminated"));
    }

    @Test
    void errorOnUnterminatedObject() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\":1"));
    }

    @Test
    void errorOnUnterminatedArray() {
        assertThrows(JSONException.class, () -> JSON.parse("[1,2"));
    }

    // ==================== read(Class) ====================

    @Test
    void readTypedString() {
        assertEquals("hello", JSON.parseObject("\"hello\"", String.class));
    }

    @Test
    void readTypedInt() {
        assertEquals(42, JSON.parseObject("42", int.class));
        assertEquals(42, JSON.parseObject("42", Integer.class));
    }

    @Test
    void readTypedLong() {
        assertEquals(42L, JSON.parseObject("42", long.class));
    }

    @Test
    void readTypedDouble() {
        assertEquals(3.14, JSON.parseObject("3.14", double.class));
    }

    @Test
    void readTypedBoolean() {
        assertEquals(true, JSON.parseObject("true", boolean.class));
    }

    // ==================== Complex JSON ====================

    @Test
    void readComplexJson() {
        String json = """
                {
                    "users": [
                        {"name": "Alice", "age": 30, "active": true},
                        {"name": "Bob", "age": 25, "active": false}
                    ],
                    "total": 2,
                    "meta": null
                }
                """;
        JSONObject obj = JSON.parseObject(json);
        assertEquals(2, obj.getIntValue("total"));
        assertNull(obj.get("meta"));
        JSONArray users = obj.getJSONArray("users");
        assertEquals(2, users.size());
        assertEquals("Alice", users.getJSONObject(0).getString("name"));
        assertEquals(25, users.getJSONObject(1).getIntValue("age"));
    }
}
