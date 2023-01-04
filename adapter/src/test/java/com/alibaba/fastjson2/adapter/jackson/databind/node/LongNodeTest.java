package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class LongNodeTest {
    @Test
    public void test() {
        Long val = 1L;
        LongNode node = LongNode.valueOf(val);
        assertFalse(node.isBigInteger());
        assertFalse(node.isBigDecimal());
        assertFalse(node.isFloat());
        assertFalse(node.isDouble());
        assertEquals(JsonParser.NumberType.LONG, node.numberType());
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
