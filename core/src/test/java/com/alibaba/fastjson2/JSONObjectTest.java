package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2_vo.Integer1;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

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
        assertEquals(object, object.clone());
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
        List<Integer1> list = object.getObject("obj",
                new TypeReference<List<Integer1>>() {
                }.getType());
        assertNotNull(list);
        assertEquals(Integer1.class, list.get(0).getClass());
        assertEquals("[{}]", JSON.toJSONString(list));
    }

    @Test
    public void test_4() {
        JSONObject object = new JSONObject();
        object.put("obj", Collections.singletonMap("val", Collections.emptyMap()));
        Map<String, Integer1> map = object.getObject("obj",
                new TypeReference<Map<String, Integer1>>() {
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
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONObject()
                        .fluentPut("val", "12")
                        .getBigInteger("val"));
    }

    @Test
    public void test_getBigDecimal() {
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12F),
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12D),
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONObject()
                        .fluentPut("val", "12")
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.ONE,
                new JSONObject()
                        .fluentPut("val", true)
                        .getBigDecimal("val"));
        assertEquals(
                BigDecimal.ZERO,
                new JSONObject()
                        .fluentPut("val", false)
                        .getBigDecimal("val"));
    }

    @Test
    public void test_getFloatValue() {
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12F),
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getFloatValue("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", "12")
                        .getFloatValue("val"));
    }

    @Test
    public void test_getFloat() {
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12F),
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getFloat("val"));
        assertEquals(
                Float.valueOf(12),
                new JSONObject()
                        .fluentPut("val", "12")
                        .getFloat("val"));
    }

    @Test
    public void test_getDoubleValue() {
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12F),
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getDoubleValue("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", "12")
                        .getDoubleValue("val"));
    }

    @Test
    public void test_getDouble() {
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12F),
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getDouble("val"));
        assertEquals(
                Double.valueOf(12),
                new JSONObject()
                        .fluentPut("val", "12")
                        .getDouble("val"));
    }

    @Test
    public void test_getBoolean() {
        assertEquals(
                Boolean.TRUE,
                new JSONObject()
                        .fluentPut("val", 1)
                        .getBoolean("val"));
        assertEquals(
                Boolean.TRUE,
                new JSONObject()
                        .fluentPut("val", "true")
                        .getBoolean("val"));
        assertEquals(
                Boolean.FALSE,
                new JSONObject()
                        .fluentPut("val", Boolean.FALSE)
                        .getBoolean("val"));
        assertEquals(
                Boolean.FALSE,
                new JSONObject()
                        .fluentPut("val", "FALSE")
                        .getBoolean("val"));
    }

    @Test
    public void test_getBooleanValue() {
        assertEquals(
                true,
                new JSONObject()
                        .fluentPut("val", 1)
                        .getBooleanValue("val"));
        assertEquals(
                true,
                new JSONObject()
                        .fluentPut("val", "true")
                        .getBooleanValue("val"));
        assertEquals(
                false,
                new JSONObject()
                        .fluentPut("val", "FALSE")
                        .getBooleanValue("val"));
        assertEquals(
                false,
                new JSONObject()
                        .fluentPut("val", Boolean.FALSE)
                        .getBooleanValue("val"));
    }

    @Test
    public void test_getShortValue() {
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", 12)
                        .getShortValue("val"));
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getShortValue("val"));
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getShortValue("val"));
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getShortValue("val"));
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getShortValue("val"));
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getShortValue("val"));
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getShortValue("val"));
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getShortValue("val"));
        assertEquals(
                (short) 12,
                new JSONObject()
                        .fluentPut("val", "12")
                        .getShortValue("val"));
    }

    @Test
    public void test_getShort() {
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", 12)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", Short.valueOf((short) 12))
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", Byte.valueOf((byte) 12))
                        .getShort("val"));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONObject()
                        .fluentPut("val", "12")
                        .getShort("val"));
    }

    @Test
    public void test_getByteValue() {
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", 12)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getByteValue("val"));
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getByteValue("val"));
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getByteValue("val"));
        assertEquals(
                (byte) 12,
                new JSONObject()
                        .fluentPut("val", "12")
                        .getByteValue("val"));
    }

    @Test
    public void test_getByte() {
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", 12)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", 12L)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", 12F)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", 12D)
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", Short.valueOf((short) 12))
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", Byte.valueOf((byte) 12))
                        .getByte("val"));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONObject()
                        .fluentPut("val", "12")
                        .getByte("val"));
    }

    @Test
    public void test_getDate() {
        assertNull(JSONObject.of("id", null).getDate("id"));
        assertNull(JSONObject.of("id", "").getDate("id"));
        assertEquals(0, JSONObject.of("id", 0).getDate("id").getTime());
        assertEquals(0L, JSONObject.of("id", 0L).getDate("id").getTime());

        long millis = System.currentTimeMillis();
        Date date = new Date(millis);
        assertSame(date, JSONObject.of("id", date).getDate("id"));
        assertEquals(date, JSONObject.of("id", millis).getDate("id"));

        ZonedDateTime zdt = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(millis), ZoneId.of("Asia/Shanghai"));
        assertEquals(date, JSONObject.of("id", zdt).getDate("id"));
        assertEquals(date, JSONObject.of("id", zdt.toString()).getDate("id"));
        assertEquals(date, JSONObject.of("id", '"' + zdt.toString() + '"').getDate("id"));

        LocalDate ldt = LocalDate.now();
        Date date1 = JSONObject.of("date", ldt).getDate("date");
        assertEquals(ldt.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), date1.getTime());
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

    @Test
    public void testGetObject() {
        Bean bean = new Bean();
        bean.id = 101;
        JSONObject object = JSONObject.of("value", bean);
        JSONObject object1 = object.getJSONObject("value");
        assertNotNull(object1);
        assertEquals(bean.id, object1.get("id"));

        JSONObject object2 = JSONArray.of(bean).getJSONObject(0);
        assertNotNull(object2);
        assertEquals(bean.id, object2.get("id"));
    }

    @Test
    public void test_init() {
        {
            JSONObject object = new JSONObject(1, 0.75f);
            object.put("id", 101);
            assertTrue(object.containsKey("id"));
            assertEquals(101, object.get("id"));
        }
        {
            JSONObject object2 = new JSONObject(1, 0.75f, false);
            object2.put("id", 101);
            assertTrue(object2.containsKey("id"));
            assertEquals(101, object2.get("id"));

            assertEquals(123, object2.getOrDefault("xx", 123));
            assertEquals(123, object2.getOrDefault(new Object(), 123));
        }
    }

    @Test
    public void test_getJSONArray2() {
        JSONObject object = new JSONObject().fluentPut("values", new ArrayList<>());
        JSONArray array = object.getJSONArray("values");
        assertEquals(0, array.size());
    }

    @Test
    public void test_getJSONObject2() {
        JSONObject object = new JSONObject().fluentPut("values", new HashMap<>());
        HashMap map = object.getJSONObject("values");
        assertEquals(0, map.size());
    }

    @Test
    public void test_getJSONObject3() {
        JSONObject object = new JSONObject();
        JSONObject j1 = new JSONObject();
        j1.put("a", "b");
        HashMap<String, Object> j2 = new HashMap<>();
        j2.put("k", "v");

        object.put("k0", null);
        object.put("k1", j1);
        object.put("k2", j2);

        assertNull(object.getJSONObject("k0"));
        assertSame(j1, object.getJSONObject("k1"));
        assertNotSame(j2, object.getJSONObject("k2"));
    }

    @Test
    public void test_getObject() {
        {
            UUID uuid = UUID.randomUUID();

            JSONObject object = new JSONObject().fluentPut("id", uuid.toString());
            UUID uuid2 = object.getObject("id", UUID.class);

            assertEquals(uuid, uuid2);
        }

        assertNull(
                JSONObject
                        .of("id", "")
                        .getObject("id", UUID.class)
        );

        assertNull(
                JSONObject
                        .of("id", "null")
                        .getObject("id", UUID.class)
        );

        assertEquals(Integer.valueOf(101),
                JSONObject
                        .of("id", 101)
                        .getObject("id", Number.class)
        );

        assertEquals(2,
                JSONObject
                        .of("id", 101, "name", "DataWorks")
                        .size()
        );

        assertNull(
                JSONObject
                        .of()
                        .getObject("id", (Type) User.class)
        );

        assertEquals(Integer.valueOf(123),
                JSONObject
                        .of("id", 123)
                        .getObject("id", (Type) Object.class)
        );

        assertEquals("123",
                JSONObject
                        .of("id", 123)
                        .getObject("id", (Type) String.class)
        );

        assertEquals(Integer.valueOf(123),
                JSONObject
                        .of("id", "123")
                        .getObject("id", (Type) Integer.class)
        );

        assertEquals(Integer.valueOf(123),
                JSONObject
                        .of("id", 123)
                        .getObject("id", (Type) Number.class)
        );

        assertEquals(new ArrayList(),
                JSONObject
                        .of("id", new ArrayList())
                        .getObject("id", List.class)
        );
    }

    @Test
    public void testFeatures() {
        JSONObject object = JSONObject.of("id", null);
        assertEquals("{}", object.toString());
        assertEquals("{\"id\":null}", object.toString(JSONWriter.Feature.WriteNulls));
    }

    @Test
    public void testFeatures1() {
        JSONArray array = JSONArray.of(JSONObject.of("id", null));
        assertEquals("[{}]", array.toString());
        assertEquals("[{\"id\":null}]", array.toString(JSONWriter.Feature.WriteNulls));
    }

    @Test
    public void testToJSONBBytes() {
        JSONObject object = JSONObject.of("id", null);
        byte[] jsonbBytes = object.toJSONBBytes(JSONWriter.Feature.WriteNulls);
        JSONObject object2 = JSONB.parseObject(jsonbBytes);
        assertEquals(object, object2);
    }

    @Test
    public void testToJSONBBytes1() {
        JSONArray array = JSONArray.of(JSONObject.of("id", null));
        byte[] jsonbBytes = array.toJSONBBytes(JSONWriter.Feature.WriteNulls);
        JSONArray array2 = JSONB.parseArray(jsonbBytes);
        assertEquals(array, array2);
    }

    public interface User {
        String getName();
    }

    @Test
    public void test_invoke2() {
        JSONObject object = new JSONObject(3);
        InvokeInterface proxy = (InvokeInterface) Proxy.newProxyInstance(
                InvokeInterface.class.getClassLoader(),
                new Class<?>[]{InvokeInterface.class, Map.class}, object
        );

        assertThrows(JSONException.class,
                () -> proxy.f(1)
        );
        assertThrows(JSONException.class,
                () -> proxy.f1(1)
        );

        assertTrue(proxy.equals(object));

        proxy.setId(101);
        assertEquals(101, object.get("id"));
        assertEquals(101, proxy.getId());

        assertThrows(JSONException.class,
                () -> proxy.set(101)
        );
        assertThrows(JSONException.class,
                () -> proxy.get()
        );
        assertThrows(JSONException.class,
                () -> proxy.getX()
        );
        assertThrows(JSONException.class,
                () -> proxy.is()
        );
        assertThrows(JSONException.class,
                () -> proxy.x0()
        );
        assertThrows(JSONException.class,
                () -> proxy.x(0)
        );
        assertThrows(UnsupportedOperationException.class,
                () -> proxy.xx(0, 1)
        );

        assertNull(proxy.getName());
        assertFalse(proxy.isName());

        object.put("small", "true");
        assertTrue(proxy.isSmall());

        object.put("y", "Y");
        assertEquals("Y", proxy.y());
        assertNull(proxy.y1());

        assertEquals(object.toString(), proxy.toString());
        assertEquals(object.hashCode(), proxy.hashCode());
    }

    @Test
    public void test_invoke3() {
        JSONObject object = new JSONObject();
        Meta proxy = (Meta) Proxy.newProxyInstance(
                Meta.class.getClassLoader(),
                new Class<?>[]{Meta.class, Map.class}, object
        );

        object.put("mask", "ok");

        // parameterCount = 0
        assertEquals("ok", proxy.getMask());

        // parameterCount = 1
        proxy.setMask("okk");
        assertEquals("okk", proxy.getMask());

        // parameterCount = 2
        boolean error = false;
        try {
            proxy.setHead("a", "b");
        } catch (Exception e) {
            error = true;
        }
        assertTrue(error);
    }

    interface Meta {
        String getMask();

        void setMask(String val);

        void setHead(String a, String b);
    }

    public interface InvokeInterface {
        void f(int p);

        Object f1(int p);

        @JSONField
        void setId(int value);

        void set(int value);

        @JSONField
        int getId();

        int get();

        void getX();

        String getName();

        boolean is();

        boolean isName();

        boolean isSmall();

        @JSONField(name = "y")
        Object y();

        @JSONField(name = "y1")
        Object y1();

        void xx(int x1, int x2);

        void x(int z1);

        Object x0();
    }

    public static class Bean {
        public int id;
    }

    @Test
    public void testCompatible() {
        assertEquals(0, JSONObject.parseObject("{}").size());
        assertEquals(0, ((JSONObject) JSONObject.parse("{}")).size());
        assertEquals(101, JSONObject.parseObject("{\"id\":101}", Bean.class).id);
        assertEquals(101, JSONObject.parseObject("{\"ID\":101}", Bean.class, JSONReader.Feature.SupportSmartMatch).id);
        assertEquals(0, JSONArray.parseArray("[]").size());
        assertEquals(0, JSONArray.parse("[]").size());
    }

    @Test
    public void test() {
        JSONObject object = new JSONObject();
        JSONArray array = object.putArray("values");
        array.add(1);
        assertEquals("{\"values\":[1]}", object.toString());
    }

    @Test
    public void test1() {
        JSONObject object = new JSONObject();
        object.putObject("values").put("id", 123);
        assertEquals("{\"values\":{\"id\":123}}", object.toString());
    }

    @Test
    public void testGetByPath() {
        JSONObject object = JSONObject.of("id", 101, "item", JSONObject.of("itemId", 1001));
        assertEquals(101, object.getByPath("id"));
        assertEquals(1001, object.getByPath("item.itemId"));
    }
}
