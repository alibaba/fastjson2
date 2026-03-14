package com.alibaba.fastjson3;

import com.alibaba.fastjson3.util.JDKUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that exercise the Vector API string scanning path.
 * These use strings longer than the vector size (32 or 64 bytes) to ensure
 * the SIMD fast path is actually taken.
 */
class VectorizedScannerTest {
    @Test
    void vectorSupportDetected() {
        // On JDK 17+ with --add-modules jdk.incubator.vector, this should be true
        assertTrue(JDKUtils.VECTOR_SUPPORT, "Vector API should be available in test environment");
        assertTrue(JDKUtils.VECTOR_BYTE_SIZE >= 16, "Vector byte size should be at least 16");
        System.out.println("Vector byte size: " + JDKUtils.VECTOR_BYTE_SIZE);
    }

    // ==================== readString (via JSON.parse) ====================

    @Test
    void longAsciiString() {
        // 200 chars — well beyond any vector size
        String value = "a".repeat(200);
        String json = "\"" + value + "\"";
        assertEquals(value, JSON.parse(json));
    }

    @Test
    void longAsciiStringFromBytes() {
        String value = "abcdefghijklmnopqrstuvwxyz0123456789".repeat(10);
        byte[] json = ("\"" + value + "\"").getBytes(StandardCharsets.UTF_8);
        assertEquals(value, JSON.parseObject(json, String.class));
    }

    @Test
    void longStringWithEscapeAtEnd() {
        // 100 ASCII chars, then an escape near the end
        String prefix = "x".repeat(100);
        String json = "\"" + prefix + "\\nhello\"";
        assertEquals(prefix + "\nhello", JSON.parse(json));
    }

    @Test
    void longStringWithEscapeInMiddle() {
        String part1 = "a".repeat(70);
        String part2 = "b".repeat(70);
        String json = "\"" + part1 + "\\t" + part2 + "\"";
        assertEquals(part1 + "\t" + part2, JSON.parse(json));
    }

    @Test
    void longStringWithUnicodeEscape() {
        String prefix = "z".repeat(80);
        String json = "\"" + prefix + "\\u4e2d\\u6587\"";
        assertEquals(prefix + "\u4e2d\u6587", JSON.parse(json));
    }

    @Test
    void longStringWithUtf8Bytes() {
        // UTF-8 multi-byte characters trigger the non-ASCII path
        String prefix = "x".repeat(80);
        String value = prefix + "\u4e2d\u6587\u65e5\u672c";
        byte[] json = ("\"" + value + "\"").getBytes(StandardCharsets.UTF_8);
        assertEquals(value, JSON.parseObject(json, String.class));
    }

    @Test
    void longStringWithBackslashAtVectorBoundary() {
        // Place a backslash right at various positions around vector boundaries
        for (int pos : new int[]{31, 32, 33, 63, 64, 65, 127, 128}) {
            String before = "a".repeat(pos);
            String json = "\"" + before + "\\n" + "b".repeat(10) + "\"";
            String expected = before + "\n" + "b".repeat(10);
            assertEquals(expected, JSON.parse(json), "Failed at backslash position " + pos);
        }
    }

    @Test
    void longStringWithQuoteAtVectorBoundary() {
        // String ending right at various vector boundary positions
        for (int len : new int[]{31, 32, 33, 63, 64, 65, 127, 128, 129}) {
            String value = "q".repeat(len);
            String json = "\"" + value + "\"";
            assertEquals(value, JSON.parse(json), "Failed at length " + len);
        }
    }

    // ==================== readStringDirect (via byte[] POJO deserialization) ====================

    @Test
    void longStringInObject() {
        String longValue = "hello_world_".repeat(20);
        String json = "{\"name\":\"" + longValue + "\"}";
        JSONObject obj = JSON.parseObject(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(longValue, obj.getString("name"));
    }

    @Test
    void multipleFieldsWithLongStrings() {
        String v1 = "field1_value_".repeat(15);
        String v2 = "field2_value_".repeat(15);
        String json = "{\"a\":\"" + v1 + "\",\"b\":\"" + v2 + "\"}";
        JSONObject obj = JSON.parseObject(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(v1, obj.getString("a"));
        assertEquals(v2, obj.getString("b"));
    }

    @Test
    void longStringInArray() {
        String v = "array_elem_".repeat(20);
        String json = "[\"" + v + "\"]";
        JSONArray arr = JSON.parseArray(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        assertEquals(v, arr.getString(0));
    }

    @Test
    void emptyAndShortStringsStillWork() {
        // Ensure short strings (< vector size) are not broken
        assertEquals("", JSON.parse("\"\""));
        assertEquals("a", JSON.parse("\"a\""));
        assertEquals("ab", JSON.parse("\"ab\""));
        assertEquals("hello", JSON.parse("\"hello\""));
        assertEquals("1234567", JSON.parse("\"1234567\""));
        assertEquals("12345678", JSON.parse("\"12345678\""));
    }

    @Test
    void stringWithOnlyEscapes() {
        String json = "\"\\n\\t\\r\\\\\\\"\"";
        assertEquals("\n\t\r\\\"", JSON.parse(json));
    }

    @Test
    void longStringAllSameChar() {
        // Stress test: 1000 identical chars
        String value = "X".repeat(1000);
        String json = "\"" + value + "\"";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        assertEquals(value, JSON.parseObject(bytes, String.class));
    }

    @Test
    void longStringWithMixedContent() {
        // Mix of ASCII, digits, spaces, and punctuation
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append((char) (32 + (i % 90))); // printable ASCII, avoiding '"' and '\\'
        }
        // Remove any accidental '"' or '\\' that would break the JSON
        String value = sb.toString().replace("\"", "A").replace("\\", "B");
        String json = "\"" + value + "\"";
        assertEquals(value, JSON.parse(json));
    }
}
