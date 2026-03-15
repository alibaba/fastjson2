package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for typed multi-path JSONPath extraction.
 */
public class JSONPathTypedMultiTest {
    // ==================== ALL_SINGLE_NAME strategy ====================

    @Test
    public void testSingleNames_basic() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.name", "$.score"},
                new Type[]{Long.class, String.class, BigDecimal.class}
        );

        Object[] result = (Object[]) path.extract("{\"id\":1,\"name\":\"test\",\"score\":99.5}");
        assertEquals(1L, result[0]);
        assertEquals("test", result[1]);
        assertEquals(new BigDecimal("99.5"), result[2]);
    }

    @Test
    public void testSingleNames_fieldOrderDifferent() {
        JSONPath path = JSONPath.of(
                new String[]{"$.name", "$.id"},
                new Type[]{String.class, Long.class}
        );

        // JSON field order is reversed compared to path order
        Object[] result = (Object[]) path.extract("{\"id\":42,\"name\":\"hello\"}");
        assertEquals("hello", result[0]);
        assertEquals(42L, result[1]);
    }

    @Test
    public void testSingleNames_missingField() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.missing"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{\"id\":1}");
        assertEquals(1L, result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testSingleNames_nullValue() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{\"id\":1,\"name\":null}");
        assertEquals(1L, result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testSingleNames_earlyTermination() {
        // All fields found before object ends - should terminate early
        JSONPath path = JSONPath.of(
                new String[]{"$.a"},
                new Type[]{Integer.class}
        );

        Object[] result = (Object[]) path.extract(
                "{\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5,\"f\":6,\"g\":7,\"h\":8}"
        );
        assertEquals(1, result[0]);
    }

    @Test
    public void testSingleNames_duplicatePath() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.id"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{\"id\":42}");
        assertEquals(42L, result[0]);
        assertEquals("42", result[1]);
    }

    @Test
    public void testSingleNames_duplicateJsonKeys() {
        // Duplicate keys in JSON should not cause early termination
        JSONPath path = JSONPath.of(
                new String[]{"$.a", "$.b"},
                new Type[]{Integer.class, Integer.class}
        );

        // "a" appears twice before "b" — must still find "b"
        Object[] result = (Object[]) path.extract("{\"a\":1,\"a\":2,\"b\":3}");
        assertEquals(2, result[0]); // last occurrence wins
        assertEquals(3, result[1]); // must not be skipped
    }

    @Test
    public void testSingleNames_treeMode() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.name"},
                new Type[]{Long.class, String.class}
        );

        JSONObject obj = new JSONObject();
        obj.put("id", 1);
        obj.put("name", "test");

        Object[] result = (Object[]) path.eval(obj);
        assertEquals(1L, result[0]);
        assertEquals("test", result[1]);
    }

    @Test
    public void testSingleNames_typeConversion() {
        JSONPath path = JSONPath.of(
                new String[]{"$.intVal", "$.strVal", "$.boolStr"},
                new Type[]{String.class, Integer.class, Boolean.class}
        );

        Object[] result = (Object[]) path.extract("{\"intVal\":123,\"strVal\":\"456\",\"boolStr\":\"true\"}");
        assertEquals("123", result[0]);
        assertEquals(456, result[1]);
        assertEquals(true, result[2]);
    }

    @Test
    public void testSingleNames_shortAndByte() {
        JSONPath path = JSONPath.of(
                new String[]{"$.s", "$.b"},
                new Type[]{Short.class, Byte.class}
        );

        Object[] result = (Object[]) path.extract("{\"s\":32000,\"b\":127}");
        assertEquals((short) 32000, result[0]);
        assertEquals((byte) 127, result[1]);
    }

    @Test
    public void testSingleNames_floatType() {
        JSONPath path = JSONPath.of(
                new String[]{"$.f"},
                new Type[]{Float.class}
        );

        Object[] result = (Object[]) path.extract("{\"f\":3.14}");
        assertInstanceOf(Float.class, result[0]);
        assertEquals(3.14f, (Float) result[0], 0.001f);
    }

    // ==================== ALL_SINGLE_INDEX strategy ====================

    @Test
    public void testSingleIndexes_basic() {
        JSONPath path = JSONPath.of(
                new String[]{"$[0]", "$[2]"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("[101, \"skip\", \"hello\"]");
        assertEquals(101L, result[0]);
        assertEquals("hello", result[1]);
    }

    @Test
    public void testSingleIndexes_arrayTooShort() {
        JSONPath path = JSONPath.of(
                new String[]{"$[0]", "$[5]"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("[1, 2, 3]");
        assertEquals(1L, result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testSingleIndexes_duplicateIndex() {
        JSONPath path = JSONPath.of(
                new String[]{"$[1]", "$[1]"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("[0, 42]");
        assertEquals(42L, result[0]);
        assertEquals("42", result[1]);
    }

    @Test
    public void testSingleIndexes_treeMode() {
        JSONPath path = JSONPath.of(
                new String[]{"$[0]", "$[2]"},
                new Type[]{Integer.class, String.class}
        );

        JSONArray arr = new JSONArray();
        arr.add(10);
        arr.add("skip");
        arr.add("value");

        Object[] result = (Object[]) path.eval(arr);
        assertEquals(10, result[0]);
        assertEquals("value", result[1]);
    }

    // ==================== PREFIX_NAME_THEN_NAMES strategy ====================

    @Test
    public void testPrefixNameThenNames_basic() {
        JSONPath path = JSONPath.of(
                new String[]{"$.data.id", "$.data.name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{\"data\":{\"id\":1,\"name\":\"test\"}}");
        assertEquals(1L, result[0]);
        assertEquals("test", result[1]);
    }

    @Test
    public void testPrefixNameThenNames_prefixNotFound() {
        JSONPath path = JSONPath.of(
                new String[]{"$.data.id", "$.data.name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{\"other\":{\"id\":1}}");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testPrefixNameThenNames_prefixNull() {
        JSONPath path = JSONPath.of(
                new String[]{"$.data.id", "$.data.name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{\"data\":null}");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testPrefixNameThenNames_treeMode() {
        JSONPath path = JSONPath.of(
                new String[]{"$.data.id", "$.data.name"},
                new Type[]{Long.class, String.class}
        );

        JSONObject inner = new JSONObject();
        inner.put("id", 1);
        inner.put("name", "test");
        JSONObject root = new JSONObject();
        root.put("data", inner);

        Object[] result = (Object[]) path.eval(root);
        assertEquals(1L, result[0]);
        assertEquals("test", result[1]);
    }

    // ==================== PREFIX_INDEX_THEN_NAMES strategy ====================

    @Test
    public void testPrefixIndexThenNames_basic() {
        JSONPath path = JSONPath.of(
                new String[]{"$[0].id", "$[0].name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("[{\"id\":1,\"name\":\"first\"},{\"id\":2}]");
        assertEquals(1L, result[0]);
        assertEquals("first", result[1]);
    }

    @Test
    public void testPrefixIndexThenNames_indexOutOfBounds() {
        JSONPath path = JSONPath.of(
                new String[]{"$[5].id", "$[5].name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("[{\"id\":1}]");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    // ==================== PREFIX_NAME2_THEN_NAMES strategy ====================

    @Test
    public void testPrefixName2ThenNames_basic() {
        JSONPath path = JSONPath.of(
                new String[]{"$.data.info.id", "$.data.info.name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract(
                "{\"data\":{\"info\":{\"id\":1,\"name\":\"deep\"}}}"
        );
        assertEquals(1L, result[0]);
        assertEquals("deep", result[1]);
    }

    @Test
    public void testPrefixName2ThenNames_firstPrefixNotFound() {
        JSONPath path = JSONPath.of(
                new String[]{"$.data.info.id", "$.data.info.name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{\"other\":{}}");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testPrefixName2ThenNames_secondPrefixNotFound() {
        JSONPath path = JSONPath.of(
                new String[]{"$.data.info.id", "$.data.info.name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{\"data\":{\"other\":{}}}");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    // ==================== GENERIC strategy ====================

    @Test
    public void testGeneric_mixedPathStructures() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$[0]"},
                new Type[]{Long.class, String.class}
        );

        // These paths have different structures, so GENERIC strategy is used
        // GENERIC falls back to tree mode eval per path
        Object[] result = (Object[]) path.extract("{\"id\":1}");
        assertEquals(1L, result[0]);
        // $[0] on an object returns null
        assertNull(result[1]);
    }

    // ==================== Byte array extraction ====================

    @Test
    public void testExtract_byteArray() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.name"},
                new Type[]{Long.class, String.class}
        );

        byte[] json = "{\"id\":1,\"name\":\"test\"}".getBytes();
        try (JSONParser parser = JSONParser.of(json)) {
            Object[] result = (Object[]) path.extract(parser);
            assertEquals(1L, result[0]);
            assertEquals("test", result[1]);
        }
    }

    // ==================== Edge cases ====================

    @Test
    public void testNullRoot_treeMode() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id"},
                new Type[]{Long.class}
        );

        Object[] result = (Object[]) path.eval(null);
        assertNull(result[0]);
    }

    @Test
    public void testEmptyObject() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.name"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("{}");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testEmptyArray() {
        JSONPath path = JSONPath.of(
                new String[]{"$[0]", "$[1]"},
                new Type[]{Long.class, String.class}
        );

        Object[] result = (Object[]) path.extract("[]");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testFactory_nullPaths() {
        assertThrows(JSONException.class, () ->
                JSONPath.of(null, new Type[]{Long.class})
        );
    }

    @Test
    public void testFactory_nullTypes() {
        assertThrows(JSONException.class, () ->
                JSONPath.of(new String[]{"$.id"}, null)
        );
    }

    @Test
    public void testFactory_emptyPaths() {
        assertThrows(JSONException.class, () ->
                JSONPath.of(new String[]{}, new Type[]{})
        );
    }

    @Test
    public void testFactory_lengthMismatch() {
        assertThrows(JSONException.class, () ->
                JSONPath.of(new String[]{"$.id", "$.name"}, new Type[]{Long.class})
        );
    }

    @Test
    public void testIsDefinite() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id"},
                new Type[]{Long.class}
        );
        assertTrue(path.isDefinite());
    }

    // ==================== Complex type scenarios ====================

    @Test
    public void testNestedObjectValue() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.detail"},
                new Type[]{Long.class, Object.class}
        );

        Object[] result = (Object[]) path.extract("{\"id\":1,\"detail\":{\"x\":1,\"y\":2}}");
        assertEquals(1L, result[0]);
        assertInstanceOf(JSONObject.class, result[1]);
    }

    @Test
    public void testArrayValue() {
        JSONPath path = JSONPath.of(
                new String[]{"$.id", "$.tags"},
                new Type[]{Long.class, Object.class}
        );

        Object[] result = (Object[]) path.extract("{\"id\":1,\"tags\":[\"a\",\"b\"]}");
        assertEquals(1L, result[0]);
        assertInstanceOf(JSONArray.class, result[1]);
    }

    @Test
    public void testBooleanAndNullTypes() {
        JSONPath path = JSONPath.of(
                new String[]{"$.flag", "$.nothing"},
                new Type[]{Boolean.class, Object.class}
        );

        Object[] result = (Object[]) path.extract("{\"flag\":true,\"nothing\":null}");
        assertEquals(true, result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testManyFields_streamPerformance() {
        // Test with many irrelevant fields to validate stream extraction efficiency
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"field").append(i).append("\":").append(i);
        }
        sb.append(",\"target\":999}");

        JSONPath path = JSONPath.of(
                new String[]{"$.target"},
                new Type[]{Integer.class}
        );

        Object[] result = (Object[]) path.extract(sb.toString());
        assertEquals(999, result[0]);
    }
}
