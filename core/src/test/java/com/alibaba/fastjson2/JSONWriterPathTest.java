package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JSONWriterPathTest {
    /**
     * Test for reproducing the bug with Unicode surrogate pairs in JSON field names.
     */
    @Test
    public void testUnicodeSurrogatePairsInPath() {
        // Create a map with a key that contains Unicode surrogate pairs
        Map<String, Object> map = new HashMap<>();
        // This emoji is represented by surrogate pairs: \uD83D\uDE00 (grinning face)
        String keyWithSurrogatePairs = "key" + "\uD83D\uDE00"; // "key😀"
        map.put(keyWithSurrogatePairs, "value");

        // Also add a nested object to trigger reference path handling
        Map<String, Object> nested = new HashMap<>();
        nested.put("nestedKey", "nestedValue");
        map.put("nested", nested);

        // Enable reference detection to trigger the Path.toString() code path
        try (JSONWriter writer = JSONWriter.of(JSONWriter.Feature.ReferenceDetection)) {
            // Use JSONObjectWriter directly instead of ObjectWriterCreator
            writer.write(map);

            String json = writer.toString();
            System.out.println("Generated JSON: " + json);
            assertNotNull(json);
            // The test passes if no exception is thrown during serialization
            // Check that the value is present in the output
            assertTrue(json.contains("value"), "JSON should contain the value 'value'");
        } catch (Exception e) {
            fail("Exception thrown during JSON serialization with Unicode surrogate pairs: " + e.getMessage(), e);
        }
    }

    /**
     * Additional test with more complex Unicode surrogate pairs.
     */
    @Test
    public void testComplexUnicodeSurrogatePairsInPath() {
        Map<String, Object> map = new HashMap<>();
        // Using various emojis that are represented with surrogate pairs
        map.put("key😀", "value1"); // Grinning face
        map.put("key🌍", "value2"); // Earth globe
        map.put("key👍", "value3"); // Thumbs up

        try (JSONWriter writer = JSONWriter.of(JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.PrettyFormat)) {
            // Use JSONObjectWriter directly instead of ObjectWriterCreator
            writer.write(map);

            String json = writer.toString();
            System.out.println("Generated JSON: " + json);
            assertNotNull(json);
            // The test passes if no exception is thrown during serialization
            // Check that the values are present in the output
            assertTrue(json.contains("value1"), "JSON should contain the value 'value1'");
            assertTrue(json.contains("value2"), "JSON should contain the value 'value2'");
            assertTrue(json.contains("value3"), "JSON should contain the value 'value3'");
        } catch (Exception e) {
            fail("Exception thrown during JSON serialization with complex Unicode surrogate pairs: " + e.getMessage(), e);
        }
    }

    /**
     * Test that specifically targets the bug with surrogate pairs in path handling.
     * This test creates a circular reference to force the Path.toString() method to be called.
     */
    @Test
    public void testSurrogatePairsInPathWithCircularReference() {
        // Create objects with circular references
        Map<String, Object> parent = new HashMap<>();
        Map<String, Object> child = new HashMap<>();

        // Use keys with surrogate pairs
        parent.put("parent😀", child);
        child.put("child😀", parent); // Circular reference

        // Enable reference detection to trigger the Path.toString() code path
        try (JSONWriter writer = JSONWriter.of(JSONWriter.Feature.ReferenceDetection)) {
            // Use JSONObjectWriter directly instead of ObjectWriterCreator
            writer.write(parent);

            String json = writer.toString();
            System.out.println("Generated JSON with circular reference: " + json);
            assertNotNull(json);
            // Should not throw any exception
        } catch (Exception e) {
            fail("Exception thrown during JSON serialization with surrogate pairs and circular reference: " + e.getMessage(), e);
        }
    }
}
