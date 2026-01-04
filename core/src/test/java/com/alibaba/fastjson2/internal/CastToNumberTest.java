package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CastToNumberTest {
    @Test
    public void testToNumberWithNull() {
        assertNull(Cast.toNumber((Object) null));
        assertNull(Cast.toNumber((String) null));
    }

    @Test
    public void testToNumberWithNumber() {
        Integer value = 42;
        Number result = Cast.toNumber(value);
        assertEquals(value, result);
    }

    @Test
    public void testToNumberWithByte() {
        byte value = 127;
        Number result = Cast.toNumber(value);
        assertEquals(Byte.valueOf(value), result);
    }

    @Test
    public void testToNumberWithShort() {
        short value = 32767;
        Number result = Cast.toNumber(value);
        assertEquals(Short.valueOf(value), result);
    }

    @Test
    public void testToNumberWithInt() {
        int value = 123456;
        Number result = Cast.toNumber(value);
        assertEquals(Integer.valueOf(value), result);
    }

    @Test
    public void testToNumberWithLong() {
        long value = 1234567890L;
        Number result = Cast.toNumber(value);
        assertEquals(Long.valueOf(value), result);
    }

    @Test
    public void testToNumberWithFloat() {
        float value = 3.14f;
        Number result = Cast.toNumber(value);
        assertEquals(Float.valueOf(value), result);
    }

    @Test
    public void testToNumberWithDouble() {
        double value = 2.71828;
        Number result = Cast.toNumber(value);
        assertEquals(Double.valueOf(value), result);
    }

    @Test
    public void testToNumberWithChar() {
        char value = 'A'; // ASCII 65
        Number result = Cast.toNumber(value);
        assertEquals(Integer.valueOf(65), result);
    }

    @Test
    public void testToNumberWithBoolean() {
        assertTrue(Cast.toNumber(true).intValue() == 1);
        assertTrue(Cast.toNumber(false).intValue() == 0);
    }

    @Test
    public void testToNumberWithStringInteger() {
        String value = "123";
        Number result = Cast.toNumber(value);
        assertEquals(Long.valueOf(123L), result);
    }

    @Test
    public void testToNumberWithStringDouble() {
        String value = "3.14";
        Number result = Cast.toNumber(value);
        assertEquals(Double.valueOf(3.14), result);
    }

    @Test
    public void testToNumberWithStringScientific() {
        String value = "1.23e5";
        Number result = Cast.toNumber(value);
        assertEquals(Double.valueOf(123000.0), result);
    }

    @Test
    public void testToNumberWithBigInteger() {
        BigInteger value = new BigInteger("12345678901234567890");
        Number result = Cast.toNumber(value);
        assertEquals(value, result);
    }

    @Test
    public void testToNumberWithBigDecimal() {
        BigDecimal value = new BigDecimal("3.141592653589793");
        Number result = Cast.toNumber(value);
        assertEquals(value, result);
    }

    @Test
    public void testToNumberWithStringNull() {
        Number result = Cast.toNumber((String) null);
        assertNull(result);
    }

    @Test
    public void testToNumberWithInvalidString() {
        assertThrows(JSONException.class, () -> {
            Cast.toNumber("invalid");
        });
    }
}
