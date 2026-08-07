package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("writer")
public class JSONWriterPathBugDemoTest {
    /**
     * This test demonstrates the bug that was fixed.
     * Before the fix, this test would fail with an ArrayIndexOutOfBoundsException
     * or produce incorrect results when processing JSON field names with Unicode surrogate pairs.
     *
     * The bug was in the Path.toString() method where the wrong loop variable 'i' was used
     * instead of 'j' when processing characters in a string containing surrogate pairs.
     */
    @Test
    public void testUnicodeSurrogatePairsPathBug() {
        // Create a map with keys that contain Unicode surrogate pairs
        Map<String, Object> map = new HashMap<>();

        // These emojis are represented by surrogate pairs:
        // 😀 (grinning face) = \uD83D\uDE00
        // 🌍 (earth globe) = \uD83C\uDF0D
        // 👍 (thumbs up) = \uD83D\uDC4D

        map.put("key😀", "value1");
        map.put("key🌍", "value2");
        map.put("key👍", "value3");

        // Create nested objects to trigger reference path handling
        Map<String, Object> nested1 = new HashMap<>();
        Map<String, Object> nested2 = new HashMap<>();
        nested1.put("nested😀", nested2); // Emoji in nested key
        nested2.put("deepValue", "deepValue");
        map.put("nested", nested1);

        try {
            // Serialize with reference detection enabled to trigger Path.toString()
            String json = JSON.toJSONString(map, JSONWriter.Feature.ReferenceDetection);
            System.out.println("Serialized JSON: " + json);

            // Verify that the JSON contains our values
            assertTrue(json.contains("value1"), "JSON should contain value1");
            assertTrue(json.contains("value2"), "JSON should contain value2");
            assertTrue(json.contains("value3"), "JSON should contain value3");

            System.out.println("Test passed - no exception thrown during serialization");
        } catch (Exception e) {
            fail("Exception thrown during JSON serialization with Unicode surrogate pairs: " + e.getMessage(), e);
        }
    }

    /**
     * Additional test with circular references to further stress the Path.toString() method.
     */
    @Test
    public void testUnicodeSurrogatePairsWithCircularReference() {
        // Create objects with circular references and Unicode surrogate pairs in keys
        Map<String, Object> parent = new HashMap<>();
        Map<String, Object> child = new HashMap<>();

        // Use keys with surrogate pairs
        parent.put("parent😀", child);
        child.put("child👍", parent); // Circular reference with emoji

        try {
            // Serialize with reference detection enabled
            String json = JSON.toJSONString(parent, JSONWriter.Feature.ReferenceDetection);
            System.out.println("Serialized JSON with circular reference: " + json);

            // Should not throw any exception
            System.out.println("Test passed - no exception thrown during serialization with circular reference");
        } catch (Exception e) {
            fail("Exception thrown during JSON serialization with surrogate pairs and circular reference: " + e.getMessage(), e);
        }
    }
}
