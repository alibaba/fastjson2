package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Byte1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteTest {
    private Byte[] values = new Byte[257];
    private int off;

    public ByteTest() {
        values[off++] = null;
        for (int i = 0; i < 256; ++i) {
            values[off++] = (byte) (Byte.MIN_VALUE + i);
        }
    }

    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Byte1> objectWriter = creator.createObjectWriter(Byte1.class);

            {
                Byte1 vo = new Byte1();
                vo.setV0000((byte) 1);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                Byte1 vo = new Byte1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                Byte1 vo = new Byte1();
                vo.setV0000((byte) 1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteClassName);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"@type\":\"com.alibaba.fastjson2_vo.Byte1\",\"v0000\":1}", jsonWriter.toString());
            }
            {
                Byte1 vo = new Byte1();
                vo.setV0000((byte) 1);
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.WriteClassName);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"@type\":\"com.alibaba.fastjson2_vo.Byte1\",\"v0000\":1}", jsonWriter.toString());
            }
            {
                Byte1 vo = new Byte1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                Byte1 vo = new Byte1();
                vo.setV0000((byte) 1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
            {
                Byte1 vo = new Byte1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }

            {
                Byte1 vo = new Byte1();
                vo.setV0000((byte) 1);
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("{\n" +
                        "\t\"v0000\":1\n" +
                        "}", JSONB.toJSONString(jsonbBytes));
            }
            {
                Byte1 vo = new Byte1();
                vo.setV0000((byte) 1);
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[1]", JSONB.toJSONString(jsonbBytes));
            }
            {
                Byte1 vo = new Byte1();
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[null]", JSONB.toJSONString(jsonbBytes));
            }
        }
    }

    @Test
    public void test_filter() {
        PropertyFilter propertyFilter = new PropertyFilter() {
            @Override
            public boolean apply(Object object, String name, Object value) {
                return false;
            }
        };

        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Byte1> objectWriter = creator.createObjectWriter(Byte1.class);

            Byte1 vo = new Byte1();
            vo.setV0000((byte) 1);
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.getContext().setPropertyFilter(propertyFilter);
            objectWriter.write(jsonWriter, vo);
            assertEquals("{}", jsonWriter.toString());
        }
    }

    @Test
    public void test_jsonb() {
        for (Byte id : values) {
            Byte1 vo = new Byte1();
            vo.setV0000(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Byte1 v1 = JSONB.parseObject(jsonbBytes, Byte1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (Byte id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            Byte id2 = JSONB.parseObject(jsonbBytes, Byte.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        Byte[] id2 = JSONB.parseObject(jsonbBytes, Byte[].class);
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
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                JSONBTest.INT24_MIN, JSONBTest.INT24_MAX,
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE,
                new BigDecimal(".123456789012345678901234567890123456789001234567890"),
                new BigDecimal("-.123456789012345678901234567890123456789001234567890"),
                new BigDecimal("123456789012345678901234567890123456789001234567890."),
                new BigDecimal("-123456789012345678901234567890123456789001234567890."),
        };

        for (Number value : values) {
            byte[] jsonbBytes = JSONB.toBytes(value);
            Byte id2 = JSONB.parseObject(jsonbBytes, Byte.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }


    @Test
    public void test_jsonb_value_cast_str() {
        Number[] values = new Number[]{
                0,
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                JSONBTest.INT24_MIN, JSONBTest.INT24_MAX,
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE,
                new BigDecimal(".123456789012345678901234567890123456789001234567890"),
                new BigDecimal("-.123456789012345678901234567890123456789001234567890"),
                new BigDecimal("123456789012345678901234567890123456789001234567890."),
                new BigDecimal("-123456789012345678901234567890123456789001234567890."),
        };

        for (Number value : values) {
            byte[] jsonbBytes = JSONB.toBytes(value.toString());
            Byte id2 = JSONB.parseObject(jsonbBytes, Byte.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_utf8() {
        for (Byte id : values) {
            Byte1 vo = new Byte1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Byte1 v1 = JSON.parseObject(utf8Bytes, Byte1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8_value() {
        for (Byte id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Byte id2 = JSON.parseObject(utf8Bytes, Byte.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Byte[] id2 = JSON.parseObject(utf8Bytes, Byte[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_str() {
        for (Byte id : values) {
            Byte1 vo = new Byte1();
            vo.setV0000(id);
            String str = JSON.toJSONString(vo);

            Byte1 v1 = JSON.parseObject(str, Byte1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str_value() {
        for (Byte id : values) {
            String str = JSON.toJSONString(id);
            Byte id2 = JSON.parseObject(str, Byte.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str_array() {
        String str = JSON.toJSONString(values);
        Byte[] id2 = JSON.parseObject(str, Byte[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_ascii() {
        for (Byte id : values) {
            Byte1 vo = new Byte1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Byte1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Byte1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii_value() {
        for (Byte id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Byte id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Byte.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Byte[] id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Byte[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }
}
