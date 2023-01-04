package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectNodeTest {
    @Test
    public void test() {
        ObjectNode object = new ObjectNode();
        object.removeAll();
        assertEquals("{}", object.getJSONObject().toString());
        assertEquals("{}", object.asText());
        assertEquals(0, object.size());

        object.put("flag", true);
        BooleanNode flag = (BooleanNode) object.get("flag");
        assertTrue(flag.booleanValue());
        assertEquals("true", flag.asText());
        assertEquals("true", flag.toString());
    }

    @Test
    public void testShort() {
        ObjectNode object = new ObjectNode();
        short value = Short.MAX_VALUE;
        object.put("value", value);
        assertEquals(1, object.size());
        ShortNode node = (ShortNode) object.get("value");
        assertEquals(value, node.getValue());
        assertEquals(Short.toString(value), node.asText());
        assertEquals(Short.toString(value), node.toString());
    }

    @Test
    public void testInteger() {
        ObjectNode object = new ObjectNode();
        int value = Integer.MAX_VALUE;
        object.put("value", value);
        IntegerNode node = (IntegerNode) object.get("value");
        assertEquals(value, node.getValue());
        assertEquals(Integer.toString(value), node.asText());
        assertEquals(Integer.toString(value), node.toString());
    }

    @Test
    public void testLong() {
        ObjectNode object = new ObjectNode();
        long value = 123;
        object.put("value", value);
        LongNode node = (LongNode) object.get("value");
        assertEquals(value, node.getValue());
        assertEquals(Long.toString(value), node.asText());
        assertEquals(Long.toString(value), node.toString());
    }

    @Test
    public void testFloat() {
        ObjectNode object = new ObjectNode();
        float value = 123.45F;
        object.put("value", value);
        FloatNode node = (FloatNode) object.get("value");
        assertEquals(value, node.getValue());
        assertEquals(Float.toString(value), node.asText());
        assertEquals(Float.toString(value), node.toString());
    }

    @Test
    public void testDouble() {
        ObjectNode object = new ObjectNode();
        double value = 123.45D;
        object.put("value", value);
        DoubleNode node = (DoubleNode) object.get("value");
        assertEquals(value, node.getValue());
        assertEquals(Double.toString(value), node.asText());
        assertEquals(Double.toString(value), node.toString());
    }

    @Test
    public void testDecimal() {
        ObjectNode object = new ObjectNode();
        BigDecimal value = BigDecimal.valueOf(123.45);
        object.put("value", value);
        DecimalNode node = (DecimalNode) object.get("value");
        assertEquals(value, node.getValue());
        assertEquals(value.toPlainString(), node.asText());
        assertEquals(value.toString(), node.toString());
    }

    @Test
    public void testBigInteger() {
        ObjectNode object = new ObjectNode();
        BigInteger value = BigInteger.valueOf(123);
        object.put("value", value);
        BigIntegerNode node = (BigIntegerNode) object.get("value");
        assertEquals(value, node.getValue());
        assertEquals(value.toString(), node.asText());
        assertEquals(value.toString(), node.toString());
    }

    @Test
    public void testString() {
        ObjectNode object = new ObjectNode();
        String value = "abc";
        object.put("value", value);
        TextNode node = (TextNode) object.get("value");
        assertEquals(value, node.getValue());
        assertEquals(value, node.asText());
        assertEquals(JSON.toJSONString(value), node.toString());
    }
}
