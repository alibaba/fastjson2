package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1652 {
    @Test
    public void testDecimal() {
        BigDecimal decimal = new BigDecimal("25.000000000000000000");
        String s2 = JSON.toJSONString(decimal);
        assertEquals(decimal, JSON.parseObject(s2, BigDecimal.class));
        assertEquals(decimal, JSON.parseObject(s2.getBytes(), BigDecimal.class));
    }

    @Test
    public void testDecimal1() {
        BigDecimal decimal = new BigDecimal("25000000000000000000");
        String s2 = JSON.toJSONString(decimal);
        assertEquals(decimal, JSON.parseObject(s2, BigDecimal.class));
        assertEquals(decimal, JSON.parseObject(s2.getBytes(), BigDecimal.class));
    }

    @Test
    public void testLong() {
        long val = 2500000000000000000L;
        String s2 = JSON.toJSONString(val);
        assertEquals(val, JSON.parseObject(s2, Long.class));
        assertEquals(val, JSON.parseObject(s2.getBytes(), Long.class));
    }

    @Test
    public void testBigInteger() {
        BigInteger bigInt = new BigInteger("25000000000000000000");
        String s2 = JSON.toJSONString(bigInt);
        assertEquals(bigInt, JSON.parseObject(s2, BigInteger.class));
        assertEquals(bigInt, JSON.parseObject(s2.getBytes(), BigInteger.class));
    }
}
