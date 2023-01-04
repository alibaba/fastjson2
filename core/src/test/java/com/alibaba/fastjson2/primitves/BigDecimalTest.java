package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.BigDecimal1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimalTest {
    static BigDecimal[] values = new BigDecimal[]{
            BigDecimal.ZERO,
            BigDecimal.ONE,
            BigDecimal.TEN,
            BigDecimal.valueOf(Byte.MIN_VALUE),
            BigDecimal.valueOf(Byte.MAX_VALUE),
            BigDecimal.valueOf(Short.MIN_VALUE),
            BigDecimal.valueOf(Short.MAX_VALUE),
            BigDecimal.valueOf(JSONBTest.INT24_MIN),
            BigDecimal.valueOf(JSONBTest.INT24_MAX),
            BigDecimal.valueOf(Integer.MIN_VALUE),
            BigDecimal.valueOf(Integer.MAX_VALUE),
            BigDecimal.valueOf(Long.MIN_VALUE),
            BigDecimal.valueOf(Long.MAX_VALUE),
            new BigDecimal("12345678901234567890123456789012345678"),
            new BigDecimal("-12345678901234567890123456789012345678"),

            BigDecimal.valueOf(Byte.MIN_VALUE, 1),
            BigDecimal.valueOf(Byte.MAX_VALUE, 2),
            BigDecimal.valueOf(Short.MIN_VALUE, 1),
            BigDecimal.valueOf(Short.MAX_VALUE, 2),
            BigDecimal.valueOf(Integer.MIN_VALUE, 1),
            BigDecimal.valueOf(Integer.MAX_VALUE, 2),
            BigDecimal.valueOf(Integer.MIN_VALUE, 1),
            BigDecimal.valueOf(Integer.MAX_VALUE, 2),
            BigDecimal.valueOf(Long.MIN_VALUE, 1),
            BigDecimal.valueOf(Long.MAX_VALUE, 2),

            new BigDecimal(".1234567890123456789012345678901234567"),
            new BigDecimal("-.1234567890123456789012345678901234567"),
            new BigDecimal("12345678901234567890123456789012345678."),
            new BigDecimal("-12345678901234567890123456789012345678."),
            null
    };

    @Test
    public void test_null() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter(BigDecimal1.class, "id", 0, 0, null, BigDecimal1.class.getMethod("getId"));
            ObjectWriter<BigDecimal1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                BigDecimal1 vo = new BigDecimal1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                BigDecimal1 vo = new BigDecimal1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                BigDecimal1 vo = new BigDecimal1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_jsonb() {
        for (BigDecimal id : values) {
            BigDecimal1 vo = new BigDecimal1();
            vo.setId(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            BigDecimal1 v1 = JSONB.parseObject(jsonbBytes, BigDecimal1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (BigDecimal id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            BigDecimal id2 = JSONB.parseObject(jsonbBytes, BigDecimal.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        BigDecimal[] id2 = JSONB.parseObject(jsonbBytes, BigDecimal[].class);
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
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE,
                new BigDecimal(".123456789012345678901234567890123456789001234567890"),
                new BigDecimal("-.123456789012345678901234567890123456789001234567890"),
                new BigDecimal("123456789012345678901234567890123456789001234567890."),
                new BigDecimal("-123456789012345678901234567890123456789001234567890."),
        };

        for (Number value : values) {
            byte[] jsonbBytes = JSONB.toBytes(value);
            BigDecimal id2 = JSONB.parseObject(jsonbBytes, BigDecimal.class);
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
            BigDecimal id2 = JSONB.parseObject(jsonbBytes, BigDecimal.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_jsonb_value_cast_str_1() {
        byte[] jsonbBytes = {124, 2, 48, 0};
        BigDecimal decimal = JSONB.parseObject(jsonbBytes, BigDecimal.class);
        assertEquals(BigDecimal.ZERO, decimal);
    }

    @Test
    public void test_jsonb_value_cast_str_2() {
        byte[] jsonbBytes = {122, 8, 45, 56, 51, 56, 56, 54, 48, 56};
        BigDecimal decimal = JSONB.parseObject(jsonbBytes, BigDecimal.class);
        assertEquals(BigDecimal.valueOf(-8388608), decimal);
    }

    @Test
    public void test_utf8() {
        for (BigDecimal id : values) {
            BigDecimal1 vo = new BigDecimal1();
            vo.setId(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            BigDecimal1 v1 = JSON.parseObject(utf8Bytes, BigDecimal1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_utf8_value() {
        for (BigDecimal id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            BigDecimal id2 = JSON.parseObject(utf8Bytes, BigDecimal.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str() {
        for (BigDecimal id : values) {
            BigDecimal1 vo = new BigDecimal1();
            vo.setId(id);
            String str = JSON.toJSONString(vo);

            BigDecimal1 v1 = JSON.parseObject(str, BigDecimal1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_str_value() {
        for (BigDecimal id : values) {
            String str = JSON.toJSONString(id);
            BigDecimal id2 = JSON.parseObject(str, BigDecimal.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii() {
        for (BigDecimal id : values) {
            BigDecimal1 vo = new BigDecimal1();
            vo.setId(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            BigDecimal1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, BigDecimal1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_ascii_value() {
        for (BigDecimal id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            BigDecimal id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, BigDecimal.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            BigDecimal1 vo = new BigDecimal1();

            JSONReader.Context readContext
                    = new JSONReader.Context(
                            new ObjectReaderProvider(creator));
            JSONPath jsonPath = JSONPath
                    .of("$.id")
                    .setReaderContext(readContext);
            jsonPath.set(vo, 101);
            assertEquals(BigDecimal.valueOf(101), vo.getId());
            jsonPath.set(vo, 102L);
            assertEquals(BigDecimal.valueOf(102), vo.getId());

            jsonPath.set(vo, null);
            assertEquals(null, vo.getId());

            jsonPath.set(vo, "103");
            assertEquals(BigDecimal.valueOf(103), vo.getId());

            assertEquals(BigDecimal.valueOf(103), jsonPath.eval(vo));

            jsonPath.setInt(vo, 101);
            assertEquals(BigDecimal.valueOf(101), vo.getId());
            jsonPath.setLong(vo, 102L);
            assertEquals(BigDecimal.valueOf(102), vo.getId());
        }
    }

    @Test
    public void test_bool_true() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", true));
        BigDecimal1 vo = JSONB.parseObject(jsonbBytes, BigDecimal1.class);
        assertEquals(BigDecimal.ONE, vo.getId());
    }

    @Test
    public void test_bool_false() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", false));
        BigDecimal1 vo = JSONB.parseObject(jsonbBytes, BigDecimal1.class);
        assertEquals(BigDecimal.ZERO, vo.getId());
    }

    @Test
    public void test_decimal_4() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", new BigDecimal("638860.1")));
        BigDecimal1 vo = JSONB.parseObject(jsonbBytes, BigDecimal1.class);
        assertEquals(new BigDecimal("638860.1"), vo.getId());
    }

    @Test
    public void test_decimal_5() {
        JSON.parse("{\"doubleval\": 12345.123E256}");
        JSON.parse("{\"doubleval\": 1.123E300}");
        JSON.parse("{\"doubleval\": 123.123E256}");
    }
}
