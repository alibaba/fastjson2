package com.alibaba.fastjson2.stream;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ColumnStatTest {
    @Test
    public void test() {
        String value = "123.45";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(5, stat.precision);
            assertEquals(2, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(0, stat.integers);
        }
        {
            byte[] bytes = value.getBytes();
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(5, stat.precision);
            assertEquals(2, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(0, stat.integers);
        }
    }

    @Test
    public void test1() {
        String value = "12345";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(5, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(1, stat.integers);

            assertSame(Integer.class, stat.getInferType());
        }
        {
            byte[] bytes = value.getBytes();
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(5, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(1, stat.integers);

            assertSame(Integer.class, stat.getInferType());
        }
    }

    @Test
    public void testLong() {
        String value = "12345678901";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(11, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(1, stat.integers);

            assertSame(Long.class, stat.getInferType());
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(11, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(1, stat.integers);

            assertSame(Long.class, stat.getInferType());
        }
    }

    @Test
    public void testBigInteger() {
        String value = "12345678901234567890";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(20, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(1, stat.integers);

            assertSame(BigInteger.class, stat.getInferType());
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(20, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(1, stat.integers);

            assertSame(BigInteger.class, stat.getInferType());
        }
    }

    @Test
    public void testNum() {
        String value = "-14.258899";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(8, stat.precision);
            assertEquals(6, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(0, stat.integers);

            assertSame(BigDecimal.class, stat.getInferType());
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(8, stat.precision);
            assertEquals(6, stat.scale);
            assertEquals(1, stat.numbers);
            assertEquals(0, stat.integers);

            assertSame(BigDecimal.class, stat.getInferType());
        }
    }

    @Test
    public void test2() {
        String value = "abcdef";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(6, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);

            assertSame(String.class, stat.getInferType());
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(6, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);

            assertSame(String.class, stat.getInferType());
        }
    }

    @Test
    public void testDate() {
        String value = "04/03/2023 12:00:00 AM";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(0, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(0, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
    }

    @Test
    public void testDate1() {
        String value = "2023-04-03 12:00:00";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(0, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);

            assertSame(java.util.Date.class, stat.getInferType());
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(0, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);

            assertSame(java.util.Date.class, stat.getInferType());
        }
    }

    @Test
    public void testDateP1() {
        String value = "2023-04-03 12:00:00.1";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(1, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);

            assertSame(java.time.Instant.class, stat.getInferType());
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(1, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);

            assertSame(java.time.Instant.class, stat.getInferType());
        }
    }

    @Test
    public void testDateP2() {
        String value = "2023-04-03 12:00:00.12";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(2, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);

            assertSame(java.time.Instant.class, stat.getInferType());
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(2, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);

            assertSame(java.time.Instant.class, stat.getInferType());
        }
    }

    @Test
    public void testDateP3() {
        String value = "2023-04-03 12:00:00.123";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(3, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(3, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
    }

    @Test
    public void testDateP4() {
        String value = "2023-04-03 12:00:00.1234";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(4, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(4, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
    }

    @Test
    public void testDateP5() {
        String value = "2023-04-03 12:00:00.12345";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(5, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(5, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
    }

    @Test
    public void testDateP6() {
        String value = "2023-04-03 12:00:00.123456";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(6, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(6, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
    }

    @Test
    public void testDateP7() {
        String value = "2023-04-03 12:00:00.1234567";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(7, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(7, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
    }

    @Test
    public void testDateP8() {
        String value = "2023-04-03 12:00:00.12345678";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(8, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(8, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
    }

    @Test
    public void testDateP9() {
        String value = "2023-04-03 12:00:00.123456789";
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            stat.stat(value);
            assertEquals(9, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
        {
            StreamReader.ColumnStat stat = new StreamReader.ColumnStat(null);
            byte[] bytes = value.getBytes();
            stat.stat(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(9, stat.precision);
            assertEquals(0, stat.scale);
            assertEquals(0, stat.numbers);
            assertEquals(0, stat.integers);
            assertEquals(1, stat.dates);
        }
    }
}
