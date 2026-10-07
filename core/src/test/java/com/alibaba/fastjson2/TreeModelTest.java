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
        // content equality is not enough: a copy must never alias the receiver
        JSONObject emptyObject = new JSONObject();
        assertNotSame(emptyObject, emptyObject.deepCopy());
        JSONArray emptyArray = new JSONArray();
        assertNotSame(emptyArray, emptyArray.deepCopy());
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
    public void deepCopyCrossTypeCycleAndSharedNode() {
        // an object -> array -> object cycle threads through the shared visited map: the
        // handoff between the two deepCopy(visited) bodies is what terminates the cycle
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        object.put("list", array);
        array.add(object);
        JSONObject copy = object.deepCopy();
        assertSame(copy, ((JSONArray) copy.get("list")).get(0));

        // an acyclic shared node deep-copies once and stays shared in the result
        JSONObject shared = JSONObject.of("k", "v");
        JSONObject root = new JSONObject();
        root.put("a", shared);
        root.put("b", new JSONArray(java.util.Collections.singletonList(shared)));
        JSONObject copied = root.deepCopy();
        assertSame(copied.get("a"), ((JSONArray) copied.get("b")).get(0));
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
        assertEquals("required value missing : x", e.getMessage());
        // the two-arg overload reports a missing key identically, not as a type mismatch
        e = assertThrows(JSONException.class, () -> object.required("x", JSONObject.class));
        assertEquals("required value missing : x", e.getMessage());
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
    public void requiredNoWidening() {
        JSONObject object = JSON.parseObject("{\"a\":1}");
        assertEquals(1, (int) object.required("a", Integer.class));
        // an Integer is not widened to Long: the type check is strict, as documented
        JSONException e = assertThrows(JSONException.class, () -> object.required("a", Long.class));
        assertTrue(e.getMessage().contains(Long.class.getName()));
    }

    @Test
    public void requiredPrimitiveClassBoxes() {
        // a primitive class literal means its boxed type; without the boxing the check could
        // never succeed, because Class.isInstance is false for every value on a primitive Class
        JSONObject object = JSON.parseObject("{\"n\":1}");
        assertEquals(1, (int) object.required("n", int.class));
        JSONException e = assertThrows(JSONException.class, () -> object.required("n", double.class));
        assertTrue(e.getMessage().contains("required value not of type double : n"));
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

        // the Long branch, with actual Long values at and past the int boundaries
        object.put("longInRange", 100000L);
        object.put("longMaxInt", (long) Integer.MAX_VALUE);
        object.put("longMinInt", (long) Integer.MIN_VALUE);
        object.put("longAbove", (long) Integer.MAX_VALUE + 1);
        object.put("longBelow", (long) Integer.MIN_VALUE - 1);
        assertTrue(object.canConvertToInt("longInRange"));
        assertTrue(object.canConvertToInt("longMaxInt"));
        assertTrue(object.canConvertToInt("longMinInt"));
        assertFalse(object.canConvertToInt("longAbove"));
        assertFalse(object.canConvertToInt("longBelow"));

        // Float values take the floating branch on both the int and the long side
        object.put("floatFraction", 2.5f);
        object.put("floatNan", Float.NaN);
        object.put("floatPosInf", Float.POSITIVE_INFINITY);
        object.put("floatNegInf", Float.NEGATIVE_INFINITY);
        assertTrue(object.canConvertToInt("floatFraction"));
        assertTrue(object.canConvertToLong("floatFraction"));
        assertFalse(object.canConvertToInt("floatNan"));
        assertFalse(object.canConvertToLong("floatNan"));
        assertFalse(object.canConvertToInt("floatPosInf"));
        assertFalse(object.canConvertToLong("floatNegInf"));
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
        // 2^63 as a double is one greater than Long.MAX_VALUE: not convertible, while the
        // greatest double below it truncates into range (the lower bound -2^63 stays inclusive)
        map.put("maxLongPlusOneD", 9.223372036854776E18);
        map.put("maxLongPlusOneF", 9.223372E18f);
        map.put("maxLongBelowD", 9223372036854774784.0);
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
        assertFalse(object.canConvertToLong("maxLongPlusOneD"));
        assertFalse(object.canConvertToLong("maxLongPlusOneF"));
        assertTrue(object.canConvertToLong("maxLongBelowD"));
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

        // wrapped or non-integral values are not convertible: a DoubleAdder's longValue
        // overflows Long at 1e30, and NaN has no integral conversion at all
        JSONObject wrapped = new JSONObject();
        java.util.concurrent.atomic.DoubleAdder huge = new java.util.concurrent.atomic.DoubleAdder();
        huge.add(1.0e30);
        wrapped.put("huge", huge);
        wrapped.put("nanAcc", Double.NaN);
        assertFalse(wrapped.canConvertToLong("huge"));
        assertFalse(wrapped.canConvertToLong("nanAcc"));
        wrapped.put("maxLongDiscriminatingProbe", Long.MAX_VALUE);
        assertTrue(wrapped.canConvertToLong("maxLongDiscriminatingProbe"));

        // a fraction is truncated as for Double and BigDecimal; NaN in an accumulator is not convertible
        java.util.concurrent.atomic.DoubleAdder fraction = new java.util.concurrent.atomic.DoubleAdder();
        fraction.add(1.5);
        wrapped.put("fraction", fraction);
        wrapped.put("double", 1.5);
        assertTrue(wrapped.canConvertToLong("fraction"));
        assertTrue(wrapped.canConvertToLong("double"));
        java.util.concurrent.atomic.DoubleAdder nan = new java.util.concurrent.atomic.DoubleAdder();
        nan.add(Double.NaN);
        wrapped.put("nan", nan);
        assertFalse(wrapped.canConvertToLong("nan"));
        assertFalse(wrapped.canConvertToInt("nan"));

        // an accumulator holding exactly 2^63 saturates longValue() to Long.MAX_VALUE while
        // doubleValue() reads the same 2^63: still not convertible (the guard-then-read idiom
        // would otherwise read Long.MAX_VALUE back), and a fraction truncates on the int side too
        java.util.concurrent.atomic.DoubleAdder maxLongEdge = new java.util.concurrent.atomic.DoubleAdder();
        maxLongEdge.add(9223372036854775807.0);
        wrapped.put("maxLongEdgeAcc", maxLongEdge);
        assertFalse(wrapped.canConvertToLong("maxLongEdgeAcc"));
        assertTrue(wrapped.canConvertToInt("fraction"));
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
