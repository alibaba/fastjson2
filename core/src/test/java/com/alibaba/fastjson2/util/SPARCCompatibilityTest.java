package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SPARCCompatibilityTest {
    private String originalProperty;

    @BeforeEach
    public void setUp() {
        originalProperty = System.getProperty("fastjson2.unsafe.sparc.enabled");
    }

    @AfterEach
    public void tearDown() {
        if (originalProperty != null) {
            System.setProperty("fastjson2.unsafe.sparc.enabled", originalProperty);
        } else {
            System.clearProperty("fastjson2.unsafe.sparc.enabled");
        }
    }

    @Test
    public void testJSONSerializationWithUnsafeEnabled() {
        System.setProperty("fastjson2.unsafe.sparc.enabled", "true");

        TestObject obj = new TestObject();
        obj.name = "Test on SPARC Platform";
        obj.value = 12345;
        obj.description = "This tests JSON serialization with potential unaligned memory access";

        assertDoesNotThrow(() -> {
            String json = JSON.toJSONString(obj);
            assertNotNull(json);
            assertTrue(json.contains("Test on SPARC Platform"));
        }, "JSON serialization should not crash on SPARC with Unsafe enabled");
    }

    @Test
    public void testJSONSerializationWithUnsafeDisabled() {
        System.setProperty("fastjson2.unsafe.sparc.enabled", "false");

        TestObject obj = new TestObject();
        obj.name = "Test SPARC Safe Mode";
        obj.value = 54321;
        obj.description = "This tests JSON serialization in safe mode without Unsafe operations";

        assertDoesNotThrow(() -> {
            String json = JSON.toJSONString(obj);
            assertNotNull(json);
            assertTrue(json.contains("Test SPARC Safe Mode"));
        }, "JSON serialization should work correctly in safe mode");
    }

    @Test
    public void testStringSerializationVariousLengths() {
        String[] testStrings = {
            "A",                                    // 1 char
            "AB",                                   // 2 chars
            "ABC",                                  // 3 chars
            "ABCD",                                 // 4 chars (1 long)
            "ABCDE",                                // 5 chars
            "ABCDEFGH",                             // 8 chars (2 longs)
            "ABCDEFGHI",                            // 9 chars
            "ABCDEFGHIJKLMNOP",                     // 16 chars (4 longs)
            "This is a longer string to test the SPARC memory alignment fix", // Long string
            "中文测试SPARC平台兼容性",                   // Unicode characters
            "Mixed ASCII and 中文 content",          // Mixed content
            "Special chars: \t\n\r\"'\\",           // Escape characters
        };

        for (String testString : testStrings) {
            assertDoesNotThrow(() -> {
                String json = JSON.toJSONString(testString);
                assertNotNull(json);

                String parsed = JSON.parseObject(json, String.class);
                assertEquals(testString, parsed);
            }, "Should handle string of length " + testString.length() + " without issues");
        }
    }

    @Test
    public void testComplexObjectSerialization() {
        ComplexTestObject obj = new ComplexTestObject();
        obj.id = 999;
        obj.name = "SPARC Compatibility Test Object";
        obj.tags = new String[]{"sparc", "memory-alignment", "unsafe", "fastjson2"};
        obj.metadata = new HashMap<>();
        obj.metadata.put("platform", "Oracle Solaris 11.4 SPARC");
        obj.metadata.put("jvm", "Oracle JDK 1.8.0_461");
        obj.metadata.put("issue", "SIGBUS BUS_ADRALN in Unsafe_GetLong");

        assertDoesNotThrow(() -> {
            String json = JSON.toJSONString(obj);
            assertNotNull(json);
            assertTrue(json.contains("SPARC Compatibility Test Object"));
            ComplexTestObject parsed = JSON.parseObject(json, ComplexTestObject.class);
            assertEquals(obj.name, parsed.name);
            assertEquals(obj.id, parsed.id);
        }, "Complex object serialization should work on SPARC");
    }

    @Test
    public void testMemoryAlignmentEdgeCases() {
        String[] oddStrings = {
            "A",
            "ABC",
            "ABCDE",
            "ABCDEFG",
            "ABCDEFGHI",
            "ABCDEFGHIJK"
        };

        for (String str : oddStrings) {
            assertDoesNotThrow(() -> {
                TestObject obj = new TestObject();
                obj.name = str;
                obj.description = "Padding: " + repeatString("X", str.length() % 8); // Create potential alignment issues

                String json = JSON.toJSONString(obj);
                assertNotNull(json);
            }, "Should handle potentially unaligned string: " + str);
        }
    }

    @Test
    public void testHighVolumeSerializationStressTest() {
        final int iterations = 1000;

        assertDoesNotThrow(() -> {
            for (int i = 0; i < iterations; i++) {
                TestObject obj = new TestObject();
                obj.name = "Stress test iteration " + i;
                obj.value = i;
                obj.description = "Testing SPARC compatibility under load with varying string lengths: " + repeatString("X", i % 50);

                String json = JSON.toJSONString(obj);
                assertNotNull(json);

                if (i % 100 == 0) {
                    TestObject parsed = JSON.parseObject(json, TestObject.class);
                    assertEquals(obj.name, parsed.name);
                }
            }
        }, "High volume serialization should be stable on SPARC");
    }

    static class TestObject {
        public String name;
        public int value;
        public String description;
    }

    static class ComplexTestObject {
        public int id;
        public String name;
        public String[] tags;
        public Map<String, String> metadata;
    }

    /**
     * JDK 8 compatible string repeat method.
     * Equivalent to String.repeat(int) introduced in JDK 11.
     */
    private static String repeatString(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
