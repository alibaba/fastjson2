package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONBTest;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NumberArrayTest {
    @Test
    public void test_parse_null() {
        Number[] values = JSON.parseObject("null", Number[].class);
        assertNull(values);
    }

    @Test
    public void test_parse_null_jsonb() {
        Number[] values = JSONB.parseObject(JSONB.toBytes((Map) null), Number[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        Number[] bytes = JSON.parseObject("[101,102]", Number[].class);
        assertEquals(2, bytes.length);
        assertEquals(101, bytes[0]);
        assertEquals(102, bytes[1]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{0, 1, null, 102, 1001}));
        Number[] array = JSONB.parseObject(jsonbBytes, Number[].class);
        assertEquals(5, array.length);
        assertEquals(0, array[0]);
        assertEquals(1, array[1]);
        assertEquals(null, array[2]);
        assertEquals(102, array[3]);
        assertEquals(1001, array[4]);
    }

    @Test
    public void test_1_jsonb() {
        Number[] values = new Number[]{
                Long.MIN_VALUE,
                Integer.MIN_VALUE,
                Short.MAX_VALUE,
                JSONBTest.INT24_MAX,
                0,
                null,
                BigDecimal.ZERO,
                BigDecimal.valueOf(Short.MAX_VALUE),
                BigDecimal.valueOf(JSONBTest.INT24_MAX),
                BigDecimal.valueOf(Integer.MAX_VALUE),
                BigDecimal.valueOf(Long.MAX_VALUE),
                new BigDecimal("12345678901234567890123456789012345678901234567890123456789012345678901234567890"),
                BigInteger.TEN
        };
        for (Number value : values) {
            Number1 vo = new Number1();
            vo.setValue(value);

            byte[] jsonbBytes = JSONB.toBytes(vo);
            Number1 vo2 = JSONB.parseObject(jsonbBytes, Number1.class);
            assertEquals(JSON.toJSONString(vo.getValue()),
                    JSON.toJSONString(vo2.getValue()));

            JSONBDump.dump(jsonbBytes);

            JSONObject jsonObject = JSONB.parseObject(jsonbBytes, JSONObject.class);
            assertEquals(JSON.toJSONString(vo.getValue()),
                    JSON.toJSONString(jsonObject.get("value")));
        }

        {
            byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", "123"));
            Number1 vo2 = JSONB.parseObject(jsonbBytes, Number1.class);
            assertEquals("123",
                    JSON.toJSONString(vo2.getValue()));
        }
    }

    @Test
    public void test_1_jsonb_1() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", "12345678901234567890123456789012345678901234567890123456789012345678901234567890"));
        Number1 vo2 = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals("12345678901234567890123456789012345678901234567890123456789012345678901234567890",
                JSON.toJSONString(vo2.getValue()));
    }

    @Test
    public void test_1_jsonb_1_bytes() {
        byte[] jsonbBytes = {-90, 78, 118, 97, 108, 117, 101, 122, 56, 80, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, -91};
        Number1 vo2 = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals("12345678901234567890123456789012345678901234567890123456789012345678901234567890",
                JSON.toJSONString(vo2.getValue()));
    }

    @Test
    public void test_byte() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", 100));
        Number1 vo = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals(Integer.valueOf(100), vo.getValue());
    }

    @Test
    public void test_decimal_1() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", new BigDecimal("1.1")));
        Number1 vo = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals(new BigDecimal("1.1"), vo.getValue());
    }

    @Test
    public void test_decimal_2() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", new BigDecimal("200.1")));
        Number1 vo = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals(new BigDecimal("200.1"), vo.getValue());
    }

    @Test
    public void test_decimal_3() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", new BigDecimal("65536.1")));
        Number1 vo = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals(new BigDecimal("65536.1"), vo.getValue());
    }

    @Test
    public void test_decimal_4() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", new BigDecimal("6388607.1")));
        Number1 vo = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals(new BigDecimal("6388607.1"), vo.getValue());
    }

    @Test
    public void test_decimal_5() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", new BigDecimal("2147483648.1")));
        Number1 vo = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals(new BigDecimal("2147483648.1"), vo.getValue());
    }

    @Test
    public void test_decimal_6() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("value", new BigDecimal("9223372036854775807.1")));
        Number1 vo = JSONB.parseObject(jsonbBytes, Number1.class);
        assertEquals(new BigDecimal("9223372036854775807.1"), vo.getValue());
    }

    public static class Number1 {
        private Number value;

        public Number getValue() {
            return value;
        }

        public void setValue(Number value) {
            this.value = value;
        }
    }
}
