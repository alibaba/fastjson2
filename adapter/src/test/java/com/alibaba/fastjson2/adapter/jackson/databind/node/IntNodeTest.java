package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class IntNodeTest {
    @Test
    public void test() {
        Integer val = 1;
        IntNode node = IntNode.valueOf(val);
        assertFalse(node.isBigInteger());
        assertFalse(node.isBigDecimal());
        assertFalse(node.isFloat());
        assertFalse(node.isDouble());
        assertEquals(JsonParser.NumberType.INT, node.numberType());
        assertEquals(JsonNodeType.NUMBER, node.getNodeType());
        assertEquals(val.intValue(), node.asInt());
        assertEquals(val.intValue(), node.asInt(2));
        assertEquals(val.intValue(), node.intValue());
        assertEquals(val.longValue(), node.longValue());
        assertEquals(val.floatValue(), node.floatValue());
        assertEquals(val.doubleValue(), node.doubleValue());
        assertEquals(val.doubleValue(), node.asDouble());
        assertEquals(BigInteger.ONE, node.bigIntegerValue());
    }
}
