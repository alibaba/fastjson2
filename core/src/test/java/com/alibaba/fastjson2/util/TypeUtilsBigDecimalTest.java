package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TypeUtilsBigDecimalTest {
    @Test
    public void doubleUsesCanonicalStringRepresentation() {
        double value = 0.00054797D;
        BigDecimal expected = BigDecimal.valueOf(value);

        assertEquals(expected, TypeUtils.toBigDecimal(value));
        assertEquals(expected, TypeUtils.toBigDecimal((Object) value));
        assertEquals(expected, JSONObject.of("value", value).getBigDecimal("value"));
        assertEquals(expected, JSONArray.of(value).getBigDecimal(0));
    }

    @Test
    public void finiteDoubleSamplesMatchBigDecimalValueOf() {
        Random random = new Random(20260822L);
        for (int i = 0; i < 10_000; i++) {
            double value = Double.longBitsToDouble(random.nextLong());
            if (Double.isFinite(value)) {
                assertEquals(BigDecimal.valueOf(value), TypeUtils.toBigDecimal(value));
            }
        }
    }

    @Test
    public void nonFiniteFloatingPointReturnsNull() {
        assertNull(TypeUtils.toBigDecimal(Float.NaN));
        assertNull(TypeUtils.toBigDecimal(Float.NEGATIVE_INFINITY));
        assertNull(TypeUtils.toBigDecimal(Float.POSITIVE_INFINITY));
        assertNull(TypeUtils.toBigDecimal(Double.NaN));
        assertNull(TypeUtils.toBigDecimal(Double.NEGATIVE_INFINITY));
        assertNull(TypeUtils.toBigDecimal(Double.POSITIVE_INFINITY));
    }
}
