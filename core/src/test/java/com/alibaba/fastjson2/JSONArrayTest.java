package com.alibaba.fastjson2;

import com.alibaba.fastjson2_vo.Integer1;
import com.alibaba.fastjson2_vo.Long1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.*;

public class JSONArrayTest {
    @Test
    public void test_0() {
        JSONArray array = new JSONArray();
        array.add(1001);
        array.add("name");
        assertEquals("[1001,\"name\"]", array.toString());
    }

    @Test
    public void test_getJSONArray() {
        JSONArray array = new JSONArray();
        array.add("[]");
        assertNotNull(array.getJSONArray(0));
        assertEquals("[]", array.getJSONArray(0).toString());
    }

    @Test
    public void test_1() {
        JSONArray array = new JSONArray();
        array.add("{}");
        assertNotNull(array.getJSONObject(0));
        assertEquals("{}", array.get(0).toString());
    }

    @Test
    public void test_2() {
        JSONArray array = new JSONArray();
        array.add(Collections.singletonMap("v0000", 101));
        Integer1 obj = array.getObject(0, Integer1.class);
        assertNotNull(obj);
        assertEquals("{\"v0000\":101}", JSON.toJSONString(obj));

        List<Long1> list = array.toJavaObject(new TypeReference<List<Long1>>(){}.getType());
        Long1 long1 = list.get(0);
        assertEquals(Long.valueOf(101), long1.getV0000());
    }

    @Test
    public void test_3() {
        JSONArray array = new JSONArray();
        array.add(Collections.singletonList(Collections.emptyMap()));
        List<Integer1> list = array.getObject(0
                , new TypeReference<List<Integer1>>(){}.getType());
        assertNotNull(list);
        assertEquals(Integer1.class, list.get(0).getClass());
        assertEquals("[{}]", JSON.toJSONString(list));
    }

    @Test
    public void test_4() {
        JSONArray array = new JSONArray();
        array.add(Collections.singletonMap("val", Collections.emptyMap()));
        Map<String, Integer1> map = array.getObject(
                0, new TypeReference<Map<String, Integer1>>(){}.getType());
        assertNotNull(map);
        assertEquals(Integer1.class, map.get("val").getClass());
        assertEquals("{\"val\":{}}", JSON.toJSONString(map));
    }

    @Test
    public void test_5() {
        JSONArray array = new JSONArray();
        array.add("123");
        assertEquals(123L, array.getLongValue(0));
        assertEquals(123L, array.getLong(0).longValue());

        assertEquals(123, array.getIntValue(0));
        assertEquals(123, array.getInteger(0).intValue());

        assertEquals("123", array.getString(0));
    }

    @Test
    public void test_6() {
        JSONArray array = new JSONArray();
        array.add(123);
        assertEquals(123L, array.getLongValue(0));
        assertEquals(123L, array.getLong(0).longValue());

        assertEquals(123, array.getIntValue(0));
        assertEquals(123, array.getInteger(0).intValue());

        assertEquals("123", array.getString(0));
    }

    @Test
    public void test_7() {
        JSONArray array = new JSONArray();
        array.add(123L);
        assertEquals(123L, array.getLongValue(0));
        assertEquals(123L, array.getLong(0).longValue());

        assertEquals(123, array.getIntValue(0));
        assertEquals(123, array.getInteger(0).intValue());

        assertEquals("123", array.getString(0));
    }

    @Test
    public void test_8() {
        JSONArray array = new JSONArray();
        array.add(BigInteger.valueOf(123));
        assertEquals(123L, array.getLongValue(0));
        assertEquals(123L, array.getLong(0).longValue());

        assertEquals(123, array.getIntValue(0));
        assertEquals(123, array.getInteger(0).intValue());

        assertEquals("123", array.getString(0));
    }

    @Test
    public void test_9() {
        JSONArray array = new JSONArray();
        array.add(BigDecimal.valueOf(123));
        assertEquals(123L, array.getLongValue(0));
        assertEquals(123L, array.getLong(0).longValue());

        assertEquals(123, array.getIntValue(0));
        assertEquals(123, array.getInteger(0).intValue());

        assertEquals("123", array.getString(0));
    }

    @Test
    public void test_null() {
        JSONArray array = new JSONArray();
        array.add(null);
        assertEquals(0L, array.getLongValue(0));
        assertEquals(null, array.getLong(0));

        assertEquals(0, array.getIntValue(0));
        assertEquals(null, array.getInteger(0));

        assertEquals(null, array.getString(0));
        assertEquals(null, array.getJSONArray(0));
        assertEquals(null, array.getJSONObject(0));
    }

    @Test
    public void read() {
        String str = "[123]";
        JSONArray jsonArray = JSON.parseArray(str);
        assertEquals(123, jsonArray.getIntValue(0));
        assertEquals(123L, jsonArray.getLongValue(0));
        assertEquals("123", jsonArray.getString(0));
        assertEquals(Integer.valueOf(123), jsonArray.getInteger(0));
        assertEquals(Long.valueOf(123), jsonArray.getLong(0));
    }

    @Test
    public void test_null2() {
        JSONArray array = new JSONArray();
        array.add(null);
        assertEquals(0L, array.getLongValue(0));
        assertEquals(null, array.getLong(0));

        assertEquals(0, array.getIntValue(0));
        assertEquals(null, array.getInteger(0));

        assertEquals(null, array.getString(0));
        assertEquals(null, array.getJSONArray(0));
        assertEquals(null, array.getJSONObject(0));
        assertEquals(null, array.getBigInteger(0));
        assertEquals(null, array.getBigDecimal(0));
        assertEquals(null, array.getDouble(0));
        assertEquals(0D, array.getDoubleValue(0));
        assertEquals(null, array.getFloat(0));
        assertEquals(0F, array.getFloatValue(0));
        assertEquals(false, array.getBooleanValue(0));
        assertEquals(null, array.getBoolean(0));
        assertEquals((short) 0, array.getShortValue(0));
        assertEquals(null, array.getShort(0));
        assertEquals((byte) 0, array.getByteValue(0));
        assertEquals(null, array.getByte(0));
    }

    @Test
    public void test_null_str() {
        JSONArray object = new JSONArray();
        object.add("null");
        assertEquals(0L, object.getLongValue(0));
        assertEquals(null, object.getLong(0));

        assertEquals(0, object.getIntValue(0));
        assertEquals(null, object.getInteger(0));

        assertEquals(null, object.getJSONArray(0));
        assertEquals(null, object.getJSONObject(0));
        assertEquals(null, object.getBigInteger(0));
        assertEquals(null, object.getBigDecimal(0));
        assertEquals(null, object.getFloat(0));
        assertEquals(null, object.getDouble(0));
        assertEquals(null, object.getBoolean(0));
        assertEquals(null, object.getByte(0));
        assertEquals(null, object.getShort(0));
        assertEquals(0, object.getByteValue(0));
        assertEquals(0, object.getShortValue(0));
    }

    @Test
    public void test_null_str_empty() {
        JSONArray object = new JSONArray();
        object.add("");
        assertEquals(0L, object.getLongValue(0));
        assertEquals(null, object.getLong(0));

        assertEquals(0, object.getIntValue(0));
        assertEquals(null, object.getInteger(0));

        assertEquals(null, object.getJSONArray(0));
        assertEquals(null, object.getJSONObject(0));
        assertEquals(null, object.getBigInteger(0));
        assertEquals(null, object.getBigDecimal(0));
        assertEquals(null, object.getBoolean(0));
        assertEquals(null, object.getFloat(0));
        assertEquals(null, object.getDouble(0));
        assertEquals(null, object.getByte(0));
        assertEquals(null, object.getShort(0));
        assertEquals(0, object.getByteValue(0));
        assertEquals(0, object.getShortValue(0));
    }

    @Test
    public void test_error() {
        JSONArray jsonObject = new JSONArray().fluentAdd(new Object());
        {
            Exception error = null;
            try {
                jsonObject.getLong(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getLongValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getInteger(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getIntValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getShort(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getShortValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getByte(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getByteValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getDouble(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getDoubleValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getFloat(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getFloatValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBigInteger(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBigDecimal(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBoolean(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBooleanValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
    }

    @Test
    public void test_getBigInt() {
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12L)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12F)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12D)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONArray()
                        .fluentAdd("12")
                        .getBigInteger(0));
    }

    @Test
    public void test_getBigDecimal() {
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12L)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12F)
                , new JSONArray()
                        .fluentAdd(12F)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12D)
                , new JSONArray()
                        .fluentAdd(12D)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONArray()
                        .fluentAdd("12")
                        .getBigDecimal(0));
    }

    @Test
    public void test_getFloatValue() {
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12L)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12F)
                , new JSONArray()
                        .fluentAdd(12F)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12D)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd("12")
                        .getFloatValue(0));
    }

    @Test
    public void test_getFloat() {
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12L)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12F)
                , new JSONArray()
                        .fluentAdd(12F)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12D)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12)
                , new JSONArray()
                        .fluentAdd("12")
                        .getFloat(0));
    }

    @Test
    public void test_getDoubleValue() {
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12L)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12F)
                , new JSONArray()
                        .fluentAdd(12F)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12D)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd("12")
                        .getDoubleValue(0));
    }

    @Test
    public void test_getDouble() {
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12L)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12F)
                , new JSONArray()
                        .fluentAdd(12F)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(12D)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12)
                , new JSONArray()
                        .fluentAdd("12")
                        .getDouble(0));
    }

    @Test
    public void test_getBoolean() {
        assertEquals(
                Boolean.TRUE
                , new JSONArray()
                        .fluentAdd(1)
                        .getBoolean(0));
        assertEquals(
                Boolean.TRUE
                , new JSONArray()
                        .fluentAdd("true")
                        .getBoolean(0));
        assertEquals(
                Boolean.FALSE
                , new JSONArray()
                        .fluentAdd(Boolean.FALSE)
                        .getBoolean(0));
        assertEquals(
                Boolean.FALSE
                , new JSONArray()
                        .fluentAdd("FALSE")
                        .getBoolean(0));
    }

    @Test
    public void test_getBooleanValue() {
        assertEquals(
                true
                , new JSONArray()
                        .fluentAdd(1)
                        .getBooleanValue(0));
        assertEquals(
                true
                , new JSONArray()
                        .fluentAdd("true")
                        .getBooleanValue(0));
        assertEquals(
                false
                , new JSONArray()
                        .fluentAdd("FALSE")
                        .getBooleanValue(0));
        assertEquals(
                false
                , new JSONArray()
                        .fluentAdd(Boolean.FALSE)
                        .getBooleanValue(0));
    }

    @Test
    public void test_getShortValue() {
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd(12)
                        .getShortValue(0));
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getShortValue(0));
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getShortValue(0));
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd(12L)
                        .getShortValue(0));
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd(12F)
                        .getShortValue(0));
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd(12D)
                        .getShortValue(0));
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getShortValue(0));
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getShortValue(0));
        assertEquals(
                (short) 12
                , new JSONArray()
                        .fluentAdd("12")
                        .getShortValue(0));
    }

    @Test
    public void test_getShort() {
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd(12)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd(12L)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd(12F)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd(12D)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd(Short.valueOf((short) 12))
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd(Byte.valueOf((byte) 12))
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONArray()
                        .fluentAdd("12")
                        .getShort(0));
    }

    @Test
    public void test_getByteValue() {
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd(12)
                        .getByteValue(0));
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getByteValue(0));
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getByteValue(0));
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd(12L)
                        .getByteValue(0));
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd(12F)
                        .getByteValue(0));
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd(12D)
                        .getByteValue(0));
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getByteValue(0));
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getByteValue(0));
        assertEquals(
                (byte) 12
                , new JSONArray()
                        .fluentAdd("12")
                        .getByteValue(0));
    }

    @Test
    public void test_getByte() {
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd(12)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd((byte) 12)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd((short) 12)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd(12L)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd(12F)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd(12D)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd(Short.valueOf((short) 12))
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd(Byte.valueOf((byte) 12))
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONArray()
                        .fluentAdd("12")
                        .getByte(0));
    }

    @Test
    public void test_getDate() {
        assertNull(JSONArray.of((Object) null).getDate(0));
        assertNull(JSONArray.of("").getDate(0));
        assertNull(JSONArray.of("null").getDate(0));
        assertNull(JSONArray.of(0).getDate(0));
        assertNull(JSONArray.of(0L).getDate(0));

        long millis = System.currentTimeMillis();
        Date date = new Date(millis);
        assertSame(date, JSONArray.of(date).getDate(0));
        assertEquals(date, JSONArray.of(millis).getDate(0));
        assertEquals(date, JSONArray.of(Instant.ofEpochMilli(millis)).getDate(0));
    }

    @Test
    public void test_getInstant() {
        assertNull(JSONArray.of((Object) null).getInstant(0));
        assertNull(JSONArray.of("").getInstant(0));
        assertNull(JSONArray.of("null").getInstant(0));
        assertNull(JSONArray.of(0).getInstant(0));
        assertNull(JSONArray.of(0L).getInstant(0));

        long millis = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(millis);
        assertSame(instant, JSONArray.of(instant).getInstant(0));
        assertEquals(instant, JSONArray.of(millis).getInstant(0));
        assertEquals(instant, JSONArray.of(new Date(millis)).getInstant(0));
    }


    @Test
    public void test_fluentAdd() {
        JSONArray array = JSONArray.of().fluentAdd("1").fluentAdd(null);
        assertEquals(2, array.size());
        assertEquals("1", array.get(0));
        assertNull(null, array.get(1));
        assertNull(array.getObject(1, String.class));
        array.set(1, "");
        assertEquals(0D, array.getDoubleValue(1));
        assertEquals(0F, array.getFloatValue(1));
        assertEquals(false, array.getBooleanValue(1));
    }

    @Test
    public void test_ofArray() {
        JSONArray array0 = JSONArray.of("1");
        assertEquals(array0.get(0), "1");

        JSONArray array1 = JSONArray.of("1", "2", 3);
        assertEquals(array1.get(0), "1");
        assertEquals(array1.get(1), "2");
        assertEquals(array1.get(2), 3);

        JSONArray array2 = JSONArray.of(1, 2, 3, 4, "5");
        assertEquals(array2.get(0), 1);
        assertEquals(array2.get(1), 2);
        assertEquals(array2.get(2), 3);
        assertEquals(array2.get(3), 4);
        assertEquals(array2.get(4), "5");
    }
}
