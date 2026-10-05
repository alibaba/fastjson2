package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FacadeReviewTest {
    @Test
    void charsetOverloadsHonorFieldBasedFeature() {
        PrivateBean bean = new PrivateBean();
        assertEquals("{\"value\":7}", new String(JSON.toJSONBytes(bean, StandardCharsets.UTF_8,
                JSONWriter.Feature.FieldBased), StandardCharsets.UTF_8));
        JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.FieldBased);
        assertEquals("{\"value\":7}", new String(JSON.toJSONBytes(bean, StandardCharsets.UTF_8, context), StandardCharsets.UTF_8));
    }

    @Test
    void dumpDecimalWithFullInt32UnscaledValue() {
        BigDecimal value = new BigDecimal("1234567.89");
        assertEquals(value, new BigDecimal(JSONB.toJSONString(JSONB.toBytes(value))));
    }

    @Test
    void symbolTableOverloadsApplyFeatures() {
        SymbolTable symbols = new SymbolTable("value");
        assertEquals(JSONObject.of("value", 7), JSONB.parse(JSONB.toBytes(
                new PrivateBean(), symbols, JSONWriter.Feature.FieldBased), symbols));
        assertEquals(JSONObject.of("value", 7), JSONB.parse(JSONB.toBytes(
                new PrivateBean(), JSONFactory.createWriteContext(), symbols, JSONWriter.Feature.FieldBased), symbols));
    }

    @Test
    void compactIntRangePredicateMatchesExplicitBounds() {
        for (int value : new int[] {Integer.MIN_VALUE, -2049, -2048, -1, 0, 2047, 2048, Integer.MAX_VALUE}) {
            assertEquals(JSONB.isInt32ByteValue1(value), JSONB.isInt32ByteValue(value));
        }
    }

    @Test
    void nullStringsHaveEnoughBinaryCapacity() {
        String[] values = new String[100];
        byte[] bytes = new byte[JSONB.IO.stringCapacity(values)];
        int end = JSONB.IO.writeString(bytes, 0, values, 0);
        assertEquals(100, JSONB.parseArray(java.util.Arrays.copyOf(bytes, end)).size());
    }

    public static class PrivateBean {
        private int value = 7;
    }
}
