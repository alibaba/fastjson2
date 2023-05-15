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

    @Test
    public void parseDouble2() {
        String str = "113.92966694974888";
        double d = Double.parseDouble(str);
        double v = JSONReader.of(str.getBytes()).readDoubleValue();
        assertEquals(d, v);

        double v1 = JSONReader.of(str.toCharArray()).readDoubleValue();
        assertEquals(d, v1);
    }

    @Test
    public void parseDoubles() {
        String[] strings = new String[] {
                "113.92966694974881",
                "113.92966694974882",
                "113.92966694974883",
                "113.92966694974884",
                "113.92966694974885",
                "113.92966694974886",
                "113.92966694974887",
                "113.92966694974888",
                "113.92966694974889",
                "113.92966694974890",
        };

        for (String str : strings) {
            double d = Double.parseDouble(str);
            double v = JSONReader.of(str.getBytes()).readDoubleValue();
            assertEquals(d, v);

            double v1 = JSONReader.of(str.toCharArray()).readDoubleValue();
            assertEquals(d, v1);
        }
    }

    @Test
    public void parseDoubles1() {
        String[] strings = new String[] {
                "11.392966694974881",
                "11.392966694974882",
                "11.392966694974883",
                "11.392966694974884",
                "11.392966694974885",
                "11.392966694974886",
                "11.392966694974887",
                "11.392966694974888",
                "11.392966694974889",
                "11.392966694974890",
        };

        for (String str : strings) {
            double d = Double.parseDouble(str);
            double v = JSONReader.of(str.getBytes()).readDoubleValue();
            assertEquals(d, v);

            double v1 = JSONReader.of(str.toCharArray()).readDoubleValue();
            assertEquals(d, v1);
        }
    }

    @Test
    public void parseDoubles2() {
        String[] strings = new String[] {
                "1139.2966694974800",
                "1139.2966694974801",
                "1139.2966694974802",
                "1139.2966694974803",
                "1139.2966694974804",
                "1139.2966694974805",
                "1139.2966694974806",
                "1139.2966694974807",
                "1139.2966694974808",
                "1139.2966694974809",
                "1139.2966694974810",
                "1139.2966694974811",
                "1139.2966694974812",
                "1139.2966694974813",
                "1139.2966694974814",
                "1139.2966694974815",
                "1139.2966694974816",
                "1139.2966694974817",
                "1139.2966694974818",
                "1139.2966694974819",
                "1139.2966694974820",
                "1139.2966694974821",
                "1139.2966694974822",
                "1139.2966694974823",
                "1139.2966694974824",
                "1139.2966694974825",
                "1139.2966694974826",
                "1139.2966694974827",
                "1139.2966694974828",
                "1139.2966694974829",
                "1139.2966694974830",
                "1139.2966694974831",
                "1139.2966694974832",
                "1139.2966694974833",
                "1139.2966694974834",
                "1139.2966694974835",
                "1139.2966694974836",
                "1139.2966694974837",
                "1139.2966694974838",
                "1139.2966694974839",
                "1139.2966694974840",
                "1139.2966694974841",
                "1139.2966694974842",
                "1139.2966694974843",
                "1139.2966694974844",
                "1139.2966694974845",
                "1139.2966694974846",
                "1139.2966694974847",
                "1139.2966694974848",
                "1139.2966694974849",
                "1139.2966694974850",
                "1139.2966694974851",
                "1139.2966694974852",
                "1139.2966694974853",
                "1139.2966694974854",
                "1139.2966694974855",
                "1139.2966694974856",
                "1139.2966694974857",
                "1139.2966694974858",
                "1139.2966694974859",
                "1139.2966694974860",
                "1139.2966694974861",
                "1139.2966694974862",
                "1139.2966694974863",
                "1139.2966694974864",
                "1139.2966694974865",
                "1139.2966694974866",
                "1139.2966694974867",
                "1139.2966694974868",
                "1139.2966694974869",
                "1139.2966694974870",
                "1139.2966694974871",
                "1139.2966694974872",
                "1139.2966694974873",
                "1139.2966694974874",
                "1139.2966694974875",
                "1139.2966694974876",
                "1139.2966694974877",
                "1139.2966694974878",
                "1139.2966694974879",
                "1139.2966694974880",
                "1139.2966694974881",
                "1139.2966694974882",
                "1139.2966694974883",
                "1139.2966694974884",
                "1139.2966694974885",
                "1139.2966694974886",
                "1139.2966694974887",
                "1139.2966694974888",
                "1139.2966694974889",
                "1139.2966694974890",
                "1139.2966694974891",
                "1139.2966694974892",
                "1139.2966694974893",
                "1139.2966694974894",
                "1139.2966694974895",
                "1139.2966694974896",
                "1139.2966694974897",
                "1139.2966694974898",
                "1139.2966694974899"
        };

        for (String str : strings) {
            double d = Double.parseDouble(str);
            double v = JSONReader.of(str.getBytes()).readDoubleValue();
            assertEquals(d, v);

            double v1 = JSONReader.of(str.toCharArray()).readDoubleValue();
            assertEquals(d, v1);
        }
    }
}
