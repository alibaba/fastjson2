package com.alibaba.fastjson2;

import com.alibaba.fastjson2_vo.Integer1;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JSONObjectTest {
    @Test
    public void test_constructor() {
        JSONObject object = new JSONObject(Collections.singletonMap("id", 1001));
        assertEquals("{\"id\":1001}", object.toString());
    }

    @Test
    public void test_0() {
        JSONObject object = new JSONObject();
        object.put("id", 1001);
        assertEquals("{\"id\":1001}", object.toString());
    }

    @Test
    public void test_getJSONArray() {
        JSONObject object = new JSONObject();
        object.put("obj", "[]");
        assertNotNull(object.getJSONArray("obj"));
        assertEquals("[]", object.getJSONArray("obj").toString());
    }

    @Test
    public void test_1() {
        JSONObject object = new JSONObject();
        object.put("obj", "{}");
        assertNotNull(object.getJSONObject("obj"));
        assertEquals("{}", object.getJSONObject("obj").toString());
    }

    @Test
    public void test_2() {
        JSONObject object = new JSONObject();
        object.put("obj", Collections.emptyMap());
        Integer1 obj = object.getObject("obj", Integer1.class);
        assertNotNull(obj);
        assertEquals("{}", JSON.toJSONString(obj));
    }

    @Test
    public void test_3() {
        JSONObject object = new JSONObject();
        object.put("obj", Collections.singletonList(Collections.emptyMap()));
        List<Integer1> list = object.getObject("obj"
                , new TypeReference<List<Integer1>>() {
                }.getType());
        assertNotNull(list);
        assertEquals(Integer1.class, list.get(0).getClass());
        assertEquals("[{}]", JSON.toJSONString(list));
    }

    @Test
    public void test_4() {
        JSONObject object = new JSONObject();
        object.put("obj", Collections.singletonMap("val", Collections.emptyMap()));
        Map<String, Integer1> map = object.getObject("obj"
                , new TypeReference<Map<String, Integer1>>() {
                }.getType());
        assertNotNull(map);
        assertEquals(Integer1.class, map.get("val").getClass());
        assertEquals("{\"val\":{}}", JSON.toJSONString(map));
    }

    @Test
    public void test_5() {
        JSONObject object = new JSONObject();
        object.put("val", "123");
        assertEquals(123L, object.getLongValue("val"));
        assertEquals(123L, object.getLong("val").longValue());

        assertEquals(123, object.getIntValue("val"));
        assertEquals(123, object.getInteger("val").intValue());

        assertEquals("123", object.getString("val"));
    }

    @Test
    public void test_6() {
        JSONObject object = new JSONObject();
        object.put("val", 123);
        assertEquals(123L, object.getLongValue("val"));
        assertEquals(123L, object.getLong("val").longValue());

        assertEquals(123, object.getIntValue("val"));
        assertEquals(123, object.getInteger("val").intValue());

        assertEquals("123", object.getString("val"));
    }

    @Test
    public void test_7() {
        JSONObject object = new JSONObject();
        object.put("val", 123L);
        assertEquals(123L, object.getLongValue("val"));
        assertEquals(123L, object.getLong("val").longValue());

        assertEquals(123, object.getIntValue("val"));
        assertEquals(123, object.getInteger("val").intValue());

        assertEquals("123", object.getString("val"));
    }

    @Test
    public void test_8() {
        JSONObject object = new JSONObject();
        object.put("val", BigInteger.valueOf(123));
        assertEquals(123L, object.getLongValue("val"));
        assertEquals(123L, object.getLong("val").longValue());

        assertEquals(123, object.getIntValue("val"));
        assertEquals(123, object.getInteger("val").intValue());

        assertEquals("123", object.getString("val"));
    }

    @Test
    public void test_9() {
        JSONObject object = new JSONObject();
        object.put("val", BigDecimal.valueOf(123));
        assertEquals(123L, object.getLongValue("val"));
        assertEquals(123L, object.getLong("val").longValue());

        assertEquals(123, object.getIntValue("val"));
        assertEquals(123, object.getInteger("val").intValue());

        assertEquals("123", object.getString("val"));
    }

    @Test
    public void test_clone() {
        JSONObject object = new JSONObject();
        object.put("val", BigDecimal.valueOf(123));
        JSONObject clone = (JSONObject) object.clone();
        assertEquals(object, clone);
    }

    @Test
    public void test_null() {
        JSONObject object = new JSONObject();
        object.put("val", null);
        assertEquals(0L, object.getLongValue("val"));
        assertEquals(null, object.getLong("val"));

        assertEquals(0, object.getIntValue("val"));
        assertEquals(null, object.getInteger("val"));

        assertEquals(null, object.getString("val"));
        assertEquals(null, object.getJSONArray("val"));
        assertEquals(null, object.getJSONObject("val"));
        assertEquals(null, object.getBigInteger("val"));
        assertEquals(null, object.getBigDecimal("val"));
        assertEquals(null, object.getDouble("val"));
        assertEquals(0D, object.getDoubleValue("val"));
        assertEquals(null, object.getFloat("val"));
        assertEquals(0F, object.getFloatValue("val"));
        assertEquals(false, object.getBooleanValue("val"));
        assertEquals(null, object.getBoolean("val"));
        assertEquals((short) 0, object.getShortValue("val"));
        assertEquals(null, object.getShort("val"));
        assertEquals((byte) 0, object.getByteValue("val"));
        assertEquals(null, object.getByte("val"));
    }

    @Test
    public void test_null_str() {
        JSONObject object = new JSONObject();
        object.put("val", "null");
        assertEquals(0L, object.getLongValue("val"));
        assertEquals(null, object.getLong("val"));

        assertEquals(0, object.getIntValue("val"));
        assertEquals(null, object.getInteger("val"));

        assertEquals(null, object.getJSONArray("val"));
        assertEquals(null, object.getJSONObject("val"));
        assertEquals(null, object.getBigInteger("val"));
        assertEquals(null, object.getBigDecimal("val"));
        assertEquals(null, object.getFloat("val"));
        assertEquals(null, object.getDouble("val"));
        assertEquals(null, object.getBoolean("val"));
        assertEquals(null, object.getByte("val"));
        assertEquals(null, object.getShort("val"));
        assertEquals(0, object.getByteValue("val"));
        assertEquals(0, object.getShortValue("val"));
    }

    @Test
    public void test_null_str_empty() {
        JSONObject object = new JSONObject();
        object.put("val", "");
        assertEquals(0L, object.getLongValue("val"));
        assertEquals(null, object.getLong("val"));

        assertEquals(0, object.getIntValue("val"));
        assertEquals(null, object.getInteger("val"));

        assertEquals(null, object.getJSONArray("val"));
        assertEquals(null, object.getJSONObject("val"));
        assertEquals(null, object.getBigInteger("val"));
        assertEquals(null, object.getBigDecimal("val"));
        assertEquals(null, object.getBoolean("val"));
        assertEquals(null, object.getFloat("val"));
        assertEquals(null, object.getDouble("val"));
        assertEquals(null, object.getByte("val"));
        assertEquals(null, object.getShort("val"));
        assertEquals(0, object.getByteValue("val"));
        assertEquals(0, object.getShortValue("val"));
    }

    @Test
    public void test_error() {
        JSONObject jsonObject = new JSONObject().fluentPut("val", new Object());
        {
            Exception error = null;
            try {
                jsonObject.getLong("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getLongValue("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getInteger("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getIntValue("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getShort("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getShortValue("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getByte("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getByteValue("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getDouble("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getDoubleValue("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getFloat("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getFloatValue("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBigInteger("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBigDecimal("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBoolean("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBooleanValue("val");
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
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getBigInteger("val"));
    }

    @Test
    public void test_getBigDecimal() {
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12F)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12D)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getBigDecimal("val"));
    }

    @Test
    public void test_getFloatValue() {
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12F)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getFloatValue("val"));
    }

    @Test
    public void test_getFloat() {
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12F)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getFloat("val"));
    }

    @Test
    public void test_getDoubleValue() {
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12F)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getDoubleValue("val"));
    }

    @Test
    public void test_getDouble() {
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12F)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getDouble("val"));
    }

    @Test
    public void test_getBoolean() {
        assertEquals(
                Boolean.TRUE
                , new JSONObject()
                        .fluentPut("val", 1)
                        .getBoolean("val"));
        assertEquals(
                Boolean.TRUE
                , new JSONObject()
                        .fluentPut("val", "true")
                        .getBoolean("val"));
        assertEquals(
                Boolean.FALSE
                , new JSONObject()
                        .fluentPut("val", Boolean.FALSE)
                        .getBoolean("val"));
        assertEquals(
                Boolean.FALSE
                , new JSONObject()
                        .fluentPut("val", "FALSE")
                        .getBoolean("val"));
    }

    @Test
    public void test_getBooleanValue() {
        assertEquals(
                true
                , new JSONObject()
                        .fluentPut("val", 1)
                        .getBooleanValue("val"));
        assertEquals(
                true
                , new JSONObject()
                        .fluentPut("val", "true")
                        .getBooleanValue("val"));
        assertEquals(
                false
                , new JSONObject()
                        .fluentPut("val", "FALSE")
                        .getBooleanValue("val"));
        assertEquals(
                false
                , new JSONObject()
                        .fluentPut("val", Boolean.FALSE)
                        .getBooleanValue("val"));
    }

    @Test
    public void test_getShortValue() {
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getShortValue("val"));
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getShortValue("val"));
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getShortValue("val"));
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getShortValue("val"));
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getShortValue("val"));
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getShortValue("val"));
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getShortValue("val"));
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getShortValue("val"));
        assertEquals(
                (short) 12
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getShortValue("val"));
    }

    @Test
    public void test_getShort() {
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", Short.valueOf((short) 12))
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", Byte.valueOf((byte) 12))
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getShort("val"));
    }

    @Test
    public void test_getByteValue() {
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getByteValue("val"));
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getByteValue("val"));
        assertEquals(
                (byte) 12
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getByteValue("val"));
    }

    @Test
    public void test_getByte() {
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", Short.valueOf((short) 12))
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", Byte.valueOf((byte) 12))
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getByte("val"));
    }

    @Test
    public void test_getDate() {
        assertNull(JSONObject.of("id", null).getDate("id"));
        assertNull(JSONObject.of("id", "").getDate("id"));
        assertNull(JSONObject.of("id", 0).getDate("id"));
        assertNull(JSONObject.of("id", 0L).getDate("id"));

        long millis = System.currentTimeMillis();
        Date date = new Date(millis);
        assertSame(date, JSONObject.of("id", date).getDate("id"));
        assertEquals(date, JSONObject.of("id", millis).getDate("id"));

        ZonedDateTime zdt = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(millis), ZoneId.of("Asia/Shanghai"));
        assertEquals(date, JSONObject.of("id", zdt).getDate("id"));
        assertEquals(date, JSONObject.of("id", zdt.toString()).getDate("id"));
        assertEquals(date, JSONObject.of("id", '"' + zdt.toString() + '"').getDate("id"));
    }

    @Test
    public void test_getInstant() {
        assertNull(JSONObject.of("id", null).getInstant("id"));
        assertNull(JSONObject.of("id", "").getInstant("id"));
        assertNull(JSONObject.of("id", "null").getInstant("id"));
        assertNull(JSONObject.of("id", 0).getInstant("id"));
        assertNull(JSONObject.of("id", 0L).getInstant("id"));

        long millis = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(millis);
        assertSame(instant, JSONObject.of("id", instant).getInstant("id"));
        assertEquals(instant, JSONObject.of("id", millis).getInstant("id"));
        assertEquals(instant, JSONObject.of("id", new Date(millis)).getInstant("id"));

        ZonedDateTime zdt = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(millis), ZoneId.of("UTC+0"));
        assertEquals(instant, JSONObject.of("id", zdt).getInstant("id"));
        assertEquals(instant, JSONObject.of("id", zdt.toString()).getInstant("id"));
        assertEquals(instant, JSONObject.of("id", '"' + zdt.toString() + '"').getInstant("id"));
    }

    @Test
    public void read() {
        String str = "{\"id\":123}";
        JSONObject jsonObject = JSON.parseObject(str);
        assertEquals(123, jsonObject.getIntValue("id"));
        assertEquals(123L, jsonObject.getLongValue("id"));
        assertEquals("123", jsonObject.getString("id"));
        assertEquals(Integer.valueOf(123), jsonObject.getInteger("id"));
        assertEquals(Long.valueOf(123), jsonObject.getLong("id"));
    }

    @Test
    public void testGet() {
        JSONObject jsonObject = JSONObject.of("key", "1");
        assertNull(jsonObject.getObject("a", String.class));
    }

    @Test
    public void test_get() {
        JSONObject jsonObject = JSONObject.of("123", "value1", "456.789", "value2", null, "value3");
        assertEquals("value1", jsonObject.get(123));
        assertEquals("value2", jsonObject.get(456.789));
        assertEquals("value3", jsonObject.get(null));
        assertEquals("value4", jsonObject.getOrDefault(false, "value4"));
    }

    @Test
    public void test_obj_key() {
        JSONObject object = new JSONObject();
        Object key = "id";
        object.put(key, 101);
    }

    @Test
    public void test_invoke() {
        JSONObject object = new JSONObject(3);
        User proxy = (User) Proxy.newProxyInstance(
                User.class.getClassLoader(),
                new Class<?>[]{User.class, Map.class}, object
        );

        object.put("empty", "1");
        assertEquals(false, ((Map) proxy).isEmpty());

        object.put("empty", true);
        assertEquals(true, ((Map) proxy).isEmpty());

        object.put("empty", "true");
        assertEquals(true, ((Map) proxy).isEmpty());

        object.remove("empty");
        assertEquals(true, ((Map) proxy).isEmpty());

        object.put("blank", "1");
        assertEquals(false, ((Map) proxy).isEmpty());

        object.put("name", "kraity");
        assertEquals("kraity", proxy.getName());
    }

    public interface User {
        String getName();
    }
}
