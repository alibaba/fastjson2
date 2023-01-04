package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XxHash64Test {
    @Test
    public void test() {
        assertEquals(
                XxHash64.hash(1),
                XxHash64.hash(1F)
        );
        assertEquals(
                XxHash64.hash(1),
                XxHash64.hash(1D)
        );
    }

    @Test
    public void test1() {
        assertEquals(
                XxHash64.hash(1),
                XxHash64.hash(BigInteger.ONE)
        );
        assertEquals(
                XxHash64.hash("92233720368547758079223372036854775807"),
                XxHash64.hash(new BigInteger("92233720368547758079223372036854775807"))
        );
    }

    @Test
    public void testDecimal() {
        assertEquals(
                XxHash64.hash(1),
                XxHash64.hash(BigDecimal.ONE)
        );
        assertEquals(
                XxHash64.hash("2.1"),
                XxHash64.hash(new BigDecimal("2.1"))
        );
        assertEquals(
                XxHash64.hash(new BigDecimal("2.100")),
                XxHash64.hash(new BigDecimal("2.1000"))
        );
    }

    @Test
    public void test2() {
        System.out.println(12345 ^ 23456);
        System.out.println(23456 ^ 12345);
    }
}
