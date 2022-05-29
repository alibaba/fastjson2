package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntTest {
    private int[] values = new int[100];
    private int off;

    public IntTest() {
        values[off++] = Integer.MIN_VALUE;
        values[off++] = Integer.MAX_VALUE;
        values[off++] = (int) Short.MIN_VALUE;
        values[off++] = (int) Short.MAX_VALUE;
        values[off++] = JSONBTest.INT24_MIN;
        values[off++] = JSONBTest.INT24_MAX;
        values[off++] = -1;
        values[off++] = -10;
        values[off++] = -100;
        values[off++] = -1000;
        values[off++] = -10000;
        values[off++] = -100000;
        values[off++] = -1000000;
        values[off++] = -10000000;
        values[off++] = -100000000;
        values[off++] = -1000000000;
        values[off++] = 1;
        values[off++] = 10;
        values[off++] = 100;
        values[off++] = 1000;
        values[off++] = 10000;
        values[off++] = 100000;
        values[off++] = 1000000;
        values[off++] = 10000000;
        values[off++] = 100000000;
        values[off++] = 1000000000;
        values[off++] = -9;
        values[off++] = -99;
        values[off++] = -999;
        values[off++] = -9999;
        values[off++] = -99999;
        values[off++] = -999999;
        values[off++] = -9999999;
        values[off++] = -99999999;
        values[off++] = -999999999;
        values[off++] = 9;
        values[off++] = 99;
        values[off++] = 999;
        values[off++] = 9999;
        values[off++] = 99999;
        values[off++] = 999999;
        values[off++] = 9999999;
        values[off++] = 99999999;
        values[off++] = 999999999;
    }

    @Test
    public void test_jsonb() {
        for (int id : values) {
            Int1 vo = new Int1();
            vo.setV0000(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Int1 v1 = JSONB.parseObject(jsonbBytes, Int1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (int id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            int id2 = JSONB.parseObject(jsonbBytes, int.class);
            assertEquals(id, id2);
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
            int id2 = JSONB.parseObject(jsonbBytes, int.class);
            assertEquals(id2, id2);
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
            int id2 = JSONB.parseObject(jsonbBytes, int.class);
            assertEquals(id2, id2);
        }
    }

    @Test
    public void test_utf8() {
        for (int id : values) {
            Int1 vo = new Int1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Int1 v1 = JSON.parseObject(utf8Bytes, Int1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8_value() {
        for (int id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            int id2 = JSON.parseObject(utf8Bytes, int.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str() {
        for (int id : values) {
            Int1 vo = new Int1();
            vo.setV0000(id);
            String str = JSON.toJSONString(vo);

            Int1 v1 = JSON.parseObject(str, Int1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str_value() {
        for (int id : values) {
            String str = JSON.toJSONString(id);
            int id2 = JSON.parseObject(str, int.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii() {
        for (int id : values) {
            Int1 vo = new Int1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Int1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Int1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii_value() {
        for (int id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            int id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, int.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            JSONReader.Context readContext
                    = new JSONReader.Context(
                            new ObjectReaderProvider(creator));
            {
                Int1 vo = new Int1();

                JSONPath jsonPath = JSONPath
                        .of("$.v0000")
                        .setReaderContext(readContext);
                jsonPath.set(vo, 101);
                assertEquals(101, vo.getV0000());
                jsonPath.set(vo, 102L);
                assertEquals(102, vo.getV0000());
                jsonPath.set(vo, null);
                assertEquals(0, vo.getV0000());
                jsonPath.set(vo, "103");
                assertEquals(103, vo.getV0000());
                assertEquals(103, jsonPath.eval(vo));

                jsonPath.setInt(vo, 101);
                assertEquals(101, vo.getV0000());
                jsonPath.setLong(vo, 102L);
                assertEquals(102, vo.getV0000());
            }

            Int1 vo2 = new Int1();
            Object[] array = new Object[]{vo2};
            {
                JSONPath jsonPath = JSONPath
                        .of("$[0].v0000")
                        .setReaderContext(readContext);
                jsonPath.set(array, 101);
                assertEquals(101, vo2.getV0000());

                jsonPath.set(array, "102");
                assertEquals(102, vo2.getV0000());

                jsonPath.setInt(array, 103);
                assertEquals(103, vo2.getV0000());

                jsonPath.setLong(array, 104);
                assertEquals(104, vo2.getV0000());
            }

            {
                JSONPath jsonPath = JSONPath.of("$[0].*");
                java.util.List eval = (java.util.List) jsonPath.eval(array);
                assertEquals(1, eval.size());
            }
            {
                JSONPath jsonPath = JSONPath.of("$.*");
                java.util.List eval = (java.util.List) jsonPath.eval(vo2);
                assertEquals(1, eval.size());
            }
            {
                JSONPath jsonPath = JSONPath.of("$['v0000','v0000']");
                java.util.List eval = (java.util.List) jsonPath.eval(array);
                assertEquals(2, eval.size());
            }
        }
    }
}
