package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2_vo.Double1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoubleTest {
    private Double[] values = new Double[100];
    private int off;

    public DoubleTest() {
        values[off++] = null;
        values[off++] = (double) Float.MIN_VALUE;
        values[off++] = (double) Float.MAX_VALUE;
//        values[off++] = Double.MIN_VALUE;
//        values[off++] = Double.MAX_VALUE;
        values[off++] = (double) Integer.MIN_VALUE;
        values[off++] = (double) Integer.MAX_VALUE;
        values[off++] = (double) Short.MIN_VALUE;
        values[off++] = (double) Short.MAX_VALUE;
        values[off++] = (double) JSONBTest.INT24_MIN;
        values[off++] = (double) JSONBTest.INT24_MAX;
        values[off++] = -1D;
        values[off++] = -10D;
        values[off++] = -100D;
        values[off++] = -1000D;
        values[off++] = -10000D;
        values[off++] = -100000D;
        values[off++] = -1000000D;
        values[off++] = -10000000D;
        values[off++] = -100000000D;
        values[off++] = -1000000000D;
        values[off++] = 1D;
        values[off++] = 10D;
        values[off++] = 100D;
        values[off++] = 1000D;
        values[off++] = 10000D;
        values[off++] = 100000D;
        values[off++] = 1000000D;
        values[off++] = 10000000D;
        values[off++] = 100000000D;
        values[off++] = 1000000000D;
        values[off++] = -9D;
        values[off++] = -99D;
        values[off++] = -999D;
        values[off++] = -9999D;
        values[off++] = -99999D;
        values[off++] = -999999D;
        values[off++] = -9999999D;
        values[off++] = -99999999D;
        values[off++] = -999999999D;
        values[off++] = 9D;
        values[off++] = 99D;
        values[off++] = 999D;
        values[off++] = 9999D;
        values[off++] = 99999D;
        values[off++] = 999999D;
        values[off++] = 9999999D;
        values[off++] = 99999999D;
        values[off++] = 999999999D;
    }

    @Test
    public void test_jsonb() {
        for (Double id : values) {
            Double1 vo = new Double1();
            vo.setV0000(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Double1 v1 = JSONB.parseObject(jsonbBytes, Double1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (Double id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            Double id2 = JSONB.parseObject(jsonbBytes, Double.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        Double[] id2 = JSONB.parseObject(jsonbBytes, Double[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_jsonb_array3() {
        Double[] primitiveValues = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                primitiveValues[i] = 0D;
                continue;
            }
            primitiveValues[i] = values[i];
        }
        byte[] jsonbBytes = JSONB.toBytes(primitiveValues);
        Double[] id2 = JSONB.parseObject(jsonbBytes, Double[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_jsonb_value_cast() {
        Number[] values = new Number[]{
                0,
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                Short.MIN_VALUE, Short.MAX_VALUE,
                JSONBTest.INT24_MIN, JSONBTest.INT24_MAX,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE,
                new BigDecimal(".123456789012345678901234567890123456789001234567890"),
                new BigDecimal("-.123456789012345678901234567890123456789001234567890"),
                new BigDecimal("123456789012345678901234567890123456789001234567890."),
                new BigDecimal("-123456789012345678901234567890123456789001234567890."),
        };

        for (Number value : values) {
            byte[] jsonbBytes = JSONB.toBytes(value);
            Double id2 = JSONB.parseObject(jsonbBytes, Double.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_jsonb_value_cast_str() {
        Number[] values = new Number[]{
                0,
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                Short.MIN_VALUE, Short.MAX_VALUE,
                JSONBTest.INT24_MIN, JSONBTest.INT24_MAX,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE,
                new BigDecimal(".123456789012345678901234567890123456789001234567890"),
                new BigDecimal("-.123456789012345678901234567890123456789001234567890"),
                new BigDecimal("123456789012345678901234567890123456789001234567890."),
                new BigDecimal("-123456789012345678901234567890123456789001234567890."),
        };

        for (Number value : values) {
            byte[] jsonbBytes = JSONB.toBytes(value.toString());
            Double id2 = JSONB.parseObject(jsonbBytes, Double.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_utf8() {
        for (Double id : values) {
            Double1 vo = new Double1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Double1 v1 = JSON.parseObject(utf8Bytes, Double1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8_value() {
        for (Double id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Double id2 = JSON.parseObject(utf8Bytes, Double.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Double[] id2 = JSON.parseObject(utf8Bytes, Double[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_str() {
        for (Double id : values) {
            Double1 vo = new Double1();
            vo.setV0000(id);
            String str = JSON.toJSONString(vo);

            Double1 v1 = JSON.parseObject(str, Double1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str_value() {
        for (Double id : values) {
            String str = JSON.toJSONString(id);
            Double id2 = JSON.parseObject(str, Double.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str_array2() {
        double[] primitiveValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                primitiveValues[i] = 0D;
                continue;
            }
            primitiveValues[i] = values[i];
        }
        String str = JSON.toJSONString(primitiveValues);
        double[] id2 = JSON.parseObject(str, double[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_str_array3() {
        Double[] primitiveValues = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                primitiveValues[i] = 0D;
                continue;
            }
            primitiveValues[i] = values[i];
        }
        String str = JSON.toJSONString(primitiveValues);
        Double[] id2 = JSON.parseObject(str, Double[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_str_array() {
        String str = JSON.toJSONString(values);
        Double[] id2 = JSON.parseObject(str, Double[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_ascii() {
        for (Double id : values) {
            Double1 vo = new Double1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Double1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Double1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii_value() {
        for (Double id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Double id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Double.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Double[] id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Double[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_float() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("v0000", 1F));
        Double1 vo = JSONB.parseObject(jsonbBytes, Double1.class);
        assertEquals(Double.valueOf(1), vo.getV0000());
    }

    @Test
    public void parseDouble0() {
        String str = "12345.6789";
        char[] chars = str.toCharArray();
        double d0 = TypeUtils.parseDouble(chars, 0, chars.length);
        assertEquals(Double.parseDouble(str), d0);
    }

    @Test
    public void parseDouble1() {
        String str = "123.123E256";
        double expected = Double.parseDouble(str);

        assertEquals(expected, JSON.parseObject(str, Double.class));
        assertEquals(expected, (Double) JSON.parse(str));
        assertEquals(expected, JSON.parseObject(str, BigDecimal.class).doubleValue());

        byte[] bytes = str.getBytes();
        assertEquals(expected, JSON.parseObject(bytes, Double.class));
        assertEquals(expected, (Double) JSON.parse(bytes));
        assertEquals(expected, JSON.parseObject(bytes, BigDecimal.class).doubleValue());

        char[] chars = str.toCharArray();
        assertEquals(expected, JSONReader.of(chars).readDoubleValue());
        assertEquals(expected, (Double) JSONReader.of(chars).readNumber());
        assertEquals(expected, JSONReader.of(chars).readBigDecimal().doubleValue());
    }
}
