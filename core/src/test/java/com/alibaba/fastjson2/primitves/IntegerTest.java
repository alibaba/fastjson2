package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Integer1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IntegerTest {
    private Integer[] values = new Integer[100];
    private int off;

    public IntegerTest() {
        values[off++] = null;
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
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Integer1> objectWriter = creator.createObjectWriter(Integer1.class);

            {
                Integer1 vo = new Integer1();
                vo.setV0000(1);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                Integer1 vo = new Integer1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                Integer1 vo = new Integer1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                Integer1 vo = new Integer1();
                vo.setV0000(1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
            {
                Integer1 vo = new Integer1();
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
        for (Integer id : values) {
            Integer1 vo = new Integer1();
            vo.setV0000(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Integer1 v1 = JSONB.parseObject(jsonbBytes, Integer1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (Integer id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            Integer id2 = JSONB.parseObject(jsonbBytes, Integer.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        Integer[] id2 = JSONB.parseObject(jsonbBytes, Integer[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_jsonb_array3() {
        int[] primitiveValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                primitiveValues[i] = 0;
                continue;
            }
            primitiveValues[i] = values[i];
        }
        byte[] jsonbBytes = JSONB.toBytes(primitiveValues);
        int[] id2 = JSONB.parseObject(jsonbBytes, int[].class);
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
            Integer id2 = JSONB.parseObject(jsonbBytes, Integer.class);
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
            Integer id2 = JSONB.parseObject(jsonbBytes, Integer.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_utf8() {
        for (Integer id : values) {
            Integer1 vo = new Integer1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Integer1 v1 = JSON.parseObject(utf8Bytes, Integer1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8_value() {
        for (Integer id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Integer id2 = JSON.parseObject(utf8Bytes, Integer.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Integer[] id2 = JSON.parseObject(utf8Bytes, Integer[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_str() {
        for (Integer id : values) {
            Integer1 vo = new Integer1();
            vo.setV0000(id);
            String str = JSON.toJSONString(vo);

            Integer1 v1 = JSON.parseObject(str, Integer1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str_value() {
        for (Integer id : values) {
            String str = JSON.toJSONString(id);
            Integer id2 = JSON.parseObject(str, Integer.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str_array3() {
        int[] primitiveValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                primitiveValues[i] = 0;
                continue;
            }
            primitiveValues[i] = values[i];
        }
        String str = JSON.toJSONString(primitiveValues);
        int[] id2 = JSON.parseObject(str, int[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_str_array() {
        String str = JSON.toJSONString(values);
        Integer[] id2 = JSON.parseObject(str, Integer[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_ascii() {
        for (Integer id : values) {
            Integer1 vo = new Integer1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Integer1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Integer1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii_value() {
        for (Integer id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Integer id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Integer.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii_array() {
        byte[] utf8Bytes = JSON.toJSONBytes(values);
        Integer[] id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Integer[].class);
        assertEquals(values.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(values[i], id2[i]);
        }
    }

    @Test
    public void test_str_decimal() {
        String str = "{\"v0000\":1001.0}";
        Integer1 v1 = JSON.parseObject(str, Integer1.class);
        assertEquals(Integer.valueOf(1001), v1.getV0000());
    }

    @Test
    public void test_str_true() {
        String str = "{\"v0000\":true}";
        Integer1 v1 = JSON.parseObject(str, Integer1.class);
        assertEquals(Integer.valueOf(1), v1.getV0000());
    }

    @Test
    public void test_str_false() {
        String str = "{\"v0000\":false}";
        Integer1 v1 = JSON.parseObject(str, Integer1.class);
        assertEquals(Integer.valueOf(0), v1.getV0000());
    }

    @Test
    public void test_str_null() {
        String str = "{\"v0000\":null}";
        Integer1 v1 = JSON.parseObject(str, Integer1.class);
        assertNull(v1.getV0000());
    }

    @Test
    public void test_str_str() {
        String str = "{\"v0000\":\"1001\"}";
        Integer1 v1 = JSON.parseObject(str, Integer1.class);
        assertEquals(Integer.valueOf(1001), v1.getV0000());
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Integer1 vo = new Integer1();

            JSONReader.Context readContext
                    = new JSONReader.Context(
                            new ObjectReaderProvider(creator));
            JSONPath jsonPath = JSONPath
                    .of("$.v0000")
                    .setReaderContext(readContext);
            jsonPath.set(vo, 101);
            assertEquals(Integer.valueOf(101), vo.getV0000());
            jsonPath.set(vo, 102L);
            assertEquals(Integer.valueOf(102), vo.getV0000());
            jsonPath.set(vo, "103");
            assertEquals(Integer.valueOf(103), vo.getV0000());

            assertEquals(Integer.valueOf(103), jsonPath.eval(vo));
        }
    }
}
