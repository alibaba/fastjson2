package com.alibaba.fastjson2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("features")
public class TreeModelTest {
    @Test
    public void deepCopyObject() {
        JSONObject object = JSON.parseObject(
                "{\"a\":{\"b\":1},\"c\":[{\"d\":2},{\"e\":[3,4]}],\"s\":\"str\",\"n\":123}"
        );
        JSONObject copy = object.deepCopy();
        assertEquals(object, copy);

        JSONObject inner = object.getJSONObject("a");
        JSONArray list = object.getJSONArray("c");
        assertNotSame(inner, copy.getJSONObject("a"));
        assertNotSame(list, copy.getJSONArray("c"));
        assertNotSame(list.getJSONObject(0), copy.getJSONArray("c").getJSONObject(0));
        assertNotSame(list.getJSONObject(1).getJSONArray("e"),
                copy.getJSONArray("c").getJSONObject(1).getJSONArray("e"));

        // mutating the copy must not affect the source, at any depth
        copy.getJSONObject("a").put("b", 99);
        copy.getJSONArray("c").getJSONObject(1).getJSONArray("e").add(5);
        assertEquals(1, object.getJSONObject("a").getIntValue("b"));
        assertEquals(2, object.getJSONArray("c").getJSONObject(1).getJSONArray("e").size());
    }

    @Test
    public void deepCopySharesScalarsAndPojos() {
        Date date = new Date(1704067200000L);
        JSONObject object = new JSONObject();
        object.put("date", date);
        object.put("s", "str");
        JSONObject copy = object.deepCopy();
        assertSame(date, copy.get("date"));
        assertSame("str", copy.get("s"));
    }

    @Test
    public void deepCopyArray() {
        JSONArray array = JSON.parseArray("[{\"a\":1},[2,3],\"s\"]");
        JSONArray copy = array.deepCopy();
        assertEquals(array, copy);
        assertNotSame(array.getJSONObject(0), copy.getJSONObject(0));
        assertNotSame(array.getJSONArray(1), copy.getJSONArray(1));
        copy.getJSONObject(0).put("a", 9);
        copy.getJSONArray(1).add(4);
        assertEquals(1, array.getJSONObject(0).getIntValue("a"));
        assertEquals(2, array.getJSONArray(1).size());
    }

    @Test
    public void deepCopyEmpty() {
        assertEquals(new JSONObject(), new JSONObject().deepCopy());
        assertEquals(new JSONArray(), new JSONArray().deepCopy());
    }

    @Test
    public void requiredPresent() {
        JSONObject object = JSON.parseObject("{\"a\":1,\"b\":{\"c\":2}}");
        assertEquals(1, object.required("a"));
        JSONObject nested = object.required("b", JSONObject.class);
        assertEquals(2, nested.getIntValue("c"));
    }

    @Test
    public void requiredMissing() {
        JSONObject object = JSON.parseObject("{\"a\":1}");
        JSONException e = assertThrows(JSONException.class, () -> object.required("x"));
        assertTrue(e.getMessage().contains("x"));
        e = assertThrows(JSONException.class, () -> object.required("x", JSONObject.class));
        assertTrue(e.getMessage().contains("x"));
    }

    @Test
    public void requiredNullValue() {
        JSONObject object = JSON.parseObject("{\"a\":null}");
        assertThrows(JSONException.class, () -> object.required("a"));
    }

    @Test
    public void requiredWrongType() {
        JSONObject object = JSON.parseObject("{\"a\":1}");
        JSONException e = assertThrows(JSONException.class, () -> object.required("a", JSONObject.class));
        assertTrue(e.getMessage().contains(JSONObject.class.getName()));
    }

    @Test
    public void canConvertToIntCheck() {
        JSONObject object = JSON.parseObject(
                "{\"i\":1,\"l\":1234567890123,\"big\":999999999999999999999,\"f\":1.5,\"s\":\"1\",\"n\":null}"
        );
        assertTrue(object.canConvertToInt("i"));
        assertFalse(object.canConvertToInt("l"));
        assertFalse(object.canConvertToInt("big"));
        assertFalse(object.canConvertToInt("f"));
        assertFalse(object.canConvertToInt("s"));
        assertFalse(object.canConvertToInt("n"));
        assertFalse(object.canConvertToInt("missing"));
    }

    @Test
    public void canConvertToLongCheck() {
        JSONObject object = JSON.parseObject(
                "{\"i\":1,\"l\":1234567890123,\"big\":999999999999999999999,\"f\":1.5,\"s\":\"1\"}"
        );
        assertTrue(object.canConvertToLong("i"));
        assertTrue(object.canConvertToLong("l"));
        assertFalse(object.canConvertToLong("big"));
        assertFalse(object.canConvertToLong("f"));
        assertFalse(object.canConvertToLong("s"));
        assertFalse(object.canConvertToLong("missing"));
    }

    @Test
    public void canConvertBoundaryValues() {
        Map<String, Object> map = new HashMap<>();
        map.put("maxInt", BigInteger.valueOf(Integer.MAX_VALUE));
        map.put("overInt", BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE));
        map.put("minInt", BigInteger.valueOf(Integer.MIN_VALUE));
        map.put("maxLong", BigInteger.valueOf(Long.MAX_VALUE));
        map.put("minLong", BigInteger.valueOf(Long.MIN_VALUE));
        map.put("overLong", BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE));
        map.put("underLong", BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE));
        map.put("decimal", new BigDecimal("10"));
        JSONObject object = new JSONObject(map);
        assertTrue(object.canConvertToInt("maxInt"));
        assertFalse(object.canConvertToInt("overInt"));
        assertTrue(object.canConvertToInt("minInt"));
        assertTrue(object.canConvertToLong("maxLong"));
        assertTrue(object.canConvertToLong("minLong"));
        assertFalse(object.canConvertToLong("overLong"));
        assertFalse(object.canConvertToLong("underLong"));
        // jackson semantics: decimal values are never reported as convertible to int/long
        assertFalse(object.canConvertToInt("decimal"));
        assertFalse(object.canConvertToLong("decimal"));
    }

    @Test
    public void integerTypesConvertable() {
        Map<String, Object> map = new HashMap<>();
        map.put("b", (byte) 1);
        map.put("s", (short) 2);
        map.put("i", 3);
        map.put("l", 4L);
        JSONObject object = new JSONObject(map);
        assertTrue(object.canConvertToInt("b"));
        assertTrue(object.canConvertToInt("s"));
        assertTrue(object.canConvertToInt("i"));
        assertTrue(object.canConvertToLong("l"));
    }
}
