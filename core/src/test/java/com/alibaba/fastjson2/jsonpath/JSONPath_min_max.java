package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPath_min_max {
    @Test
    public void test_0() {
        Object[] array = new Object[]{1, 2, 3, 4};
        assertEquals(1, JSONPath.of("$.min()").eval(array));
        assertEquals(4, JSONPath.of("$.max()").eval(array));
    }

    @Test
    public void test_1() {
        Object[] array = new Object[]{"1", 2f, 3D, 4};
        assertEquals("\"1\"",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(array)));
        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(array)));
    }

    @Test
    public void test_2() {
        Object[] array = new Object[]{"21474836480", 2f, 3D, 4};
        assertEquals("2.0",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(array)));
        assertEquals("\"21474836480\"",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(array)));
    }

    @Test
    public void test_3() {
        Object[] array = new Object[]{"214748364802147483648021474836480", 3D, 4};
        assertEquals("3.0",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(array)));
        assertEquals("\"214748364802147483648021474836480\"",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(array)));
    }

    @Test
    public void test_4() {
        Object[] array = new Object[]{
                "214748364802147483648021474836480",
                BigInteger.valueOf(3),
                BigDecimal.valueOf(4), 5F, 6D, 7, 8L, BigInteger.valueOf(9), BigDecimal.valueOf(10), 11L, 12D};
        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(array)));
        assertEquals("\"214748364802147483648021474836480\"",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(array)));
    }

    @Test
    public void test_5() {
        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), 3})));
        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), 3})));

        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), 3L})));
        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), 3L})));

        assertEquals("3.0",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), 3F})));
        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), 3F})));

        assertEquals("3.0",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), 3D})));
        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), 3D})));

        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), BigInteger.valueOf(3)})));
        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{BigDecimal.valueOf(4), BigInteger.valueOf(3)})));
    }

    @Test
    public void test_6() {
        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{4L, 3})));

        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4L, 3})));

        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{4L, BigDecimal.valueOf(3)})));

        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4L, BigInteger.valueOf(3)})));

        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4L, 3F})));
        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4L, 3D})));

        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4L, "3"})));
    }

    @Test
    public void test_7() {
        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{4, 3L})));

        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4, 3})));

        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{4, BigDecimal.valueOf(3)})));

        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4, BigInteger.valueOf(3)})));

        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4, 3F})));
        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4, 3D})));

        assertEquals("4",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4, "3"})));
    }

    @Test
    public void test_8_float() {
        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{4F, 3L})));

        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4F, 3})));

        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{4F, BigDecimal.valueOf(3)})));

        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4F, BigInteger.valueOf(3)})));

        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4F, 3F})));
        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4F, 3D})));

        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4F, "3"})));
    }

    @Test
    public void test_9_double() {
        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{4D, 3L})));

        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4D, 3})));

        assertEquals("3",
                JSON.toJSONString(
                        JSONPath.of("$.min()")
                                .eval(
                                        new Object[]{4D, BigDecimal.valueOf(3)})));

        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4D, BigInteger.valueOf(3)})));

        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4D, 3F})));

        assertEquals("4.0",
                JSON.toJSONString(
                        JSONPath.of("$.max()")
                                .eval(
                                        new Object[]{4D, "3"})));
    }
}
