package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

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

        assertNull(TypeUtils.toBigDecimal((String) null));
        assertNull(TypeUtils.toBigDecimal((byte[]) null));
        assertNull(TypeUtils.toBigDecimal((Object) null));
        assertNull(TypeUtils.toBigDecimal(""));
        assertNull(TypeUtils.toBigDecimal("null"));
        assertEquals(BigDecimal.valueOf(1), TypeUtils.toBigDecimal(1));
        assertEquals(BigDecimal.valueOf(1), TypeUtils.toBigDecimal(1L));
        assertEquals(BigDecimal.valueOf(1), TypeUtils.toBigDecimal("1"));

        assertEquals(byte.class, TypeUtils.loadClass("B"));
        assertEquals(short.class, TypeUtils.loadClass("S"));
        assertEquals(int.class, TypeUtils.loadClass("I"));
        assertEquals(long.class, TypeUtils.loadClass("J"));
        assertEquals(float.class, TypeUtils.loadClass("F"));
        assertEquals(double.class, TypeUtils.loadClass("D"));
        assertEquals(boolean.class, TypeUtils.loadClass("Z"));
        assertEquals(char.class, TypeUtils.loadClass("C"));

        Class[] classes = new Class[]{
                String.class,
                BigDecimal.class,
                Integer.class,
                Long.class,
                char[].class,
                Collections.EMPTY_MAP.getClass(),
                Collections.EMPTY_SET.getClass(),
                Collections.EMPTY_LIST.getClass()
        };
        for (Class clazz : classes) {
            assertEquals(clazz, TypeUtils.loadClass(clazz.getName()));
        }
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

        assertEquals(Instant.ofEpochSecond(instant.getEpochSecond()),
                TypeUtils.cast(JSONObject.of("epochSecond", instant.getEpochSecond()), Instant.class));
        assertEquals(instant,
                TypeUtils.cast(JSONObject.of("epochMilli", instant.toEpochMilli()), Instant.class));

        Exception error = null;
        try {
            TypeUtils.cast(JSONObject.of("", "xx"), Instant.class);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void testGetDefaultValue() {
        assertEquals(false, TypeUtils.getDefaultValue(boolean.class));
        assertEquals((byte) 0, TypeUtils.getDefaultValue(byte.class));
        assertEquals((short) 0, TypeUtils.getDefaultValue(short.class));
        assertEquals(0, TypeUtils.getDefaultValue(int.class));
        assertEquals(0L, TypeUtils.getDefaultValue(long.class));
        assertEquals(0F, TypeUtils.getDefaultValue(float.class));
        assertEquals(0D, TypeUtils.getDefaultValue(double.class));
        assertEquals((char) 0, TypeUtils.getDefaultValue(char.class));

        assertEquals(Optional.empty(), TypeUtils.getDefaultValue(Optional.class));
        assertEquals(OptionalInt.empty(), TypeUtils.getDefaultValue(OptionalInt.class));
        assertEquals(OptionalLong.empty(), TypeUtils.getDefaultValue(OptionalLong.class));
        assertEquals(OptionalDouble.empty(), TypeUtils.getDefaultValue(OptionalDouble.class));
    }

    @Test
    public void test2() {
        assertEquals(
                Integer.valueOf(1),
                TypeUtils.cast("1", (Type) Integer.class)
        );
        assertNull(
                TypeUtils.cast(null, (Type) Integer.class)
        );

        HashMap<Object, Object> map = new HashMap<>();
        map.put("id", "123");

        assertEquals(
                Integer.valueOf(123),
                ((Bean<Integer>) TypeUtils.cast(map, new TypeReference<Bean<Integer>>() {
                }.getType())).id
        );

        List<Map> list = new ArrayList<>();
        list.add(map);

        assertEquals(
                Integer.valueOf(123),
                ((List<Bean<Integer>>) TypeUtils.cast(list, new TypeReference<List<Bean<Integer>>>() {
                }.getType())).get(0).id
        );
    }

    @Test
    public void testToStringArray() {
        assertNull(TypeUtils.toStringArray(null));

        String[] strings = {"1", "2", "3"};
        assertArrayEquals(strings, TypeUtils.toStringArray(strings));
        assertArrayEquals(strings, TypeUtils.toStringArray(Arrays.asList(strings)));
        assertArrayEquals(strings, TypeUtils.toStringArray(new Object[]{1, 2, 3}));
    }

    @Test
    public void testCast() {
        assertNull(TypeUtils.cast(null, new Type[0]));

        assertArrayEquals(
                new Object[]{
                        1,
                        2L,
                        BigDecimal.valueOf(3)
                },
                TypeUtils.cast(
                        new String[]{"1", "2", "3"},
                        new Type[]{Integer.class, Long.class, BigDecimal.class})
        );
    }

    @Test
    public void loadClass() {
        assertSame(
                Collections.EMPTY_MAP.getClass(),
                TypeUtils.loadClass("java.util.Collections$EmptyMap")
        );
        assertSame(
                Collections.EMPTY_SET.getClass(),
                TypeUtils.loadClass("java.util.Collections$EmptySet")
        );
        assertSame(
                Collections.unmodifiableList(new ArrayList<>()).getClass(),
                TypeUtils.loadClass("java.util.Collections$UnmodifiableRandomAccessList")
        );
        assertSame(
                java.util.Optional.class,
                TypeUtils.loadClass("java.util.Optional")
        );
        assertSame(
                java.util.OptionalInt.class,
                TypeUtils.loadClass("java.util.OptionalInt")
        );
        assertSame(
                java.util.OptionalLong.class,
                TypeUtils.loadClass("java.util.OptionalLong")
        );
        assertSame(
                List.class,
                TypeUtils.loadClass("java.util.List")
        );
        assertSame(
                List.class,
                TypeUtils.loadClass("List")
        );
        assertSame(
                Set.class,
                TypeUtils.loadClass("java.util.Set")
        );
        assertSame(
                String[].class,
                TypeUtils.loadClass("[String")
        );
        assertSame(
                String[].class,
                TypeUtils.loadClass("String[]")
        );
        assertSame(
                byte[].class,
                TypeUtils.loadClass("byte[]")
        );
        assertSame(
                short[].class,
                TypeUtils.loadClass("short[]")
        );
        assertSame(
                short[].class,
                TypeUtils.loadClass("[S")
        );
        assertSame(
                int[].class,
                TypeUtils.loadClass("[I")
        );
        assertSame(
                int[].class,
                TypeUtils.loadClass("int[]")
        );
        assertSame(
                long[].class,
                TypeUtils.loadClass("[J")
        );
        assertSame(
                long[].class,
                TypeUtils.loadClass("long[]")
        );
        assertSame(
                float[].class,
                TypeUtils.loadClass("[F")
        );
        assertSame(
                float[].class,
                TypeUtils.loadClass("float[]")
        );
        assertSame(
                double[].class,
                TypeUtils.loadClass("[D")
        );
        assertSame(
                double[].class,
                TypeUtils.loadClass("double[]")
        );
        assertSame(
                boolean[].class,
                TypeUtils.loadClass("[Z")
        );
        assertSame(
                boolean[].class,
                TypeUtils.loadClass("boolean[]")
        );
    }

    @Test
    public void getInnerMap() {
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        Map innerMap = TypeUtils.getInnerMap(object);
        assertSame(object.getInnerMap(), innerMap);
    }

    public static class Bean<T> {
        public T id;
    }

    @Test
    public void toBigDecimal() {
        String[] strings = new String[]{
                "0",
                "1",
                "-1",
                "10",
                "-10",
                "55618.851",
                "-55618.851",
                "1234567890123456789",
                "-1234567890123456789",
                "12345678901234567890",
                "-12345678901234567890",
                "1234e10",
                "-1234e10",
                "1234567890.123456789",
                "-1234567890.123456789",
                "1234567890.1234567890",
                "-1234567890.1234567890",
        };
        for (String string : strings) {
            BigDecimal decimal = new BigDecimal(string);
            assertEquals(decimal, TypeUtils.toBigDecimal(string));
            assertEquals(decimal, TypeUtils.toBigDecimal(string.getBytes()));
            assertEquals(decimal, TypeUtils.toBigDecimal(string.toCharArray()));
        }
    }

    @Test
    public void toBigDecimalError() {
        assertThrows(Exception.class, () -> TypeUtils.toBigDecimal("123.45.67"));
        assertThrows(Exception.class, () -> TypeUtils.toBigDecimal("123.45.67".getBytes()));
        assertThrows(Exception.class, () -> TypeUtils.toBigDecimal("123.45.67".toCharArray()));

        assertNull(TypeUtils.toBigDecimal((byte[]) null));
        assertNull(TypeUtils.toBigDecimal(new byte[0]));

        assertNull(TypeUtils.toBigDecimal((char[]) null));
        assertNull(TypeUtils.toBigDecimal(new char[0]));
    }

    @Test
    public void test() {
        String str = "123.45";
        byte[] bytes = str.getBytes();
        assertArrayEquals(str.toCharArray(), TypeUtils.toAsciiCharArray(bytes));
    }

    @Test
    public void toStringTest() {
        for (char i = 0; i < 512; i++) {
            assertEquals(Character.toString(i), TypeUtils.toString(i));
        }

        for (char i = 0; i < 512; i++) {
            for (char j = 0; j < 512; j++) {
                assertEquals(new String(new char[]{i, j}), TypeUtils.toString(i, j));
            }
        }
    }

    @Test
    public void toStringTestJSON() {
        for (char i = 0; i < 512; i++) {
            String s1 = Character.toString(i);
            String json = JSON.toJSONString(s1);
            assertEquals(s1, JSON.parse(json.toCharArray()));
            assertEquals(s1, JSON.parse(json.getBytes()));
        }

        for (char i = 0; i < 512; i++) {
            for (char j = 0; j < 512; j++) {
                String s2 = new String(new char[]{i, j});
                String json = JSON.toJSONString(s2);
                assertEquals(s2, JSON.parse(json.toCharArray()));
                assertEquals(s2, JSON.parse(json.getBytes()));
            }
        }
    }

    @Test
    public void toStringTestJSONB() {
        for (char i = 0; i < 512; i++) {
            String s1 = Character.toString(i);
            byte[] json = JSONB.toBytes(s1);
            assertEquals(s1, JSONB.parse(json));
        }

        for (char i = 0; i < 512; i++) {
            for (char j = 0; j < 512; j++) {
                String s2 = new String(new char[]{i, j});
                byte[] json = JSONB.toBytes(s2);
                assertEquals(s2, JSONB.parse(json));
            }
        }
    }

    @Test
    public void toStringTestJSONB1() {
        for (char i = 0; i < 512; i++) {
            String s1 = Character.toString(i);
            byte[] json = JSONB.toBytes(JSONObject.of("value", s1));
            assertEquals(s1, JSONB.parseObject(json).get("value"));
        }

        for (char i = 0; i < 512; i++) {
            for (char j = 0; j < 512; j++) {
                String s2 = new String(new char[]{i, j});
                byte[] json = JSONB.toBytes(JSONObject.of("value", s2));
                assertEquals(s2, JSONB.parseObject(json).get("value"));
            }
        }
    }

    @Test
    public void toStringTestJSONB2() {
        for (char i = '0'; i <= '9'; i++) {
            String s1 = Character.toString(i);
            byte[] json = JSONB.toBytes(s1);
            assertEquals(Integer.parseInt(s1), JSONB.parseObject(json, Integer.class));
        }

        for (char i = '1'; i <= '9'; i++) {
            for (char j = '0'; j <= '9'; j++) {
                String s2 = new String(new char[]{i, j});
                byte[] json = JSONB.toBytes(s2);
                assertEquals(Integer.parseInt(s2), JSONB.parseObject(json, Integer.class));
            }
        }
    }

    @Test
    public void toStringTestJSONB3() {
        for (char i = 0; i < 512; i++) {
            String s1 = Character.toString(i);
            byte[] json = JSONB.toBytes(JSONObject.of(s1, s1));
            JSONObject object = JSONB.parseObject(json);
            Object v1 = object.get(s1);
            assertEquals(s1, v1);
        }

        for (char i = 0; i < 512; i++) {
            for (char j = 0; j < 512; j++) {
                String s2 = new String(new char[]{i, j});
                byte[] json = JSONB.toBytes(JSONObject.of(s2, s2));
                JSONObject object = JSONB.parseObject(json);
                Object v2 = object.get(s2);
                assertEquals(s2, v2, Integer.toString(i) + "-" + Integer.toString(j));
            }
        }
    }

    @Test
    public void toStringTestJSONB3_3() {
        for (char i = 0; i < 256; i++) {
            for (char j = 0; j < 256; j++) {
                for (char k = 0; k < 256; k++) {
                    String s3 = new String(new char[]{i, j, k});
                    byte[] json = JSONB.toBytes(JSONObject.of(s3, s3));
                    if (i == 128 && j == 0 && k == 1) {
                        JSONB.parseObject(json);
                    }
                    JSONObject object = JSONB.parseObject(json);
                    Object v2 = object.get(s3);
                    assertEquals(
                            s3,
                            v2,
                            Integer.toString(i) + "-" + Integer.toString(j) + "-" + Integer.toString(k)
                    );
                }
            }
        }
    }

    @Test
    public void isInteger() {
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.0")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.1")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.00")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.01")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.0000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.0001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.00000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.00001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.000000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.000001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.0000000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.0000001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.00000000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.00000001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.000000000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.000000001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.0000000000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.0000000001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.00000000000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.00000000001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("2.000000000000")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("2.000000000001")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("-92233720368547758081")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("-92233720368547758081")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("-9223372036854775808.1")));
    }
}
