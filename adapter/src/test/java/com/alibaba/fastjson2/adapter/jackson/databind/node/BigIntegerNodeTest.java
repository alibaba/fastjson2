package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BigIntegerNodeTest {
    @Test
    public void test() {
        BigInteger bigInt = BigInteger.ONE;
        BigIntegerNode node = BigIntegerNode.valueOf(bigInt);
        assertFalse(node.isBigDecimal());
        assertTrue(node.isBigInteger());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, node.numberType());
        assertEquals(JsonNodeType.NUMBER, node.getNodeType());
        assertEquals(bigInt.intValue(), node.asInt());
        assertEquals(bigInt.intValue(), node.asInt(2));
        assertEquals(bigInt.intValue(), node.intValue());
        assertEquals(bigInt.longValue(), node.longValue());
        assertEquals(bigInt.doubleValue(), node.asDouble());
    }
}
