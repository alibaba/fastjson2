package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class Issue1537 {
    @Test
    public void testSingle() {
        // default
        assertEquals("true", JSON.toJSONString(true));
        assertEquals("123", JSON.toJSONString(123));
        assertEquals("123", JSON.toJSONString(Integer.valueOf("123")));
        assertEquals("3.14", JSON.toJSONString(3.14));
        assertEquals("3.14", JSON.toJSONString(Double.valueOf("3.14")));
        assertEquals("3", JSON.toJSONString(Long.valueOf("3")));
        assertEquals("3", JSON.toJSONString(new BigInteger("3")));
        assertEquals("3", JSON.toJSONString(new BigDecimal("3")));
        assertEquals("3.14", JSON.toJSONString(new BigDecimal("3.14")));

        // as string
        assertEquals("true", JSON.toJSONString(true, JSONWriter.Feature.WriteNonStringValueAsString));
        assertEquals("\"123\"", JSON.toJSONString(123, JSONWriter.Feature.WriteNonStringValueAsString));
        assertEquals("\"123\"", JSON.toJSONString(Integer.valueOf("123"), JSONWriter.Feature.WriteNonStringValueAsString));
        assertEquals("\"3.14\"", JSON.toJSONString(3.14, JSONWriter.Feature.WriteNonStringValueAsString));
        assertEquals("\"3.14\"", JSON.toJSONString(Double.valueOf("3.14"), JSONWriter.Feature.WriteNonStringValueAsString));
        assertEquals("\"3\"", JSON.toJSONString(Long.valueOf("3"), JSONWriter.Feature.WriteNonStringValueAsString));
        assertEquals("\"3\"", JSON.toJSONString(new BigInteger("3"), JSONWriter.Feature.WriteNonStringValueAsString));
        assertEquals("\"3\"", JSON.toJSONString(new BigDecimal("3"), JSONWriter.Feature.WriteNonStringValueAsString));
        assertEquals("\"3.14\"", JSON.toJSONString(new BigDecimal("3.14"), JSONWriter.Feature.WriteNonStringValueAsString));
    }
}
