package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FloatNodeTest {
    @Test
    public void test() {
        Float val = 1F;
        FloatNode node = FloatNode.valueOf(val);
        assertFalse(node.isBigInteger());
        assertFalse(node.isBigDecimal());
        assertTrue(node.isFloat());
        assertFalse(node.isDouble());
        assertEquals(JsonParser.NumberType.FLOAT, node.numberType());
        assertEquals(JsonNodeType.NUMBER, node.getNodeType());
        assertEquals(val.intValue(), node.asInt());
        assertEquals(val.intValue(), node.asInt(2));
        assertEquals(val.intValue(), node.intValue());
        assertEquals(val.longValue(), node.longValue());
        assertEquals(val.doubleValue(), node.asDouble());
    }
}
