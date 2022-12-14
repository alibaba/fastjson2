package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DoubleNodeTest {
    @Test
    public void test() {
        Double val = 1D;
        DoubleNode node = DoubleNode.valueOf(val);
        assertFalse(node.isBigInteger());
        assertFalse(node.isBigDecimal());
        assertFalse(node.isFloat());
        assertTrue(node.isDouble());
        assertEquals(JsonParser.NumberType.DOUBLE, node.numberType());
        assertEquals(JsonNodeType.NUMBER, node.getNodeType());
        assertEquals(val.intValue(), node.asInt());
        assertEquals(val.intValue(), node.asInt(2));
        assertEquals(val.intValue(), node.intValue());
        assertEquals(val.longValue(), node.longValue());
        assertEquals(val.doubleValue(), node.asDouble());
    }
}
