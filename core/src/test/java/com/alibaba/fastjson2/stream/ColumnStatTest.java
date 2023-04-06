package com.alibaba.fastjson2.stream;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ColumnStatTest {
    @Test
    public void test() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("123.45");
        assertEquals(5, stat.precision);
        assertEquals(2, stat.scale);
        assertEquals(1, stat.numbers);
        assertEquals(0, stat.integers);
    }

    @Test
    public void test1() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("12345");
        assertEquals(5, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(1, stat.numbers);
        assertEquals(1, stat.integers);

        assertSame(Integer.class, stat.getInferType());
    }

    @Test
    public void testLong() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("12345678901");
        assertEquals(11, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(1, stat.numbers);
        assertEquals(1, stat.integers);

        assertSame(Long.class, stat.getInferType());
    }

    @Test
    public void testBigInteger() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("12345678901234567890");
        assertEquals(20, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(1, stat.numbers);
        assertEquals(1, stat.integers);

        assertSame(BigInteger.class, stat.getInferType());
    }

    @Test
    public void testNum() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("-14.258899");
        assertEquals(8, stat.precision);
        assertEquals(6, stat.scale);
        assertEquals(1, stat.numbers);
        assertEquals(0, stat.integers);

        assertSame(BigDecimal.class, stat.getInferType());
    }

    @Test
    public void test2() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("abcdef");
        assertEquals(6, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);

        assertSame(String.class, stat.getInferType());
    }

    @Test
    public void testDate() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("04/03/2023 12:00:00 AM");
        assertEquals(0, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);
    }

    @Test
    public void testDate1() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00");
        assertEquals(0, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);

        assertSame(java.util.Date.class, stat.getInferType());
    }

    @Test
    public void testDateP1() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.1");
        assertEquals(1, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);

        assertSame(java.time.Instant.class, stat.getInferType());
    }

    @Test
    public void testDateP2() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.12");
        assertEquals(2, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);

        assertSame(java.time.Instant.class, stat.getInferType());
    }

    @Test
    public void testDateP3() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.123");
        assertEquals(3, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);
    }

    @Test
    public void testDateP4() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.1234");
        assertEquals(4, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);
    }

    @Test
    public void testDateP5() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.12345");
        assertEquals(5, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);
    }

    @Test
    public void testDateP6() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.123456");
        assertEquals(6, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);
    }

    @Test
    public void testDateP7() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.1234567");
        assertEquals(7, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);
    }

    @Test
    public void testDateP8() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.12345678");
        assertEquals(8, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);
    }

    @Test
    public void testDateP9() {
        StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
        stat.stat("2023-04-03 12:00:00.123456789");
        assertEquals(9, stat.precision);
        assertEquals(0, stat.scale);
        assertEquals(0, stat.numbers);
        assertEquals(0, stat.integers);
        assertEquals(1, stat.dates);
    }
}
