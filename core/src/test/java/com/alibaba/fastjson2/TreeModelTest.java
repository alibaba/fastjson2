package com.alibaba.fastjson2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    public void deepCopyCyclicObject() {
        JSONObject object = JSON.parseObject("{\"a\":{\"$ref\":\"$\"}}");
        assertSame(object, object.get("a"));
        JSONObject copy = object.deepCopy();
        assertTrue(object != copy);
        assertSame(copy, copy.get("a"));
    }

    @Test
    public void deepCopyCyclicArray() {
        JSONArray array = new JSONArray();
        array.add(array);
        JSONArray copy = array.deepCopy();
        assertTrue(array != copy);
        assertSame(copy, copy.get(0));
    }

    @Test
    public void deepCopyNestedCycle() {
        JSONObject object = JSON.parseObject("{\"a\":{\"b\":{\"$ref\":\"$.a\"}}}");
        JSONObject copy = object.deepCopy();
        assertTrue(object != copy);
        assertSame(copy.getJSONObject("a"), copy.getJSONObject("a").getJSONObject("b"));
    }

    @Test
    public void deepCopySharedSubtreesStayDistinct() {
        JSONObject left = JSONObject.of("k", 1);
        JSONObject right = JSONObject.of("k", 1);
        JSONObject root = new JSONObject();
        root.put("l", left);
        root.put("r", right);
        JSONObject copy = root.deepCopy();
        // equal-but-distinct source subtrees must not collapse into one shared copy
        assertNotSame(copy.get("l"), copy.get("r"));
        assertEquals(left, copy.get("l"));
        assertEquals(right, copy.get("r"));
        assertNotSame(left, right);
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
                "{\"i\":1,\"l\":1234567890123,\"inRange\":100000,\"big\":999999999999999999999,\"f\":1.5,\"s\":\"1\",\"n\":null}"
        );
        assertTrue(object.canConvertToInt("i"));
        assertFalse(object.canConvertToInt("l"));
        // an in-range Long is convertible
        assertTrue(object.canConvertToInt("inRange"));
        assertFalse(object.canConvertToInt("big"));
        // jackson semantics: decimal within int range converts by truncation
        assertTrue(object.canConvertToInt("f"));
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
        // jackson semantics: decimal within long range converts by truncation
        assertTrue(object.canConvertToLong("f"));
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
        map.put("overIntD", 2147483648.0);
        map.put("maxIntD", 2147483647.0);
        map.put("fraction", 0.999);
        map.put("nan", Double.NaN);
        map.put("posInf", Double.POSITIVE_INFINITY);
        map.put("negInf", Double.NEGATIVE_INFINITY);
        map.put("overIntDec", new BigDecimal("2147483648"));
        map.put("hugeDec", new BigDecimal("1e30"));
        // value bounds are checked on the value itself, not on the truncated integer (jackson)
        map.put("overIntByFraction", new BigDecimal("2147483647.1"));
        map.put("maxIntMinusFraction", new BigDecimal("2147483646.9"));
        map.put("underMinByFraction", new BigDecimal("-2147483648.1"));
        map.put("overLongByFraction", new BigDecimal("9223372036854775807.1"));
        map.put("underMinLongByFraction", new BigDecimal("-9223372036854775808.1"));
        map.put("hugeExponent", new BigDecimal("1e2147483647"));
        map.put("tinyExponent", new BigDecimal("1e-2147483647"));
        JSONObject object = new JSONObject(map);
        assertTrue(object.canConvertToInt("maxInt"));
        assertFalse(object.canConvertToInt("overInt"));
        assertTrue(object.canConvertToInt("minInt"));
        assertTrue(object.canConvertToLong("maxLong"));
        assertTrue(object.canConvertToLong("minLong"));
        assertFalse(object.canConvertToLong("overLong"));
        assertFalse(object.canConvertToLong("underLong"));
        assertTrue(object.canConvertToInt("decimal"));
        assertTrue(object.canConvertToLong("decimal"));
        // jackson doubles: finite values within range convert by truncation
        assertTrue(object.canConvertToInt("maxIntD"));
        assertFalse(object.canConvertToInt("overIntD"));
        assertTrue(object.canConvertToInt("fraction"));
        assertFalse(object.canConvertToInt("nan"));
        assertFalse(object.canConvertToLong("nan"));
        assertFalse(object.canConvertToInt("posInf"));
        assertFalse(object.canConvertToLong("negInf"));
        assertFalse(object.canConvertToInt("overIntDec"));
        assertTrue(object.canConvertToLong("overIntDec"));
        assertFalse(object.canConvertToInt("hugeDec"));
        assertFalse(object.canConvertToLong("hugeDec"));
        assertFalse(object.canConvertToInt("overIntByFraction"));
        assertTrue(object.canConvertToInt("maxIntMinusFraction"));
        assertFalse(object.canConvertToInt("underMinByFraction"));
        assertFalse(object.canConvertToLong("overLongByFraction"));
        assertFalse(object.canConvertToLong("underMinLongByFraction"));
        assertFalse(object.canConvertToInt("hugeExponent"));
        assertFalse(object.canConvertToLong("hugeExponent"));
        assertTrue(object.canConvertToInt("tinyExponent"));
        assertTrue(object.canConvertToLong("tinyExponent"));
    }

    @Test
    public void integerTypesConvertable() {
        Map<String, Object> map = new HashMap<>();
        map.put("b", (byte) 1);
        map.put("s", (short) 2);
        map.put("i", 3);
        map.put("l", 4L);
        map.put("ai", new java.util.concurrent.atomic.AtomicInteger(5));
        map.put("al", new java.util.concurrent.atomic.AtomicLong(6));
        java.util.concurrent.atomic.LongAdder adder = new java.util.concurrent.atomic.LongAdder();
        adder.add(8);
        map.put("adder", adder);
        map.put("alMax", new java.util.concurrent.atomic.AtomicLong(Long.MAX_VALUE));
        JSONObject object = new JSONObject(map);
        assertTrue(object.canConvertToInt("b"));
        assertTrue(object.canConvertToInt("s"));
        assertTrue(object.canConvertToInt("i"));
        assertTrue(object.canConvertToLong("b"));
        assertTrue(object.canConvertToLong("s"));
        assertTrue(object.canConvertToLong("i"));
        assertTrue(object.canConvertToLong("l"));
        // other integral Number types are answered like getIntValue/getLongValue would convert them
        assertTrue(object.canConvertToInt("ai"));
        assertTrue(object.canConvertToLong("ai"));
        assertTrue(object.canConvertToInt("al"));
        assertTrue(object.canConvertToLong("al"));
        assertTrue(object.canConvertToInt("adder"));
        assertTrue(object.canConvertToLong("adder"));
        assertFalse(object.canConvertToInt("alMax"));
        assertTrue(object.canConvertToLong("alMax"));
    }

    @com.alibaba.fastjson2.annotation.JSONType(alphabetic = false)
    public static class SortChild {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class NestedSortHolder {
        public java.util.List<java.util.List<SortChild>> rows =
                java.util.Collections.singletonList(java.util.Collections.singletonList(new SortChild()));

        public java.util.List<java.util.Map<String, SortChild>> indexBy =
                java.util.Collections.singletonList(java.util.Collections.singletonMap("k", new SortChild()));
    }

    @Test
    public void fromSortsBeansInsideNestedListsAndMaps() {
        JSONObject tree = JSONObject.from(new NestedSortHolder(),
                JSONWriter.Feature.SortFieldNamesAlphabetically);
        assertEquals("{\"indexBy\":[{\"k\":{\"apple\":1,\"zebra\":3}}],\"rows\":[[{\"apple\":1,\"zebra\":3}]]}",
                tree.toString());
    }

    public static class LongsHolder {
        public long id = 9007199254740993L;
        public List<Long> ids = Arrays.asList(1L, 9007199254740993L);
        public List<Map<String, Object>> rows = Collections.singletonList(Collections.<String, Object>singletonMap("n", 5L));
    }

    @Test
    public void fromKeepsValueFormatFeaturesOffNestedValues() {
        // value-format features are not applied to tree values, nested or not
        String plain = JSONObject.from(new LongsHolder()).toString();
        assertEquals("{\"id\":9007199254740993,\"ids\":[1,9007199254740993],\"rows\":[{\"n\":5}]}", plain);
        assertEquals(plain, JSONObject.from(new LongsHolder(), JSONWriter.Feature.WriteLongAsString).toString());
        assertEquals(plain, JSONObject.from(new LongsHolder(), JSONWriter.Feature.WriteNonStringValueAsString).toString());
        assertEquals(plain, JSONObject.from(new LongsHolder(), JSONWriter.Feature.WriteClassName).toString());
    }
}
