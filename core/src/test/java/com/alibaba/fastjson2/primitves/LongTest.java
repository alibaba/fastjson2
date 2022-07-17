package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Long1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LongTest {
    private Long[] values = new Long[100];
    private int off;

    public LongTest() {
        values[off++] = null;
        values[off++] = (long) Integer.MIN_VALUE;
        values[off++] = (long) Integer.MAX_VALUE;
        values[off++] = (long) Short.MIN_VALUE;
        values[off++] = (long) Short.MAX_VALUE;
        values[off++] = (long) JSONBTest.INT24_MIN;
        values[off++] = (long) JSONBTest.INT24_MAX;
        values[off++] = -1L;
        values[off++] = -10L;
        values[off++] = -100L;
        values[off++] = -1000L;
        values[off++] = -10000L;
        values[off++] = -100000L;
        values[off++] = -1000000L;
        values[off++] = -10000000L;
        values[off++] = -100000000L;
        values[off++] = -1000000000L;
        values[off++] = -10000000000L;
        values[off++] = -100000000000L;
        values[off++] = -1000000000000L;
        values[off++] = -10000000000000L;
        values[off++] = -100000000000000L;
        values[off++] = -1000000000000000L;
        values[off++] = -10000000000000000L;
        values[off++] = -100000000000000000L;
        values[off++] = -1000000000000000000L;
        values[off++] = -1000000000000000000L;
        values[off++] = 1L;
        values[off++] = 10L;
        values[off++] = 100L;
        values[off++] = 1000L;
        values[off++] = 10000L;
        values[off++] = 100000L;
        values[off++] = 1000000L;
        values[off++] = 10000000L;
        values[off++] = 100000000L;
        values[off++] = 1000000000L;
        values[off++] = -9L;
        values[off++] = -99L;
        values[off++] = -999L;
        values[off++] = -9999L;
        values[off++] = -99999L;
        values[off++] = -999999L;
        values[off++] = -9999999L;
        values[off++] = -99999999L;
        values[off++] = -999999999L;
        values[off++] = 9L;
        values[off++] = 99L;
        values[off++] = 999L;
        values[off++] = 9999L;
        values[off++] = 99999L;
        values[off++] = 999999L;
        values[off++] = 9999999L;
        values[off++] = 99999999L;
        values[off++] = 999999999L;
    }

    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Long1> objectWriter = creator.createObjectWriter(Long1.class);

            {
                Long1 vo = new Long1();
                vo.setV0000(1L);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                Long1 vo = new Long1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                Long1 vo = new Long1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                Long1 vo = new Long1();
                vo.setV0000(1L);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
            {
                Long1 vo = new Long1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_jsonb() {
        for (Long id : values) {
            Long1 vo = new Long1();
            vo.setV0000(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Long1 v1 = JSONB.parseObject(jsonbBytes, Long1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (Long id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            Long id2 = JSONB.parseObject(jsonbBytes, Long.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        Long[] id2 = JSONB.parseObject(jsonbBytes, Long[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_jsonb_array2() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        long[] id2 = JSONB.parseObject(jsonbBytes, long[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            if (values[i] == null) {
                assertEquals(0, id2[i]);
                continue;
            }
            assertEquals(values[i].longValue(), id2[i]);
        }
    }

    @Test
    public void test_jsonb_array3() {
        long[] primitiveValues = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                primitiveValues[i] = 0;
                continue;
            }
            primitiveValues[i] = values[i];
        }
        byte[] jsonbBytes = JSONB.toBytes(primitiveValues);
        long[] id2 = JSONB.parseObject(jsonbBytes, long[].class);
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
            Long id2 = JSONB.parseObject(jsonbBytes, Long.class);
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
            Long id2 = JSONB.parseObject(jsonbBytes, Long.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_jsonb_value_cast_str_1() {
        byte[] jsonbBytes = {124, 2, 48, 0};
        assertEquals(0, JSONB.parseObject(jsonbBytes, Long.class).longValue());
    }

    @Test
    public void test_jsonb_value_cast_str_2() {
        byte[] jsonbBytes = {122, 8, 45, 56, 51, 56, 56, 54, 48, 56};
        assertEquals(-8388608L, JSONB.parseObject(jsonbBytes, Long.class).longValue());
    }

    @Test
    public void test_utf8() {
        for (Long id : values) {
            Long1 vo = new Long1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Long1 v1 = JSON.parseObject(utf8Bytes, Long1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8_value() {
        for (Long id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Long id2 = JSON.parseObject(utf8Bytes, Long.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Long[] id2 = JSON.parseObject(utf8Bytes, Long[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_str() {
        for (Long id : values) {
            Long1 vo = new Long1();
            vo.setV0000(id);
            String str = JSON.toJSONString(vo);

            Long1 v1 = JSON.parseObject(str, Long1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str_decimal() {
        String str = "{\"v0000\":1001.0}";
        Long1 v1 = JSON.parseObject(str, Long1.class);
        assertEquals(Long.valueOf(1001), v1.getV0000());
    }

    @Test
    public void test_str_true() {
        String str = "{\"v0000\":true}";
        Long1 v1 = JSON.parseObject(str, Long1.class);
        assertEquals(Long.valueOf(1), v1.getV0000());
    }

    @Test
    public void test_str_false() {
        String str = "{\"v0000\":false}";
        Long1 v1 = JSON.parseObject(str, Long1.class);
        assertEquals(Long.valueOf(0), v1.getV0000());
    }

    @Test
    public void test_str_null() {
        String str = "{\"v0000\":null}";
        Long1 v1 = JSON.parseObject(str, Long1.class);
        assertNull(v1.getV0000());
    }

    @Test
    public void test_str_str() {
        String str = "{\"v0000\":\"1001\"}";
        Long1 v1 = JSON.parseObject(str, Long1.class);
        assertEquals(Long.valueOf(1001), v1.getV0000());
    }

    @Test
    public void test_str_value() {
        for (Long id : values) {
            String str = JSON.toJSONString(id);
            Long id2 = JSON.parseObject(str, Long.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str_array3() {
        long[] primitiveValues = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                primitiveValues[i] = 0;
                continue;
            }
            primitiveValues[i] = values[i];
        }
        String str = JSON.toJSONString(primitiveValues);
        long[] id2 = JSON.parseObject(str, long[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_str_array() {
        String str = JSON.toJSONString(values);
        Long[] id2 = JSON.parseObject(str, Long[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_ascii() {
        for (Long id : values) {
            Long1 vo = new Long1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Long1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Long1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii_value() {
        for (Long id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Long id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Long.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Long[] id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Long[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }
}
