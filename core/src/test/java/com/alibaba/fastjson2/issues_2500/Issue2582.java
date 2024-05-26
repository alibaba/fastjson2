package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2582 {
    @Test
    public void testAddOne() {
        BigDecimal decimal = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE);
        String str = JSON.toJSONString(decimal);
        assertEquals("9223372036854775808", str);
        assertEquals(decimal, JSON.parseObject(str, BigDecimal.class));
        assertEquals(decimal, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), BigDecimal.class));
        assertEquals(decimal, JSONB.parse(JSONB.toBytes(decimal)));
    }

    @Test
    public void testAddTwo() {
        BigDecimal decimal = BigDecimal.valueOf(Long.MAX_VALUE).add(new BigDecimal("2"));
        String str = JSON.toJSONString(decimal);
        assertEquals("9223372036854775809", str);
        assertEquals(decimal, JSON.parseObject(str, BigDecimal.class));
        assertEquals(decimal, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), BigDecimal.class));
        assertEquals(decimal, JSONB.parse(JSONB.toBytes(decimal)));
    }

    @Test
    public void testAddThree() {
        BigDecimal decimal = BigDecimal.valueOf(Long.MAX_VALUE).add(new BigDecimal("3"));
        String str = JSON.toJSONString(decimal);
        assertEquals("9223372036854775810", str);
        assertEquals(decimal, JSON.parseObject(str, BigDecimal.class));
        assertEquals(decimal, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), BigDecimal.class));
        assertEquals(decimal, JSONB.parse(JSONB.toBytes(decimal)));
    }
}
