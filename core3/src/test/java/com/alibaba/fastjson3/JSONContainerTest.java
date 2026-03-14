package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSONObject and JSONArray typed accessor methods.
 * Covers all branches: null, direct type, Number conversion,
 * String conversion, empty string, and error paths.
 */
class JSONContainerTest {
    // ==================== JSONObject ====================

    @Test
    void jsonObjectFluentPut() {
        JSONObject obj = new JSONObject()
                .fluentPut("a", 1)
                .fluentPut("b", "two");
        assertEquals(1, obj.get("a"));
        assertEquals("two", obj.get("b"));
    }

    // ---- getString ----
    @Test
    void getStringDirect() {
        JSONObject obj = new JSONObject();
        obj.put("k", "hello");
        assertEquals("hello", obj.getString("k"));
    }

    @Test
    void getStringFromNonString() {
        JSONObject obj = new JSONObject();
        obj.put("k", 42);
        assertEquals("42", obj.getString("k"));
    }

    @Test
    void getStringNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getString("missing"));
    }

    // ---- getInteger / getIntValue ----
    @Test
    void getIntegerDirect() {
        JSONObject obj = new JSONObject();
        obj.put("k", 42);
        assertEquals(42, obj.getInteger("k"));
    }

    @Test
    void getIntegerFromNumber() {
        JSONObject obj = new JSONObject();
        obj.put("k", 42L);
        assertEquals(42, obj.getInteger("k"));
    }

    @Test
    void getIntegerFromString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "123");
        assertEquals(123, obj.getInteger("k"));
    }

    @Test
    void getIntegerFromEmptyString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "");
        assertNull(obj.getInteger("k"));
    }

    @Test
    void getIntegerNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getInteger("missing"));
    }

    @Test
    void getIntegerThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", new Object());
        assertThrows(JSONException.class, () -> obj.getInteger("k"));
    }

    @Test
    void getIntValueDefault() {
        JSONObject obj = new JSONObject();
        assertEquals(0, obj.getIntValue("missing"));
    }

    // ---- getLong / getLongValue ----
    @Test
    void getLongDirect() {
        JSONObject obj = new JSONObject();
        obj.put("k", 100L);
        assertEquals(100L, obj.getLong("k"));
    }

    @Test
    void getLongFromNumber() {
        JSONObject obj = new JSONObject();
        obj.put("k", 42);
        assertEquals(42L, obj.getLong("k"));
    }

    @Test
    void getLongFromString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "999");
        assertEquals(999L, obj.getLong("k"));
    }

    @Test
    void getLongFromEmptyString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "");
        assertNull(obj.getLong("k"));
    }

    @Test
    void getLongNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getLong("missing"));
    }

    @Test
    void getLongThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", new Object());
        assertThrows(JSONException.class, () -> obj.getLong("k"));
    }

    @Test
    void getLongValueDefault() {
        JSONObject obj = new JSONObject();
        assertEquals(0L, obj.getLongValue("missing"));
    }

    // ---- getDouble / getDoubleValue ----
    @Test
    void getDoubleDirect() {
        JSONObject obj = new JSONObject();
        obj.put("k", 3.14);
        assertEquals(3.14, obj.getDouble("k"));
    }

    @Test
    void getDoubleFromNumber() {
        JSONObject obj = new JSONObject();
        obj.put("k", 42);
        assertEquals(42.0, obj.getDouble("k"));
    }

    @Test
    void getDoubleFromString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "1.5");
        assertEquals(1.5, obj.getDouble("k"));
    }

    @Test
    void getDoubleFromEmptyString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "");
        assertNull(obj.getDouble("k"));
    }

    @Test
    void getDoubleNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getDouble("missing"));
    }

    @Test
    void getDoubleThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", new Object());
        assertThrows(JSONException.class, () -> obj.getDouble("k"));
    }

    @Test
    void getDoubleValueDefault() {
        JSONObject obj = new JSONObject();
        assertEquals(0.0, obj.getDoubleValue("missing"));
    }

    // ---- getFloat / getFloatValue ----
    @Test
    void getFloatDirect() {
        JSONObject obj = new JSONObject();
        obj.put("k", 1.5f);
        assertEquals(1.5f, obj.getFloat("k"));
    }

    @Test
    void getFloatFromNumber() {
        JSONObject obj = new JSONObject();
        obj.put("k", 42);
        assertEquals(42.0f, obj.getFloat("k"));
    }

    @Test
    void getFloatFromString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "2.5");
        assertEquals(2.5f, obj.getFloat("k"));
    }

    @Test
    void getFloatFromEmptyString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "");
        assertNull(obj.getFloat("k"));
    }

    @Test
    void getFloatNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getFloat("missing"));
    }

    @Test
    void getFloatThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", new Object());
        assertThrows(JSONException.class, () -> obj.getFloat("k"));
    }

    @Test
    void getFloatValueDefault() {
        JSONObject obj = new JSONObject();
        assertEquals(0.0f, obj.getFloatValue("missing"));
    }

    // ---- getBoolean / getBooleanValue ----
    @Test
    void getBooleanDirect() {
        JSONObject obj = new JSONObject();
        obj.put("k", true);
        assertTrue(obj.getBoolean("k"));
    }

    @Test
    void getBooleanFromString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "TRUE");
        assertTrue(obj.getBoolean("k"));
    }

    @Test
    void getBooleanFromEmptyString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "");
        assertNull(obj.getBoolean("k"));
    }

    @Test
    void getBooleanFromNumber() {
        JSONObject obj = new JSONObject();
        obj.put("k", 1);
        assertTrue(obj.getBoolean("k"));
        obj.put("k", 0);
        assertFalse(obj.getBoolean("k"));
    }

    @Test
    void getBooleanNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getBoolean("missing"));
    }

    @Test
    void getBooleanThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", new Object());
        assertThrows(JSONException.class, () -> obj.getBoolean("k"));
    }

    @Test
    void getBooleanValueDefault() {
        JSONObject obj = new JSONObject();
        assertFalse(obj.getBooleanValue("missing"));
    }

    // ---- getBigDecimal ----
    @Test
    void getBigDecimalDirect() {
        JSONObject obj = new JSONObject();
        obj.put("k", new BigDecimal("1.23"));
        assertEquals(new BigDecimal("1.23"), obj.getBigDecimal("k"));
    }

    @Test
    void getBigDecimalFromBigInteger() {
        JSONObject obj = new JSONObject();
        obj.put("k", BigInteger.TEN);
        assertEquals(new BigDecimal(BigInteger.TEN), obj.getBigDecimal("k"));
    }

    @Test
    void getBigDecimalFromNumber() {
        JSONObject obj = new JSONObject();
        obj.put("k", 3.14);
        assertNotNull(obj.getBigDecimal("k"));
    }

    @Test
    void getBigDecimalFromString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "99.99");
        assertEquals(new BigDecimal("99.99"), obj.getBigDecimal("k"));
    }

    @Test
    void getBigDecimalFromEmptyString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "");
        assertNull(obj.getBigDecimal("k"));
    }

    @Test
    void getBigDecimalNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getBigDecimal("missing"));
    }

    @Test
    void getBigDecimalThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", new Object());
        assertThrows(JSONException.class, () -> obj.getBigDecimal("k"));
    }

    // ---- getBigInteger ----
    @Test
    void getBigIntegerDirect() {
        JSONObject obj = new JSONObject();
        obj.put("k", BigInteger.TEN);
        assertEquals(BigInteger.TEN, obj.getBigInteger("k"));
    }

    @Test
    void getBigIntegerFromBigDecimal() {
        JSONObject obj = new JSONObject();
        obj.put("k", new BigDecimal("100"));
        assertEquals(BigInteger.valueOf(100), obj.getBigInteger("k"));
    }

    @Test
    void getBigIntegerFromNumber() {
        JSONObject obj = new JSONObject();
        obj.put("k", 42);
        assertEquals(BigInteger.valueOf(42), obj.getBigInteger("k"));
    }

    @Test
    void getBigIntegerFromString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "999");
        assertEquals(BigInteger.valueOf(999), obj.getBigInteger("k"));
    }

    @Test
    void getBigIntegerFromEmptyString() {
        JSONObject obj = new JSONObject();
        obj.put("k", "");
        assertNull(obj.getBigInteger("k"));
    }

    @Test
    void getBigIntegerNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getBigInteger("missing"));
    }

    @Test
    void getBigIntegerThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", new Object());
        assertThrows(JSONException.class, () -> obj.getBigInteger("k"));
    }

    // ---- getJSONObject / getJSONArray ----
    @Test
    void getJSONObjectDirect() {
        JSONObject obj = new JSONObject();
        JSONObject inner = new JSONObject();
        inner.put("x", 1);
        obj.put("k", inner);
        assertSame(inner, obj.getJSONObject("k"));
    }

    @Test
    void getJSONObjectNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getJSONObject("missing"));
    }

    @Test
    void getJSONObjectThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", "not a map");
        assertThrows(JSONException.class, () -> obj.getJSONObject("k"));
    }

    @Test
    void getJSONArrayDirect() {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        arr.add(1);
        obj.put("k", arr);
        assertSame(arr, obj.getJSONArray("k"));
    }

    @Test
    void getJSONArrayNull() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getJSONArray("missing"));
    }

    @Test
    void getJSONArrayThrowsOnBadType() {
        JSONObject obj = new JSONObject();
        obj.put("k", "not an array");
        assertThrows(JSONException.class, () -> obj.getJSONArray("k"));
    }

    @Test
    void jsonObjectToString() {
        JSONObject obj = new JSONObject();
        obj.put("a", 1);
        String s = obj.toString();
        assertTrue(s.contains("\"a\""));
        assertTrue(s.contains("1"));
    }

    @Test
    void jsonObjectFromMap() {
        java.util.Map<String, Object> map = java.util.Map.of("a", 1);
        JSONObject obj = new JSONObject(map);
        assertEquals(1, obj.getIntValue("a"));
    }

    // ==================== JSONArray ====================

    @Test
    void jsonArrayFluentAdd() {
        JSONArray arr = new JSONArray()
                .fluentAdd("a")
                .fluentAdd(1);
        assertEquals(2, arr.size());
        assertEquals("a", arr.getString(0));
    }

    @Test
    void jsonArrayGetStringDirect() {
        JSONArray arr = new JSONArray();
        arr.add("hello");
        assertEquals("hello", arr.getString(0));
    }

    @Test
    void jsonArrayGetStringFromNonString() {
        JSONArray arr = new JSONArray();
        arr.add(42);
        assertEquals("42", arr.getString(0));
    }

    @Test
    void jsonArrayGetStringNull() {
        JSONArray arr = new JSONArray();
        arr.add(null);
        assertNull(arr.getString(0));
    }

    @Test
    void jsonArrayGetIntegerAllBranches() {
        JSONArray arr = new JSONArray();
        arr.add(42);
        arr.add(42L);
        arr.add("123");
        arr.add("");
        arr.add(null);
        assertEquals(42, arr.getInteger(0));
        assertEquals(42, arr.getInteger(1));
        assertEquals(123, arr.getInteger(2));
        assertNull(arr.getInteger(3));
        assertNull(arr.getInteger(4));
    }

    @Test
    void jsonArrayGetIntegerThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add(new Object());
        assertThrows(JSONException.class, () -> arr.getInteger(0));
    }

    @Test
    void jsonArrayGetIntValueDefault() {
        JSONArray arr = new JSONArray();
        arr.add(null);
        assertEquals(0, arr.getIntValue(0));
    }

    @Test
    void jsonArrayGetLongAllBranches() {
        JSONArray arr = new JSONArray();
        arr.add(100L);
        arr.add(42);
        arr.add("999");
        arr.add("");
        arr.add(null);
        assertEquals(100L, arr.getLong(0));
        assertEquals(42L, arr.getLong(1));
        assertEquals(999L, arr.getLong(2));
        assertNull(arr.getLong(3));
        assertNull(arr.getLong(4));
    }

    @Test
    void jsonArrayGetLongThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add(new Object());
        assertThrows(JSONException.class, () -> arr.getLong(0));
    }

    @Test
    void jsonArrayGetLongValueDefault() {
        JSONArray arr = new JSONArray();
        arr.add(null);
        assertEquals(0L, arr.getLongValue(0));
    }

    @Test
    void jsonArrayGetDoubleAllBranches() {
        JSONArray arr = new JSONArray();
        arr.add(3.14);
        arr.add(42);
        arr.add("1.5");
        arr.add(null);
        assertEquals(3.14, arr.getDouble(0));
        assertEquals(42.0, arr.getDouble(1));
        assertEquals(1.5, arr.getDouble(2));
        assertNull(arr.getDouble(3));
    }

    @Test
    void jsonArrayGetDoubleThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add(new Object());
        assertThrows(JSONException.class, () -> arr.getDouble(0));
    }

    @Test
    void jsonArrayGetDoubleValueDefault() {
        JSONArray arr = new JSONArray();
        arr.add(null);
        assertEquals(0.0, arr.getDoubleValue(0));
    }

    @Test
    void jsonArrayGetFloatAllBranches() {
        JSONArray arr = new JSONArray();
        arr.add(1.5f);
        arr.add(42);
        arr.add("2.5");
        arr.add(null);
        assertEquals(1.5f, arr.getFloat(0));
        assertEquals(42.0f, arr.getFloat(1));
        assertEquals(2.5f, arr.getFloat(2));
        assertNull(arr.getFloat(3));
    }

    @Test
    void jsonArrayGetFloatThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add(new Object());
        assertThrows(JSONException.class, () -> arr.getFloat(0));
    }

    @Test
    void jsonArrayGetFloatValueDefault() {
        JSONArray arr = new JSONArray();
        arr.add(null);
        assertEquals(0.0f, arr.getFloatValue(0));
    }

    @Test
    void jsonArrayGetBooleanAllBranches() {
        JSONArray arr = new JSONArray();
        arr.add(true);
        arr.add("TRUE");
        arr.add(1);
        arr.add(0);
        arr.add(null);
        assertTrue(arr.getBoolean(0));
        assertTrue(arr.getBoolean(1));
        assertTrue(arr.getBoolean(2));
        assertFalse(arr.getBoolean(3));
        assertNull(arr.getBoolean(4));
    }

    @Test
    void jsonArrayGetBooleanThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add(new Object());
        assertThrows(JSONException.class, () -> arr.getBoolean(0));
    }

    @Test
    void jsonArrayGetBooleanValueDefault() {
        JSONArray arr = new JSONArray();
        arr.add(null);
        assertFalse(arr.getBooleanValue(0));
    }

    @Test
    void jsonArrayGetBigDecimalAllBranches() {
        JSONArray arr = new JSONArray();
        arr.add(new BigDecimal("1.23"));
        arr.add(3.14);
        arr.add("99.99");
        arr.add(null);
        assertEquals(new BigDecimal("1.23"), arr.getBigDecimal(0));
        assertNotNull(arr.getBigDecimal(1));
        assertEquals(new BigDecimal("99.99"), arr.getBigDecimal(2));
        assertNull(arr.getBigDecimal(3));
    }

    @Test
    void jsonArrayGetBigDecimalThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add(new Object());
        assertThrows(JSONException.class, () -> arr.getBigDecimal(0));
    }

    @Test
    void jsonArrayGetBigIntegerAllBranches() {
        JSONArray arr = new JSONArray();
        arr.add(BigInteger.TEN);
        arr.add(42);
        arr.add("999");
        arr.add(null);
        assertEquals(BigInteger.TEN, arr.getBigInteger(0));
        assertEquals(BigInteger.valueOf(42), arr.getBigInteger(1));
        assertEquals(BigInteger.valueOf(999), arr.getBigInteger(2));
        assertNull(arr.getBigInteger(3));
    }

    @Test
    void jsonArrayGetBigIntegerThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add(new Object());
        assertThrows(JSONException.class, () -> arr.getBigInteger(0));
    }

    @Test
    void jsonArrayGetJSONObject() {
        JSONArray arr = new JSONArray();
        JSONObject obj = new JSONObject();
        arr.add(obj);
        arr.add(null);
        assertSame(obj, arr.getJSONObject(0));
        assertNull(arr.getJSONObject(1));
    }

    @Test
    void jsonArrayGetJSONObjectThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add("not an object");
        assertThrows(JSONException.class, () -> arr.getJSONObject(0));
    }

    @Test
    void jsonArrayGetJSONArray() {
        JSONArray arr = new JSONArray();
        JSONArray inner = new JSONArray();
        arr.add(inner);
        arr.add(null);
        assertSame(inner, arr.getJSONArray(0));
        assertNull(arr.getJSONArray(1));
    }

    @Test
    void jsonArrayGetJSONArrayThrowsOnBadType() {
        JSONArray arr = new JSONArray();
        arr.add("not an array");
        assertThrows(JSONException.class, () -> arr.getJSONArray(0));
    }

    @Test
    void jsonArrayToString() {
        JSONArray arr = new JSONArray();
        arr.add(1);
        arr.add("two");
        String s = arr.toString();
        assertTrue(s.contains("1"));
        assertTrue(s.contains("\"two\""));
    }

    @Test
    void jsonArrayFromCollection() {
        JSONArray arr = new JSONArray(java.util.List.of(1, 2, 3));
        assertEquals(3, arr.size());
        assertEquals(1, arr.getIntValue(0));
    }

    // ==================== Parse integration ====================

    @Test
    void parseObjectReturnsJSONObject() {
        JSONObject obj = JSON.parseObject("{\"a\":1,\"b\":\"two\"}");
        assertEquals(1, obj.getIntValue("a"));
        assertEquals("two", obj.getString("b"));
    }

    @Test
    void parseArrayReturnsJSONArray() {
        JSONArray arr = JSON.parseArray("[1,\"two\",true]");
        assertEquals(3, arr.size());
        assertEquals(1, arr.getIntValue(0));
        assertEquals("two", arr.getString(1));
        assertTrue(arr.getBooleanValue(2));
    }
}
