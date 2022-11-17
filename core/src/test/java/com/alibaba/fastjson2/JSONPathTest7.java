package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTest7 {
    JSONObject object = JSONObject.of("name", "DataWorks", "id", 1001);
    String json = object.toString();
    byte[] jsonBytes = json.getBytes();
    byte[] jsonbBytes = object.toJSONBBytes();

    @Test
    public void testInteger() {
        JSONPath jsonPath = JSONPath.of("id", Integer.class);

        Integer id = 1001;
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testLong() {
        JSONPath jsonPath = JSONPath.of("id", Long.class);

        Long id = 1001L;
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testBigInteger() {
        JSONPath jsonPath = JSONPath.of("id", BigInteger.class);

        BigInteger id = object.getBigInteger("id");
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testBigDecimal() {
        JSONPath jsonPath = JSONPath.of("id", BigDecimal.class);

        BigDecimal id = object.getBigDecimal("id");
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testString() {
        JSONPath jsonPath = JSONPath.of("id", String.class);

        String id = "1001";
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testJSONPath0() {
        JSONObject object = JSONObject.of("item", 1);
        JSONObject root = JSONObject.of("values", JSONArray.of(object));
        JSONPath path = JSONPath.of("$.values[0].item");
        path.setInt(root, 2);

        assertEquals(1, object.size());
        assertEquals("2", object.get("item").toString());
    }

    @Test
    public void testJSONPath1() {
        JSONObject object = JSONObject.of("item", 1);
        JSONObject root = JSONObject.of("values", JSONArray.of(object));
        JSONPath path = JSONPath.of("$.values[0].item");
        path.setLong(root, 2);

        assertEquals(1, object.size());
        assertEquals("2", object.get("item").toString());
    }

    @Test
    public void testJSONPath2() {
        JSONObject object = JSONObject.of("item", 1);
        JSONObject root = JSONObject.of("values", JSONArray.of(object));

        JSONPath path = JSONPath.of("$.values[0].item");

        String json = root.toJSONString();
        String extractScalar = path.extractScalar(JSONReader.of(json));
        assertEquals("1", extractScalar);
    }

    @Test
    public void testToString() {
        JSONPathSegmentIndex segmentIndex = new JSONPathSegmentIndex(123);
        assertEquals("[123]", segmentIndex.toString());
    }

    @Test
    public void testRoot() {
        JSONPath jsonPath = JSONPath.of("$");
        assertSame(jsonPath, JSONPath.compile("$"));
        assertEquals(
                "1",
                jsonPath
                        .extractScalar(JSONReader.of("1"))
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.set(new Object(), 1)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.set(new Object(), 1, JSONReader.Feature.DuplicateKeyValueAsArray)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setCallback(null, (Function) null)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setCallback(null, (BiFunction) null)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setInt(null, 0)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setLong(null, 0)
        );
        assertFalse(jsonPath.remove(null));
    }

    @Test
    public void testPre() {
        JSONPath.PreviousPath jsonPath = (JSONPath.PreviousPath) JSONPath.of("#-1");
        assertSame(jsonPath, JSONPath.of("#-1", JSONPath.Feature.AlwaysReturnList));
        assertThrows(
                JSONException.class,
                () -> jsonPath.isRef()
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.contains(null)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.eval(null)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.extract((JSONReader) null)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.extractScalar((JSONReader) null)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.set(new Object(), 1)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.set(new Object(), 1, JSONReader.Feature.DuplicateKeyValueAsArray)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setCallback(new Object(), (BiFunction) null)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setInt(new Object(), 0)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setLong(new Object(), 0)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.remove(new Object())
        );
    }

    @Test
    public void testHashCode() {
        JSONPathMulti jsonPath = (JSONPathMulti) JSONPath.of("$.a.b.c.d.e.f");
        for (int i = 0; i < jsonPath.segments.size(); i++) {
            jsonPath.segments.get(i).hashCode();
        }
    }

    @Test
    public void testSingle() {
        JSONArray array = JSONArray.of("123");

        JSONPath jsonPath = JSONPath.of("$[0]");

        jsonPath.set(array, "234");
        assertEquals("234", array.get(0));

        jsonPath.set(
                array,
                "345",
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.AllowUnQuotedFieldNames
        );
        assertEquals("345", array.get(0));
    }

    @Test
    public void testFloor() {
        JSONArray array = JSONArray.of(1.8D, 2.3D, 3.5F, new BigDecimal("4.1"));
        JSONPath jsonPath = JSONPath.of("$.floor()");
        jsonPath.eval(array);
        assertEquals("[1.0,2.0,3.0,4]", jsonPath.eval(array).toString());
    }

    @Test
    public void testCeil() {
        JSONArray array = JSONArray.of(1.8D, 2.3D, 3.5F, new BigDecimal("4.1"));
        JSONPath jsonPath = JSONPath.of("$.ceil()");
        jsonPath.eval(array);
        assertEquals("[2.0,3.0,4.0,5]", jsonPath.eval(array).toString());
    }

    @Test
    public void testNegative() {
        JSONPath jsonPath = JSONPath.of("-$");

        assertNull(jsonPath.eval(null));
        assertEquals(-1, jsonPath.eval(1));
        assertEquals(-1L, jsonPath.eval(1L));
        assertEquals((short) -1, jsonPath.eval((short) 1));
        assertEquals((byte) -1, jsonPath.eval((byte) 1));
        assertEquals(-1F, jsonPath.eval(1F));
        assertEquals(-1D, jsonPath.eval(1D));
        assertEquals(BigDecimal.ONE.negate(), jsonPath.eval(BigDecimal.ONE));
        assertEquals(BigInteger.ONE.negate(), jsonPath.eval(BigInteger.ONE));

        JSONArray array = JSONArray.of(null, 1, 1L, (short) 1, (byte) 1, 1F, 1D, BigDecimal.ONE, BigInteger.ONE);
        assertEquals(
                "[null,-1,-1,-1,-1,-1.0,-1.0,-1,-1]",
                jsonPath.eval(array).toString()
        );

        assertEquals(BigInteger.valueOf(Long.MIN_VALUE).negate(), jsonPath.eval(Long.MIN_VALUE));
        assertEquals(-(long) Integer.MIN_VALUE, jsonPath.eval(Integer.MIN_VALUE));
        assertEquals(-(int) Short.MIN_VALUE, jsonPath.eval(Short.MIN_VALUE));
        assertEquals(-(short) Byte.MIN_VALUE, jsonPath.eval(Byte.MIN_VALUE));
    }

    @Test
    public void testAbs() {
        JSONPath jsonPath = JSONPath.of("$.abs()");

        assertNull(jsonPath.eval(null));
        assertEquals(1, jsonPath.eval(-1));
        assertEquals(1L, jsonPath.eval(-1L));
        assertEquals((short) 1, jsonPath.eval((short) -1));
        assertEquals((byte) 1, jsonPath.eval((byte) -1));
        assertEquals(1F, jsonPath.eval(-1F));
        assertEquals(1D, jsonPath.eval(-1D));
        assertEquals(BigDecimal.ONE, jsonPath.eval(BigDecimal.ONE.negate()));
        assertEquals(BigInteger.ONE, jsonPath.eval(BigInteger.ONE.negate()));

        JSONArray array = JSONArray.of(null, -1, -1L, (short) -1, (byte) -1, -1F, -1D, BigDecimal.ONE.negate(), BigInteger.ONE.negate());
        assertEquals(
                "[null,1,1,1,1,1.0,1.0,1,1]",
                jsonPath.eval(array).toString()
        );
    }
}
