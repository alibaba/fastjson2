package com.alibaba.fastjson2.adapter.jackson.databind.node;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayNodeTest {
    @Test
    public void test() {
        ArrayNode array = new ArrayNode();
        assertEquals(0, array.size());
        assertEquals("[]", array.asText());
    }

    @Test
    public void testDecimal() {
        ArrayNode array = new ArrayNode();
        array.add(BigDecimal.ONE);
        assertEquals("[1]", array.toString());
    }

    @Test
    public void testBigInteger() {
        ArrayNode array = new ArrayNode();
        array.add(BigInteger.ONE);
        assertEquals("[1]", array.toString());
    }

    @Test
    public void testLong() {
        ArrayNode array = new ArrayNode();
        array.add(Long.valueOf(1));
        assertEquals("[1]", array.toString());
    }

    @Test
    public void testInteger() {
        ArrayNode array = new ArrayNode();
        array.add(Integer.valueOf(1));
        assertEquals("[1]", array.toString());
    }

    @Test
    public void testShort() {
        ArrayNode array = new ArrayNode();
        array.add(Short.valueOf((short) 1));
        assertEquals("[1]", array.toString());
    }

    @Test
    public void testByte() {
        ArrayNode array = new ArrayNode();
        array.add(Byte.valueOf((byte) 1));
        assertEquals("[1]", array.toString());
    }

    @Test
    public void testBoolean() {
        ArrayNode array = new ArrayNode();
        array.add(Boolean.TRUE);
        assertEquals("[true]", array.toString());
    }

    @Test
    public void testBooleanValue() {
        ArrayNode array = new ArrayNode();
        array.add(true);
        assertEquals("[true]", array.toString());
    }

    @Test
    public void testFloat() {
        ArrayNode array = new ArrayNode();
        array.add(12.34F);
        assertEquals("[12.34]", array.toString());
    }

    @Test
    public void testDouble() {
        ArrayNode array = new ArrayNode();
        array.add(12.34D);
        assertEquals("[12.34]", array.toString());
    }

    @Test
    public void testString() {
        ArrayNode array = new ArrayNode();
        array.add("abc");
        assertEquals("[\"abc\"]", array.toString());
    }
}
