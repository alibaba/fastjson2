package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.FloatValue1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatValueTest {
    private float[] values = new float[100];
    private int off;

    public FloatValueTest() {
        values[off++] = Float.MIN_VALUE;
        values[off++] = Float.MAX_VALUE;
        values[off++] = (float) Integer.MIN_VALUE;
        values[off++] = (float) Integer.MAX_VALUE;
        values[off++] = (float) Short.MIN_VALUE;
        values[off++] = (float) Short.MAX_VALUE;
        values[off++] = (float) JSONBTest.INT24_MIN;
        values[off++] = (float) JSONBTest.INT24_MAX;
        values[off++] = -1F;
        values[off++] = -10F;
        values[off++] = -100F;
        values[off++] = -1000F;
        values[off++] = -10000F;
        values[off++] = -100000F;
        values[off++] = -1000000F;
        values[off++] = -10000000F;
        values[off++] = -100000000F;
        values[off++] = -1000000000F;
        values[off++] = 1F;
        values[off++] = 10F;
        values[off++] = 100F;
        values[off++] = 1000F;
        values[off++] = 10000F;
        values[off++] = 100000F;
        values[off++] = 1000000F;
        values[off++] = 10000000F;
        values[off++] = 100000000F;
        values[off++] = 1000000000F;
        values[off++] = -9F;
        values[off++] = -99F;
        values[off++] = -999F;
        values[off++] = -9999F;
        values[off++] = -99999F;
        values[off++] = -999999F;
        values[off++] = -9999999F;
        values[off++] = -99999999F;
        values[off++] = -999999999F;
        values[off++] = 9F;
        values[off++] = 99F;
        values[off++] = 999F;
        values[off++] = 9999F;
        values[off++] = 99999F;
        values[off++] = 999999F;
        values[off++] = 9999999F;
        values[off++] = 99999999F;
        values[off++] = 999999999F;
    }

    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<FloatValue1> objectWriter = creator.createObjectWriter(FloatValue1.class);

            {
                FloatValue1 vo = new FloatValue1();
                vo.setV0000(1);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1.0}", jsonWriter.toString());
            }
            {
                FloatValue1 vo = new FloatValue1();
                vo.setV0000(1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1.0]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_jsonb() {
        for (Float id : values) {
            FloatValue1 vo = new FloatValue1();
            vo.setV0000(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            FloatValue1 v1 = JSONB.parseObject(jsonbBytes, FloatValue1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (Float id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            Float id2 = JSONB.parseObject(jsonbBytes, Float.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        Float[] id2 = JSONB.parseObject(jsonbBytes, Float[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_jsonb_array3() {
        float[] primitiveValues = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            primitiveValues[i] = values[i];
        }
        byte[] jsonbBytes = JSONB.toBytes(primitiveValues);
        float[] id2 = JSONB.parseObject(jsonbBytes, float[].class);
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
            Float id2 = JSONB.parseObject(jsonbBytes, Float.class);
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
            Float id2 = JSONB.parseObject(jsonbBytes, Float.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_utf8() {
        for (Float id : values) {
            FloatValue1 vo = new FloatValue1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            FloatValue1 v1 = JSON.parseObject(utf8Bytes, FloatValue1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8_null() {
        byte[] utf8Bytes = "{\"v0000\":null}".getBytes(StandardCharsets.UTF_8);
        FloatValue1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.UTF_8, FloatValue1.class);
        assertEquals(0F, v1.getV0000());
    }

    @Test
    public void test_utf8_value() {
        for (Float id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Float id2 = JSON.parseObject(utf8Bytes, Float.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Float[] id2 = JSON.parseObject(utf8Bytes, Float[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_str() {
        for (Float id : values) {
            FloatValue1 vo = new FloatValue1();
            vo.setV0000(id);
            String str = JSON.toJSONString(vo);

            FloatValue1 v1 = JSON.parseObject(str, FloatValue1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str_null() {
        String str = "{\"v0000\":null}";
        FloatValue1 v1 = JSON.parseObject(str, FloatValue1.class);
        assertEquals(0F, v1.getV0000());
    }

    @Test
    public void test_str_value() {
        for (Float id : values) {
            String str = JSON.toJSONString(id);
            Float id2 = JSON.parseObject(str, Float.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str_array3() {
        float[] primitiveValues = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            primitiveValues[i] = values[i];
        }
        String str = JSON.toJSONString(primitiveValues);
        float[] id2 = JSON.parseObject(str, float[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_str_array() {
        String str = JSON.toJSONString(values);
        Float[] id2 = JSON.parseObject(str, Float[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_ascii() {
        for (Float id : values) {
            FloatValue1 vo = new FloatValue1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            FloatValue1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, FloatValue1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii_null() {
        byte[] utf8Bytes = "{\"v0000\":null}".getBytes(StandardCharsets.UTF_8);
        FloatValue1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, FloatValue1.class);
        assertEquals(0F, v1.getV0000());
    }

    @Test
    public void test_ascii_value() {
        for (Float id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Float id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Float.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Float[] id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Float[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_v3() {
        float f = values[3];
        byte[] utf8Bytes = JSON.toJSONBytes(f);
        float f1 = JSONReader.of(utf8Bytes).readFloatValue();
        assertEquals(f, f1);
    }

    @Test
    public void test0() {
        float f = 0.37172526f;
        byte[] utf8Bytes = JSON.toJSONBytes(f);
        float f1 = JSONReader.of(utf8Bytes).readFloatValue();
        assertEquals(f, f1);
    }
}
