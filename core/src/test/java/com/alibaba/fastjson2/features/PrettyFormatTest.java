package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrettyFormatTest {
    @Test
    public void testArray() {
        JSONArray array = JSONArray.of(1, 2, 3);
        assertEquals("[1,2,3]", array.toString());
        assertEquals("[1,2,3]", array.toString(OptimizedForAscii));

        assertEquals(
                "[\n" +
                        "\t1,\n" +
                        "\t2,\n" +
                        "\t3\n" +
                        "]",
                array
                        .toString(PrettyFormat));
        assertEquals(
                "[\n" +
                        "\t1,\n" +
                        "\t2,\n" +
                        "\t3\n" +
                        "]",
                array
                        .toString(PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of("id", 123, "value", "abc");

        assertEquals("{\"id\":123,\"value\":\"abc\"}", jsonObject.toString());
        assertEquals("{\"id\":123,\"value\":\"abc\"}", jsonObject.toString(OptimizedForAscii));

        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormat, OptimizedForAscii));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormatWith4Space));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormatWith4Space, OptimizedForAscii));

        assertEquals(
                "{\n" +
                        "  \"id\":123,\n" +
                        "  \"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormatWith2Space));
        assertEquals(
                "{\n" +
                        "  \"id\":123,\n" +
                        "  \"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormatWith2Space, OptimizedForAscii));
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.setId(123);
        bean.setValue("abc");

        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat, OptimizedForAscii));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormatWith4Space));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormatWith4Space, OptimizedForAscii));

        Bean1[] array = new Bean1[] {bean};
        assertEquals(
                "[\n" +
                        "\t{\n" +
                        "\t\t\"id\":123,\n" +
                        "\t\t\"value\":\"abc\"\n" +
                        "\t}\n" +
                        "]",
                JSON.toJSONString(array, PrettyFormat));
        assertEquals(
                "[\n" +
                        "    {\n" +
                        "        \"id\":123,\n" +
                        "        \"value\":\"abc\"\n" +
                        "    }\n" +
                        "]",
                JSON.toJSONString(array, PrettyFormatWith4Space));
    }

    @Data
    private static class Bean1 {
        private int id;
        private String value;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.setId(123);
        bean.setValue("abc");

        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat, OptimizedForAscii));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormatWith4Space));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormatWith4Space, OptimizedForAscii));

        Bean2[] array = new Bean2[] {bean};
        assertEquals(
                "[\n" +
                        "\t{\n" +
                        "\t\t\"id\":123,\n" +
                        "\t\t\"value\":\"abc\"\n" +
                        "\t}\n" +
                        "]",
                JSON.toJSONString(array, PrettyFormat));
        assertEquals(
                "[\n" +
                        "    {\n" +
                        "        \"id\":123,\n" +
                        "        \"value\":\"abc\"\n" +
                        "    }\n" +
                        "]",
                JSON.toJSONString(array, PrettyFormatWith4Space));
    }

    @Data
    public static class Bean2 {
        private int id;
        private String value;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.values.add("a01");
        bean.values.add("a02");

        assertEquals(
                "{\n" +
                        "\t\"values\":[\n" +
                        "\t\t\"a01\",\n" +
                        "\t\t\"a02\"\n" +
                        "\t]\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"values\":[\n" +
                        "\t\t\"a01\",\n" +
                        "\t\t\"a02\"\n" +
                        "\t]\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat, OptimizedForAscii));
    }

    public static class Bean3 {
        public List<String> values = new ArrayList<>();
    }

    @Test
    public void ofPrettyUTF16() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16();
        jsonWriter = JSONWriter.ofPretty(jsonWriter);
        jsonWriter = JSONWriter.ofPretty(jsonWriter);
        jsonWriter.startObject();
        jsonWriter.writeNameValue("id", 123);
        jsonWriter.endObject();
        jsonWriter.close();

        assertEquals("{\n" +
                "\t\"id\":123\n" +
                "}", jsonWriter.toString());

        jsonWriter.incrementIndent();
        assertEquals(1, jsonWriter.level());
        jsonWriter.println();
        assertEquals("{\n" +
                "\t\"id\":123\n" +
                "}\n\t", jsonWriter.toString());
        jsonWriter.decrementIdent();
        assertEquals(0, jsonWriter.level());
    }

    @Test
    public void ofPrettyUTF8() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter = JSONWriter.ofPretty(jsonWriter);
        jsonWriter = JSONWriter.ofPretty(jsonWriter);
        jsonWriter.startObject();
        jsonWriter.writeNameValue("id", 123);
        jsonWriter.endObject();
        jsonWriter.close();

        assertEquals("{\n" +
                "\t\"id\":123\n" +
                "}", jsonWriter.toString());

        jsonWriter.incrementIndent();
        assertEquals(1, jsonWriter.level());
        jsonWriter.println();
        assertEquals("{\n" +
                "\t\"id\":123\n" +
                "}\n\t", jsonWriter.toString());
        jsonWriter.decrementIdent();
        assertEquals(0, jsonWriter.level());
    }

    @Test
    public void testJSONObject() {
        {
            JSONObject object = JSONObject.of(
                    "f0", null,
                    "f1", 101,
                    "f2", 102L,
                    "f3", new BigDecimal("103"),
                    "f4", new JSONObject(),
                    "f5", new JSONArray(),
                    "f6", (short) 106,
                    "f7", true
            );
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
                jsonWriter.write(object);
                assertEquals("{\n" +
                        "\t\"f1\":101,\n" +
                        "\t\"f2\":102,\n" +
                        "\t\"f3\":103,\n" +
                        "\t\"f4\":{},\n" +
                        "\t\"f5\":[],\n" +
                        "\t\"f6\":106,\n" +
                        "\t\"f7\":true\n" +
                        "}", jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
                jsonWriter.write(object);
                assertEquals("{\n" +
                        "\t\"f1\":101,\n" +
                        "\t\"f2\":102,\n" +
                        "\t\"f3\":103,\n" +
                        "\t\"f4\":{},\n" +
                        "\t\"f5\":[],\n" +
                        "\t\"f6\":106,\n" +
                        "\t\"f7\":true\n" +
                        "}", jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of(PrettyFormat, WriteNulls);
                jsonWriter.write(object);
                assertEquals("{\n" +
                        "\t\"f0\":null,\n" +
                        "\t\"f1\":101,\n" +
                        "\t\"f2\":102,\n" +
                        "\t\"f3\":103,\n" +
                        "\t\"f4\":{},\n" +
                        "\t\"f5\":[],\n" +
                        "\t\"f6\":106,\n" +
                        "\t\"f7\":true\n" +
                        "}", jsonWriter.toString());
            }
        }
        {
            JSONWriter jsonWriter = JSONWriter.of(PrettyFormat);
            jsonWriter.write(JSONObject.of());
            assertEquals("{}", jsonWriter.toString());
        }
    }

    private static JSONWriter.Context createInlineArraysContext(JSONWriter.Feature... features) {
        JSONWriter.Context context = new JSONWriter.Context(features);
        context.config(PrettyFormat);
        context.setPrettyFormatInlineArrays(true);
        return context;
    }

    @Test
    public void testInlineArrays() {
        // Test simple array with inline formatting - no spaces after commas
        JSONArray array = JSONArray.of(1, 2, 3);
        JSONWriter.Context context = createInlineArraysContext();
        assertEquals("[1,2,3]", JSON.toJSONString(array, context));

        JSONWriter.Context contextAscii = createInlineArraysContext(OptimizedForAscii);
        assertEquals("[1,2,3]", JSON.toJSONString(array, contextAscii));
    }

    @Test
    public void testInlineArraysWithObject() {
        // Test object containing array - object should still be pretty printed, array inline
        JSONObject object = JSONObject.of("id", 123, "values", JSONArray.of(1, 2, 3));
        JSONWriter.Context context = createInlineArraysContext();
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"values\":[1,2,3]\n" +
                        "}",
                JSON.toJSONString(object, context));

        JSONWriter.Context contextAscii = createInlineArraysContext(OptimizedForAscii);
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"values\":[1,2,3]\n" +
                        "}",
                JSON.toJSONString(object, contextAscii));
    }

    @Test
    public void testInlineArraysWithNestedObjects() {
        // Test array of objects - array inline but objects inside still get pretty-printed
        JSONObject obj1 = JSONObject.of("a", 1);
        JSONObject obj2 = JSONObject.of("b", 2);
        JSONArray array = JSONArray.of(obj1, obj2);

        // Array elements separated by comma (no space), objects still pretty-printed
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(array, context);
        assertEquals("[{\n\t\t\"a\":1\n\t},{\n\t\t\"b\":2\n\t}]", result);
    }

    @Test
    public void testInlineArraysUTF8() {
        JSONArray array = JSONArray.of(0, 45732);
        JSONWriter.Context context = createInlineArraysContext(OptimizedForAscii);
        JSONWriter jsonWriter = JSONWriter.ofUTF8(context);
        jsonWriter.write(array);
        assertEquals("[0,45732]", jsonWriter.toString());
    }

    @Test
    public void testInlineArraysUTF16() {
        JSONArray array = JSONArray.of(0, 45732);
        JSONWriter.Context context = createInlineArraysContext();
        // JSONWriter.of(context) creates UTF16 writer when OptimizedForAscii is not set
        JSONWriter jsonWriter = JSONWriter.of(context);
        jsonWriter.write(array);
        assertEquals("[0,45732]", jsonWriter.toString());
    }

    @Test
    public void testInlineArraysWithStrings() {
        JSONArray array = JSONArray.of("a", "b", "c");
        JSONWriter.Context context = createInlineArraysContext();
        assertEquals("[\"a\",\"b\",\"c\"]", JSON.toJSONString(array, context));
    }

    @Test
    public void testInlineArraysIssue2972() {
        // This test is based on issue #2972 - user wants array on single line like original fastjson
        JSONObject jsonObject = JSONObject.of("job_id", JSONArray.of(0, 45732));
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(jsonObject, context);
        assertEquals(
                "{\n" +
                        "\t\"job_id\":[0,45732]\n" +
                        "}",
                result);
    }

    @Test
    public void testInlineArraysIssue2972FullExample() {
        // Full example from issue #2972
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", 1001);
        jsonObject.put("job_id", JSONArray.of(0, 45732));
        jsonObject.put("id", JSONArray.of(1297059520L, 4117498193L));
        jsonObject.put("is_client", false);
        jsonObject.put("stream_count", 0);

        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(jsonObject, context);
        // Verify arrays are inline
        assertTrue(result.contains("\"job_id\":[0,45732]"));
        assertTrue(result.contains("\"id\":[1297059520,4117498193]"));
        // Verify object properties are still on separate lines
        assertTrue(result.contains("\n"));
    }

    @Test
    public void testInlineArraysEmpty() {
        // Empty array
        JSONArray array = new JSONArray();
        JSONWriter.Context context = createInlineArraysContext();
        assertEquals("[]", JSON.toJSONString(array, context));
    }

    @Test
    public void testInlineArraysSingleElement() {
        // Single element array
        JSONArray array = JSONArray.of(42);
        JSONWriter.Context context = createInlineArraysContext();
        assertEquals("[42]", JSON.toJSONString(array, context));
    }

    @Test
    public void testInlineArraysNestedArrays() {
        // Nested arrays - both should be inline
        JSONArray inner1 = JSONArray.of(1, 2);
        JSONArray inner2 = JSONArray.of(3, 4);
        JSONArray outer = JSONArray.of(inner1, inner2);
        JSONWriter.Context context = createInlineArraysContext();
        assertEquals("[[1,2],[3,4]]", JSON.toJSONString(outer, context));
    }

    @Test
    public void testInlineArraysDeeplyNested() {
        // Deeply nested structure
        JSONArray deepArray = JSONArray.of(1, 2, 3);
        JSONObject innerObj = JSONObject.of("arr", deepArray);
        JSONArray outerArray = JSONArray.of(innerObj);
        JSONObject root = JSONObject.of("data", outerArray);

        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(root, context);
        // Arrays should be inline, objects should be pretty-printed
        assertTrue(result.contains("[1,2,3]"));
    }

    @Test
    public void testInlineArraysMixedTypes() {
        // Array with mixed types
        JSONArray array = JSONArray.of(1, "two", true, null, 3.14);
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(array, context);
        assertEquals("[1,\"two\",true,null,3.14]", result);
    }

    @Test
    public void testInlineArraysWithNulls() {
        // Array containing nulls
        JSONArray array = new JSONArray();
        array.add(null);
        array.add(1);
        array.add(null);
        JSONWriter.Context context = createInlineArraysContext();
        assertEquals("[null,1,null]", JSON.toJSONString(array, context));
    }

    @Test
    public void testInlineArraysMultipleArraysInObject() {
        // Object with multiple arrays
        JSONObject obj = new JSONObject();
        obj.put("arr1", JSONArray.of(1, 2));
        obj.put("arr2", JSONArray.of(3, 4));
        obj.put("value", 100);

        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(obj, context);
        assertTrue(result.contains("\"arr1\":[1,2]"));
        assertTrue(result.contains("\"arr2\":[3,4]"));
    }

    @Test
    public void testInlineArraysPreservesObjectFormatting() {
        // Ensure object formatting is not affected
        JSONObject obj = JSONObject.of("a", 1, "b", 2, "c", 3);
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(obj, context);
        // Object should still have newlines between properties
        assertEquals("{\n\t\"a\":1,\n\t\"b\":2,\n\t\"c\":3\n}", result);
    }

    @Test
    public void testInlineArraysWithPrettyFormat2Space() {
        // Test with 2-space indentation
        JSONObject obj = JSONObject.of("arr", JSONArray.of(1, 2, 3));
        JSONWriter.Context context = new JSONWriter.Context(PrettyFormatWith2Space);
        context.setPrettyFormatInlineArrays(true);
        String result = JSON.toJSONString(obj, context);
        assertEquals("{\n  \"arr\":[1,2,3]\n}", result);
    }

    @Test
    public void testInlineArraysWithPrettyFormat4Space() {
        // Test with 4-space indentation
        JSONObject obj = JSONObject.of("arr", JSONArray.of(1, 2, 3));
        JSONWriter.Context context = new JSONWriter.Context(PrettyFormatWith4Space);
        context.setPrettyFormatInlineArrays(true);
        String result = JSON.toJSONString(obj, context);
        assertEquals("{\n    \"arr\":[1,2,3]\n}", result);
    }

    @Test
    public void testInlineArraysDoesNotAffectNonPretty() {
        // Without PrettyFormat, prettyFormatInlineArrays should have no effect
        JSONArray array = JSONArray.of(1, 2, 3);
        JSONWriter.Context context = new JSONWriter.Context();
        context.setPrettyFormatInlineArrays(true); // No PrettyFormat enabled
        assertEquals("[1,2,3]", JSON.toJSONString(array, context));
        assertEquals("[1,2,3]", array.toString()); // Same as default
    }

    @Test
    public void testInlineArraysWithEscapedStrings() {
        // Test strings requiring escape sequences
        JSONArray array = JSONArray.of("hello\"world", "back\\slash", "tab\there");
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(array, context);
        assertEquals("[\"hello\\\"world\",\"back\\\\slash\",\"tab\\there\"]", result);
    }

    @Test
    public void testInlineArraysWithUnicode() {
        // Test unicode characters
        JSONArray array = JSONArray.of("你好", "مرحبا", "🎉");
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(array, context);
        assertTrue(result.startsWith("[\""));
        assertTrue(result.endsWith("\"]"));
        // Verify no newlines in the output
        assertEquals(-1, result.indexOf('\n'));
    }

    @Test
    public void testInlineArraysWithJavaArray() {
        // Test with native Java arrays
        int[] intArray = {1, 2, 3, 4, 5};
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(intArray, context);
        assertEquals("[1,2,3,4,5]", result);
    }

    @Test
    public void testInlineArraysWithStringArray() {
        // Test with String array
        String[] strArray = {"a", "b", "c"};
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(strArray, context);
        assertEquals("[\"a\",\"b\",\"c\"]", result);
    }

    @Test
    public void testInlineArraysWithBeanList() {
        // Test with bean containing a list
        Bean3 bean = new Bean3();
        bean.values.add("item1");
        bean.values.add("item2");

        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(bean, context);
        assertTrue(result.contains("\"values\":[\"item1\",\"item2\"]"));
    }

    @Test
    public void testInlineArraysAlternatingNesting() {
        // Test alternating object/array nesting
        // Structure: { arr: [ { nested: [1,2] } ] }
        JSONArray innerArray = JSONArray.of(1, 2);
        JSONObject innerObj = JSONObject.of("nested", innerArray);
        JSONArray outerArray = JSONArray.of(innerObj);
        JSONObject root = JSONObject.of("arr", outerArray);

        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(root, context);
        // Outer array inline, inner array inline, but objects still pretty-printed
        assertTrue(result.contains("[1,2]"));
    }

    @Test
    public void testInlineArraysLargeArray() {
        // Test with larger array to verify buffer handling
        JSONArray array = new JSONArray();
        for (int i = 0; i < 100; i++) {
            array.add(i);
        }
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(array, context);
        // Should be all on one line
        assertEquals(-1, result.indexOf('\n'));
        assertTrue(result.startsWith("[0,1,2,"));
        assertTrue(result.endsWith(",98,99]"));
    }

    @Test
    public void testInlineArraysEmptyNested() {
        // Test nested empty arrays
        JSONArray inner1 = new JSONArray();
        JSONArray inner2 = new JSONArray();
        JSONArray outer = JSONArray.of(inner1, inner2);
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(outer, context);
        assertEquals("[[],[]]", result);
    }

    @Test
    public void testInlineArraysObjectWithEmptyArray() {
        // Object containing empty array
        JSONObject obj = JSONObject.of("empty", new JSONArray(), "value", 42);
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(obj, context);
        assertTrue(result.contains("\"empty\":[]"));
        assertTrue(result.contains("\"value\":42"));
    }

    @Test
    public void testInlineArraysWithBigNumbers() {
        // Test with BigDecimal and BigInteger
        JSONArray array = new JSONArray();
        array.add(new BigDecimal("123456789.123456789"));
        array.add(new java.math.BigInteger("999999999999999999999"));
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(array, context);
        assertEquals("[123456789.123456789,999999999999999999999]", result);
    }

    @Test
    public void testInlineArraysComplexNesting() {
        // Complex nesting: obj -> arr -> obj -> arr -> values
        JSONArray deepArray = JSONArray.of("a", "b");
        JSONObject innerObj = JSONObject.of("deep", deepArray);
        JSONArray midArray = JSONArray.of(innerObj);
        JSONObject root = JSONObject.of("mid", midArray);

        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(root, context);
        // All arrays should be inline
        assertTrue(result.contains("[\"a\",\"b\"]"));
    }

    @Test
    public void testInlineArraysWithFloatingPoint() {
        // Test with various floating point values
        JSONArray array = JSONArray.of(1.0, 2.5, 3.14159, 0.0, -1.5);
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(array, context);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertEquals(-1, result.indexOf('\n'));
    }

    @Test
    public void testInlineArraysBothWriters() {
        // Explicitly test both UTF8 and UTF16 writers with same input
        JSONObject obj = JSONObject.of("data", JSONArray.of(1, 2, 3));

        // UTF8
        JSONWriter.Context utf8Context = createInlineArraysContext(OptimizedForAscii);
        JSONWriter utf8Writer = JSONWriter.ofUTF8(utf8Context);
        utf8Writer.write(obj);
        String utf8Result = utf8Writer.toString();

        // UTF16 - JSONWriter.of(context) creates UTF16 writer when OptimizedForAscii is not set
        JSONWriter.Context utf16Context = createInlineArraysContext();
        JSONWriter utf16Writer = JSONWriter.of(utf16Context);
        utf16Writer.write(obj);
        String utf16Result = utf16Writer.toString();

        // Both should produce identical results
        assertEquals(utf8Result, utf16Result);
        assertTrue(utf8Result.contains("\"data\":[1,2,3]"));
    }

    @Test
    public void testInlineArraysTripleNested() {
        // Triple nested arrays
        JSONArray level3 = JSONArray.of(1);
        JSONArray level2 = JSONArray.of(level3);
        JSONArray level1 = JSONArray.of(level2);
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(level1, context);
        assertEquals("[[[1]]]", result);
    }

    @Test
    public void testInlineArraysObjectInArrayInObject() {
        // Object containing array containing object
        JSONObject inner = JSONObject.of("x", 1);
        JSONArray arr = JSONArray.of(inner);
        JSONObject outer = JSONObject.of("items", arr);

        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(outer, context);
        // Object should be pretty, array inline, inner object pretty
        assertTrue(result.contains("\"items\":["));
        assertTrue(result.contains("\"x\":1"));
    }

    @Test
    public void testInlineArraysDeeplyNestedArrays() {
        // Test deeply nested arrays (up to a reasonable depth)
        // This tests the levelArray bitmask at multiple levels
        JSONArray current = JSONArray.of(1);
        for (int i = 0; i < 20; i++) {
            current = JSONArray.of(current);
        }
        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(current, context);
        // Should have 21 opening brackets, all on one line
        assertEquals(-1, result.indexOf('\n'));
        assertTrue(result.startsWith("[[[[["));
        assertTrue(result.endsWith("]]]]]"));
    }

    @Test
    public void testInlineArraysAlternatingDeepNesting() {
        // Test alternating arrays and objects deeply nested
        // { arr: [ { arr: [ { arr: [1] } ] } ] }
        Object current = JSONArray.of(1);
        for (int i = 0; i < 5; i++) {
            JSONObject obj = JSONObject.of("arr", current);
            current = JSONArray.of(obj);
        }
        JSONObject root = JSONObject.of("root", current);

        JSONWriter.Context context = createInlineArraysContext();
        String result = JSON.toJSONString(root, context);
        // Arrays should be inline, verify deepest array is inline
        assertTrue(result.contains("[1]"));
    }

    @Test
    public void testInlineArraysPreservesExistingBehavior() {
        // Verify that without prettyFormatInlineArrays, behavior is unchanged
        JSONObject obj = JSONObject.of("arr", JSONArray.of(1, 2, 3));

        String withoutInline = obj.toString(PrettyFormat);
        JSONWriter.Context context = createInlineArraysContext();
        String withInline = JSON.toJSONString(obj, context);

        // Without inline: array elements on separate lines
        assertTrue(withoutInline.contains("[\n"));
        // With inline: array on single line
        assertTrue(withInline.contains("[1,2,3]"));
    }

    @Test
    public void testInlineArraysContextGetter() {
        // Test the getter method
        JSONWriter.Context context = new JSONWriter.Context(PrettyFormat);
        assertFalse(context.isPrettyFormatInlineArrays());

        context.setPrettyFormatInlineArrays(true);
        assertTrue(context.isPrettyFormatInlineArrays());
    }

    @Test
    public void testInlineArraysDefaultFactory() {
        // Test using JSONFactory default
        boolean originalDefault = JSONFactory.isDefaultWriterPrettyFormatInlineArrays();
        try {
            // Set default to true
            JSONFactory.setDefaultWriterPrettyFormatInlineArrays(true);

            // New context should have inline arrays enabled
            JSONWriter.Context context = new JSONWriter.Context(PrettyFormat);
            assertTrue(context.isPrettyFormatInlineArrays());

            // Verify it works
            JSONArray array = JSONArray.of(1, 2, 3);
            assertEquals("[1,2,3]", JSON.toJSONString(array, context));
        } finally {
            // Restore original default
            JSONFactory.setDefaultWriterPrettyFormatInlineArrays(originalDefault);
        }
    }

    @Test
    public void testInlineArraysDefaultFactoryWithPrettyFormat() {
        // Test that default works with pretty format
        boolean originalDefault = JSONFactory.isDefaultWriterPrettyFormatInlineArrays();
        try {
            JSONFactory.setDefaultWriterPrettyFormatInlineArrays(true);

            JSONObject obj = JSONObject.of("data", JSONArray.of(1, 2, 3));
            JSONWriter.Context context = new JSONWriter.Context(PrettyFormat);
            String result = JSON.toJSONString(obj, context);

            assertTrue(result.contains("\"data\":[1,2,3]"));
        } finally {
            JSONFactory.setDefaultWriterPrettyFormatInlineArrays(originalDefault);
        }
    }
}
