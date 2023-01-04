package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Short1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShortTest {
    private Short[] values = new Short[100];
    private int off;

    public ShortTest() {
        values[off++] = null;
        values[off++] = Short.MIN_VALUE;
        values[off++] = Short.MAX_VALUE;
        values[off++] = (int) Short.MIN_VALUE;
        values[off++] = (int) Short.MAX_VALUE;
        values[off++] = -1;
        values[off++] = -10;
        values[off++] = -100;
        values[off++] = -1000;
        values[off++] = -10000;
        values[off++] = 1;
        values[off++] = 10;
        values[off++] = 100;
        values[off++] = 1000;
        values[off++] = 10000;
        values[off++] = -9;
        values[off++] = -99;
        values[off++] = -999;
        values[off++] = -9999;
        values[off++] = 9;
        values[off++] = 99;
        values[off++] = 999;
        values[off++] = 9999;
    }

    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Short1> objectWriter = creator.createObjectWriter(Short1.class);

            {
                Short1 vo = new Short1();
                vo.setV0000((short) 1);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
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
        for (Short id : values) {
            Short1 vo = new Short1();
            vo.setV0000(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Short1 v1 = JSONB.parseObject(jsonbBytes, Short1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (Short id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            Short id2 = JSONB.parseObject(jsonbBytes, Short.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        Short[] id2 = JSONB.parseObject(jsonbBytes, Short[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_jsonb_value_cast() {
        Number[] values = new Number[]{
                0,
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                Short.MIN_VALUE, Short.MAX_VALUE,
                JSONBTest.INT24_MIN, JSONBTest.INT24_MAX,
                Short.MIN_VALUE, Short.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE,
                new BigDecimal(".123456789012345678901234567890123456789001234567890"),
                new BigDecimal("-.123456789012345678901234567890123456789001234567890"),
                new BigDecimal("123456789012345678901234567890123456789001234567890."),
                new BigDecimal("-123456789012345678901234567890123456789001234567890."),
        };

        for (Number value : values) {
            byte[] jsonbBytes = JSONB.toBytes(value);
            Short id2 = JSONB.parseObject(jsonbBytes, Short.class);
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
                Short.MIN_VALUE, Short.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE,
                new BigDecimal(".123456789012345678901234567890123456789001234567890"),
                new BigDecimal("-.123456789012345678901234567890123456789001234567890"),
                new BigDecimal("123456789012345678901234567890123456789001234567890."),
                new BigDecimal("-123456789012345678901234567890123456789001234567890."),
        };

        for (Number value : values) {
            byte[] jsonbBytes = JSONB.toBytes(value.toString());
            Short id2 = JSONB.parseObject(jsonbBytes, Short.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_jsonb_value_cast_str_short_1() {
        byte[] jsonbBytes = {124, 2, 48, 0};
        assertEquals(0, JSONB.parseObject(jsonbBytes, Short.class).shortValue());
    }

    @Test
    public void test_jsonb_value_cast_str_short_2() {
        byte[] jsonbBytes = {122, 8, 45, 56, 51, 56, 56, 54, 48, 56};
        assertEquals((short) -8388608, JSONB.parseObject(jsonbBytes, Short.class).shortValue());
    }

    @Test
    public void test_utf8() {
        for (Short id : values) {
            Short1 vo = new Short1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Short1 v1 = JSON.parseObject(utf8Bytes, Short1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8_value() {
        for (Short id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Short id2 = JSON.parseObject(utf8Bytes, Short.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str() {
        for (Short id : values) {
            Short1 vo = new Short1();
            vo.setV0000(id);
            String str = JSON.toJSONString(vo);

            Short1 v1 = JSON.parseObject(str, Short1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str_value() {
        for (Short id : values) {
            String str = JSON.toJSONString(id);
            Short id2 = JSON.parseObject(str, Short.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii() {
        for (Short id : values) {
            Short1 vo = new Short1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Short1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Short1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii_value() {
        for (Short id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Short id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Short.class);
            assertEquals(id, id2);
        }
    }
}
