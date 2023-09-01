package com.alibaba.fastjson2.benchmark.fastcode;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecimalUtilsTest {
    @Test
    public void test() {
        assertEquals(
                "0.0123",
                DecimalUtils.toString(123, 4)
        );
        assertEquals(
                "12.3",
                DecimalUtils.toString(123, 1)
        );

        System.out.println(
                BigDecimal
                        .valueOf(123, 10)
                        .toString()
        );
        assertEquals("1.23E-8", DecimalUtils.layout(123, 10, true));
        assertEquals(
                "1230",
                DecimalUtils.toString(123, -1)
        );
    }

    @Test
    public void test1() {
        long unscaledVal = 1234567890123456789L;
        int scale = 2;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal);
        assertEquals(decimal.toString(), DecimalUtils.layout(unscaledVal, scale, true));
    }

    @Test
    public void test1_bigint() {
        long unscaledVal = 1234567890123456789L;
        int scale = 2;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal);
        assertEquals(decimal.toString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, true));
    }

    @Test
    public void test2() {
        long unscaledVal = 123;
        int scale = -100;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal);
        assertEquals(decimal.toString(), DecimalUtils.layout(unscaledVal, scale, true));
    }

    @Test
    public void test2_bigint() {
        long unscaledVal = 123;
        int scale = -100;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal);
        assertEquals(decimal.toString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, true));
    }

    @Test
    public void test3() {
        long unscaledVal = 12345678901234567L;
        int scale = 20;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal);
        assertEquals(decimal.toString(), DecimalUtils.layout(unscaledVal, scale, true));
    }

    @Test
    public void test3_bigint() {
        long unscaledVal = 12345678901234567L;
        int scale = 20;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal);
        assertEquals(decimal.toString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, true));
    }

    @Test
    public void test4() {
        long unscaledVal = 1;
        int scale = -100;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal);
        assertEquals(decimal.toString(), DecimalUtils.layout(unscaledVal, scale, true));
    }

    @Test
    public void test4_bigint() {
        long unscaledVal = 1;
        int scale = -100;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal);
        assertEquals(decimal.toString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, true));
    }

    @Test
    public void test4_bigint_sci() {
        long unscaledVal = 1;
        int scale = -100;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, false));
    }

    @Test
    public void test4_sci() {
        long unscaledVal = 1;
        int scale = -100;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(unscaledVal, scale, false));
    }

    @Test
    public void test5_sci_bigint() {
        long unscaledVal = 12345;
        int scale = -100;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, false));
    }

    @Test
    public void test5_sci() {
        long unscaledVal = 12345;
        int scale = -100;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(unscaledVal, scale, false));
    }

    @Test
    public void test_zero() {
        long unscaledVal = 0;
        int scale = 10;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(unscaledVal, scale, false));
    }

    @Test
    public void test_zero_bigint() {
        long unscaledVal = 0;
        int scale = 10;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, false));
    }

    @Test
    public void test_zero_sci() {
        long unscaledVal = 0;
        int scale = 10;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toString());
        assertEquals(decimal.toString(), DecimalUtils.layout(unscaledVal, scale, true));
    }

    @Test
    public void test_zero_1() {
        long unscaledVal = 0;
        int scale = 11;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(unscaledVal, scale, false));
    }

    @Test
    public void test_zero_1_bigint() {
        long unscaledVal = 0;
        int scale = 11;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, false));
    }

    @Test
    public void test_zero_2() {
        long unscaledVal = 0;
        int scale = 12;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(unscaledVal, scale, false));
    }

    @Test
    public void test_zero_2_bigint() {
        long unscaledVal = 0;
        int scale = 12;
        BigDecimal decimal = BigDecimal
                .valueOf(unscaledVal, scale);
        System.out.println(decimal.toEngineeringString());
        assertEquals(decimal.toEngineeringString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, false));
    }

    @Test
    public void test_multi() {
        long[] values = {
                1234567890123456789L, 2,
                -1234567890123456789L, 2,
                123, -100,
                -123, -100,
                12345678901234567L, 20,
                -12345678901234567L, 20,
                1, -100,
                -1, -100,
                12345, -100,
                -12345, -100,
                0, 10,
                0, 11,
                0, 12,
                100, 0,
                -100, 0,
                Long.MAX_VALUE, 0
        };

        for (int i = 0; i < values.length; i += 2) {
            long unscaledVal = values[i];
            int scale = (int) values[i + 1];

            BigDecimal decimal = BigDecimal.valueOf(unscaledVal, scale);
            assertEquals(decimal.toString(), DecimalUtils.layout(unscaledVal, scale, true));
            assertEquals(decimal.toString(), DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, true));
            assertEquals(
                    decimal.toEngineeringString(),
                    DecimalUtils.layout(unscaledVal, scale, false)
            );
            assertEquals(
                    decimal.toEngineeringString(),
                    DecimalUtils.layout(BigInteger.valueOf(unscaledVal), scale, false)
            );
            assertEquals(
                    decimal.toPlainString(),
                    DecimalUtils.toString(unscaledVal, scale)
            );
            assertEquals(
                    decimal.toPlainString(),
                    DecimalUtils.toString(BigInteger.valueOf(unscaledVal), scale)
            );
        }
    }
}
