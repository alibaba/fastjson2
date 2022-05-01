package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TypeUtilsTest {
    @Test
    public void test_0() {
        assertNull(TypeUtils.toByte(null));
        assertNull(TypeUtils.toByte(""));
        assertNull(TypeUtils.toByte("null"));

        assertEquals(0, TypeUtils.toByteValue(null));
        assertEquals(0, TypeUtils.toByteValue(""));
        assertEquals(0, TypeUtils.toByteValue("null"));

        assertEquals(Byte.valueOf((byte) 0), TypeUtils.toByte("0"));
        assertEquals(0, TypeUtils.toByteValue("0"));

        assertEquals(0, TypeUtils.toByteValue((short) 0));
        assertEquals(0, TypeUtils.toByteValue((byte) 0));
        assertEquals(Byte.valueOf((byte) 0), TypeUtils.toByte((short) 0));
        assertEquals(Byte.valueOf((byte) 0), TypeUtils.toByte((byte) 0));


        assertNull(TypeUtils.toDouble(null));
        assertNull(TypeUtils.toDouble(""));
        assertNull(TypeUtils.toDouble("null"));

        assertEquals(0D, TypeUtils.toDoubleValue(null));
        assertEquals(0D, TypeUtils.toDoubleValue(""));
        assertEquals(0D, TypeUtils.toDoubleValue("null"));

        assertEquals(0D, TypeUtils.toDoubleValue(0D));
        assertEquals(0D, TypeUtils.toDouble(0D));

        assertEquals(0D, TypeUtils.toDouble("0"));
        assertEquals(0D, TypeUtils.toDoubleValue("0"));

        assertEquals(0D, TypeUtils.toDoubleValue((short) 0));
        assertEquals(0D, TypeUtils.toDoubleValue((byte) 0));
        assertEquals(0D, TypeUtils.toDouble((short) 0));
        assertEquals(0D, TypeUtils.toDouble((byte) 0));


        assertNull(TypeUtils.toFloat(null));
        assertNull(TypeUtils.toFloat(""));
        assertNull(TypeUtils.toFloat("null"));

        assertEquals(0F, TypeUtils.toFloatValue(null));
        assertEquals(0F, TypeUtils.toFloatValue(""));
        assertEquals(0F, TypeUtils.toFloatValue("null"));

        assertEquals(0F, TypeUtils.toFloatValue(0F));
        assertEquals(0F, TypeUtils.toFloat(0F));

        assertEquals(0F, TypeUtils.toFloat("0"));
        assertEquals(0F, TypeUtils.toFloatValue("0"));

        assertEquals(0F, TypeUtils.toFloatValue((short) 0));
        assertEquals(0F, TypeUtils.toFloatValue((byte) 0));
        assertEquals(0F, TypeUtils.toFloatValue((byte) 0));
        assertEquals(0F, TypeUtils.toFloatValue((float) 0));
        assertEquals(0F, TypeUtils.toFloat((short) 0));
        assertEquals(0F, TypeUtils.toFloat((byte) 0));
        assertEquals(0F, TypeUtils.toFloat((float) 0));

        assertNull(TypeUtils.toShort(null));
        assertNull(TypeUtils.toShort(""));
        assertNull(TypeUtils.toShort("null"));

        assertEquals(0, TypeUtils.toShortValue(null));
        assertEquals(0, TypeUtils.toShortValue(""));
        assertEquals(0, TypeUtils.toShortValue("null"));

        assertEquals(Short.valueOf((short) 0), TypeUtils.toShort("0"));
        assertEquals(0, TypeUtils.toShortValue("0"));

        assertEquals(0, TypeUtils.toShortValue((short) 0));
        assertEquals(Short.valueOf((short) 0), TypeUtils.toShort((short) 0));


        assertNull(TypeUtils.toInteger(null));
        assertNull(TypeUtils.toInteger(""));
        assertNull(TypeUtils.toInteger("null"));

        assertEquals(0, TypeUtils.toIntValue(null));
        assertEquals(0, TypeUtils.toIntValue(""));
        assertEquals(0, TypeUtils.toIntValue("null"));
        assertEquals(0, TypeUtils.toIntValue("0"));

        assertEquals(Integer.valueOf(0), TypeUtils.toInteger("0"));
        assertEquals(0, TypeUtils.toIntValue("0"));

        assertEquals(0, TypeUtils.toIntValue(0));
        assertEquals(Integer.valueOf(0), TypeUtils.toInteger(0));
        assertEquals(0, TypeUtils.toIntValue((short) 0));
        assertEquals(0, TypeUtils.toIntValue((byte) 0));
        assertEquals(0, TypeUtils.toIntValue((byte) 0));
        assertEquals(0, TypeUtils.toIntValue((float) 0));
        assertEquals(Integer.valueOf(0), TypeUtils.toInteger((short) 0));
        assertEquals(Integer.valueOf(0), TypeUtils.toInteger((byte) 0));
        assertEquals(Integer.valueOf(0), TypeUtils.toInteger((float) 0));
        assertEquals(Integer.valueOf(1), TypeUtils.toInteger(1));

        assertNull(TypeUtils.toLong(null));
        assertNull(TypeUtils.toLong(""));
        assertNull(TypeUtils.toLong("null"));

        assertEquals(0, TypeUtils.toLongValue(null));
        assertEquals(0, TypeUtils.toLongValue(""));
        assertEquals(0, TypeUtils.toLongValue("null"));

        assertEquals(Long.valueOf(0), TypeUtils.toLong("0"));
        assertEquals(0, TypeUtils.toLongValue("0"));

        assertEquals(0, TypeUtils.toLongValue(0L));
        assertEquals(Long.valueOf(0), TypeUtils.toLong(0L));
        assertEquals(0, TypeUtils.toLongValue((short) 0));
        assertEquals(0, TypeUtils.toLongValue((byte) 0));
        assertEquals(0, TypeUtils.toLongValue((byte) 0));
        assertEquals(0, TypeUtils.toLongValue((float) 0));
        assertEquals(Long.valueOf(0), TypeUtils.toLong((short) 0));
        assertEquals(Long.valueOf(0), TypeUtils.toLong((byte) 0));
        assertEquals(Long.valueOf(0), TypeUtils.toLong((float) 0));
        assertEquals(Long.valueOf(1), TypeUtils.toLong(1));

        assertNull(TypeUtils.toBigInteger(null));
        assertNull(TypeUtils.toBigInteger(""));
        assertNull(TypeUtils.toBigInteger("null"));
        assertEquals(BigInteger.valueOf(1), TypeUtils.toBigInteger(1));
        assertEquals(BigInteger.valueOf(1), TypeUtils.toBigInteger("1"));

        assertNull(TypeUtils.toBigDecimal(null));
        assertNull(TypeUtils.toBigDecimal(""));
        assertNull(TypeUtils.toBigDecimal("null"));
        assertEquals(BigDecimal.valueOf(1), TypeUtils.toBigDecimal(1));
        assertEquals(BigDecimal.valueOf(1), TypeUtils.toBigDecimal("1"));
    }

    @Test
    public void test_castToJavaBean_0() {
        Object o = new Object();
        assertSame(o, TypeUtils.cast(o, Object.class));
        assertNull(TypeUtils.cast(null, Object.class));
    }

    @Test
    public void test_error_0() {
        {
            JSONException error = null;
            try {
                TypeUtils.toByte(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            JSONException error = null;
            try {
                TypeUtils.toByteValue(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }

        {
            JSONException error = null;
            try {
                TypeUtils.toShort(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            JSONException error = null;
            try {
                TypeUtils.toShortValue(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }

        {
            JSONException error = null;
            try {
                TypeUtils.toInteger(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            JSONException error = null;
            try {
                TypeUtils.toIntValue(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }

        {
            JSONException error = null;
            try {
                TypeUtils.toLong(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            JSONException error = null;
            try {
                TypeUtils.toLongValue(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }

        {
            JSONException error = null;
            try {
                TypeUtils.toFloat(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            JSONException error = null;
            try {
                TypeUtils.toFloatValue(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }

        {
            JSONException error = null;
            try {
                TypeUtils.toDouble(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            JSONException error = null;
            try {
                TypeUtils.toDoubleValue(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }

        {
            JSONException error = null;
            try {
                TypeUtils.toBigDecimal(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            JSONException error = null;
            try {
                TypeUtils.toBigInteger(new Object());
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }

        {
            JSONException error = null;
            try {
                TypeUtils.cast(new Object(), Int1.class);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
    }

    @Test
    public void test_cast_0() {
        long millis = System.currentTimeMillis();
        Date date = new Date(millis);
        Instant instant = date.toInstant();

        assertSame(date, TypeUtils.cast(date, Date.class));
        assertEquals(date, TypeUtils.cast(date.toInstant(), Date.class));
        assertEquals(instant, TypeUtils.cast(date, Instant.class));
        assertSame(instant, TypeUtils.cast(instant, Instant.class));

        assertEquals(Instant.ofEpochSecond(instant.getEpochSecond())
                , TypeUtils.cast(JSONObject.of("epochSecond", instant.getEpochSecond()), Instant.class));
        assertEquals(instant
                , TypeUtils.cast(JSONObject.of("epochMilli", instant.toEpochMilli()), Instant.class));

        Exception error = null;
        try {
            TypeUtils.cast(JSONObject.of("", "xx"), Instant.class);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }
}
