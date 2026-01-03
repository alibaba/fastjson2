package com.alibaba.fastjson2.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CastTest {
    @Test
    public void testToShort() {
        // Test with Short object
        Short shortValue = 123;
        assertEquals(shortValue, Cast.toShort(shortValue));

        // Test with null
        assertNull(Cast.toShort(null));

        // Test with Integer that can be converted to short
        assertEquals(Short.valueOf((short) 456), Cast.toShort(456));

        // Test with String
        assertEquals(Short.valueOf((short) 789), Cast.toShort("789"));
    }

    @Test
    public void testToInteger() {
        // Test with Integer object
        Integer intValue = 12345;
        assertEquals(intValue, Cast.toInteger(intValue));

        // Test with null
        assertNull(Cast.toInteger(null));

        // Test with Short that can be converted to Integer
        assertEquals(Integer.valueOf(678), Cast.toInteger((short) 678));

        // Test with String
        assertEquals(Integer.valueOf(9012), Cast.toInteger("9012"));
    }

    @Test
    public void testToLong() {
        // Test with Long object
        Long longValue = 123456789L;
        assertEquals(longValue, Cast.toLong(longValue));

        // Test with null
        assertNull(Cast.toLong(null));

        // Test with Integer that can be converted to Long
        assertEquals(Long.valueOf(12345), Cast.toLong(12345));

        // Test with String
        assertEquals(Long.valueOf(9876543210L), Cast.toLong("9876543210"));
    }

    @Test
    public void testToFloat() {
        // Test with Float object
        Float floatValue = 123.45f;
        assertEquals(floatValue, Cast.toFloat(floatValue));

        // Test with null
        assertNull(Cast.toFloat(null));

        // Test with Double that can be converted to Float
        assertEquals(Float.valueOf(67.89f), Cast.toFloat(67.89));

        // Test with String
        assertEquals(Float.valueOf(12.34f), Cast.toFloat("12.34"));
    }

    @Test
    public void testToDouble() {
        // Test with Double object
        Double doubleValue = 123.456;
        assertEquals(doubleValue, Cast.toDouble(doubleValue));

        // Test with null
        assertNull(Cast.toDouble(null));

        // Test with Float that can be converted to Double (with delta for precision)
        assertEquals(78.90, Cast.toDouble(78.90f), 0.001);

        // Test with String
        assertEquals(Double.valueOf(45.67), Cast.toDouble("45.67"));
    }

    @Test
    public void testToCharacter() {
        // Test with Character object
        Character charValue = 'A';
        assertEquals(charValue, Cast.toCharacter(charValue));

        // Test with null
        assertNull(Cast.toCharacter(null));

        // Test with String (single character)
        assertEquals(Character.valueOf('B'), Cast.toCharacter("B"));

        // Test with Integer that can be converted to Character
        assertEquals(Character.valueOf('C'), Cast.toCharacter((int) 'C'));
    }

    @Test
    public void testToBoolean() {
        // Test with Boolean object
        Boolean boolValue = true;
        assertEquals(boolValue, Cast.toBoolean(boolValue));

        // Test with null - should return null
        assertNull(Cast.toBoolean(null));

        // Test with String
        assertTrue(Cast.toBoolean("true"));
        assertFalse(Cast.toBoolean("false"));

        // Test with Integer
        assertTrue(Cast.toBoolean(1));
        assertFalse(Cast.toBoolean(0));
    }
}
