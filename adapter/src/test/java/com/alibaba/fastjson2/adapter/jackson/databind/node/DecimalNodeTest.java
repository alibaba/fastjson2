package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecimalNodeTest {
    @Test
    public void test() {
        BigDecimal dec = BigDecimal.ONE;
        DecimalNode node = DecimalNode.valueOf(dec);
        assertTrue(node.isBigDecimal());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, node.numberType());
        assertEquals(JsonNodeType.NUMBER, node.getNodeType());
        assertEquals(dec.intValue(), node.asInt());
        assertEquals(dec.intValue(), node.asInt(2));
        assertEquals(dec.intValue(), node.intValue());
        assertEquals(dec.longValue(), node.longValue());
        assertEquals(dec.doubleValue(), node.asDouble());
    }
}
