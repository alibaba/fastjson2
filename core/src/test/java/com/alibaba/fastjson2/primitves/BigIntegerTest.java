package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.BigInteger1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigIntegerTest {
    static BigInteger[] values = new BigInteger[]{
            null,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.TEN,
            BigInteger.valueOf(Byte.MIN_VALUE),
            BigInteger.valueOf(Byte.MAX_VALUE),
            BigInteger.valueOf(Short.MIN_VALUE),
            BigInteger.valueOf(Short.MAX_VALUE),
            BigInteger.valueOf(Integer.MIN_VALUE),
            BigInteger.valueOf(Integer.MAX_VALUE),
            BigInteger.valueOf(Long.MIN_VALUE),
            BigInteger.valueOf(Long.MAX_VALUE),
            new BigInteger("12345678901234567890123456789012345678"),
            new BigInteger("-12345678901234567890123456789012345678")
    };

    @Test
    public void test_null() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter(BigInteger1.class, "id", 0, 0, null, BigInteger1.class.getMethod("getId"));
            ObjectWriter<BigInteger1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                BigInteger1 vo = new BigInteger1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                BigInteger1 vo = new BigInteger1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                BigInteger1 vo = new BigInteger1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_jsonb() {
        for (BigInteger id : values) {
            BigInteger1 vo = new BigInteger1();
            vo.setId(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            BigInteger1 v1 = JSONB.parseObject(jsonbBytes, BigInteger1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (BigInteger id : values) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            BigInteger id2 = JSONB.parseObject(jsonbBytes, BigInteger.class);
            assertEquals(id, id2);

            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            BigInteger id3 = (BigInteger) jsonReader.readAny();
            assertEquals(id, id3);
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(values);
        BigInteger[] id2 = JSONB.parseObject(jsonbBytes, BigInteger[].class);
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
            BigInteger id2 = JSONB.parseObject(jsonbBytes, BigInteger.class);
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
            BigInteger id2 = JSONB.parseObject(jsonbBytes, BigInteger.class);
            assertEquals(id2.intValue(), id2.intValue());
        }
    }

    @Test
    public void test_jsonb_value_cast_str_1() {
        byte[] jsonbBytes = {124, 2, 48, 0};
        BigInteger integer = JSONB.parseObject(jsonbBytes, BigInteger.class);
        assertEquals(BigInteger.ZERO, integer);
    }

    @Test
    public void test_jsonb_value_cast_str_2() {
        byte[] jsonbBytes = {122, 8, 45, 56, 51, 56, 56, 54, 48, 56};
        BigInteger integer = JSONB.parseObject(jsonbBytes, BigInteger.class);
        assertEquals(BigInteger.valueOf(-8388608), integer);
    }

    @Test
    public void test_utf8() {
        for (BigInteger id : values) {
            BigInteger1 vo = new BigInteger1();
            vo.setId(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            BigInteger1 v1 = JSON.parseObject(utf8Bytes, BigInteger1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_utf8_value() {
        for (BigInteger id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            BigInteger id2 = JSON.parseObject(utf8Bytes, BigInteger.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str() {
        for (BigInteger id : values) {
            BigInteger1 vo = new BigInteger1();
            vo.setId(id);
            String str = JSON.toJSONString(vo);

            BigInteger1 v1 = JSON.parseObject(str, BigInteger1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_str_value() {
        for (BigInteger id : values) {
            String str = JSON.toJSONString(id);
            BigInteger id2 = JSON.parseObject(str, BigInteger.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii() {
        for (BigInteger id : values) {
            BigInteger1 vo = new BigInteger1();
            vo.setId(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            BigInteger1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, BigInteger1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_ascii_value() {
        for (BigInteger id : values) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            BigInteger id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, BigInteger.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            BigInteger1 vo = new BigInteger1();

            JSONReader.Context readContext
                    = new JSONReader.Context(
                            new ObjectReaderProvider(creator));
            JSONPath jsonPath = JSONPath
                    .of("$.id")
                    .setReaderContext(readContext);
            jsonPath.set(vo, 101);
            assertEquals(BigInteger.valueOf(101), vo.getId());
            jsonPath.set(vo, 102L);
            assertEquals(BigInteger.valueOf(102), vo.getId());

            jsonPath.set(vo, null);
            assertEquals(null, vo.getId());

            jsonPath.set(vo, "103");
            assertEquals(BigInteger.valueOf(103), vo.getId());

            assertEquals(BigInteger.valueOf(103), jsonPath.eval(vo));

            jsonPath.setInt(vo, 101);
            assertEquals(BigInteger.valueOf(101), vo.getId());
            jsonPath.setLong(vo, 102L);
            assertEquals(BigInteger.valueOf(102), vo.getId());
        }
    }

    @Test
    public void test_direct() {
        assertEquals("0", JSON.toJSONString(BigInteger.ZERO));
    }

    @Test
    public void test_direct_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(BigInteger.ZERO);
        assertEquals(BigInteger.ZERO, JSONB.parse(jsonbBytes));
    }

    @Test
    public void test_bool_true() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", true));
        BigInteger1 vo = JSONB.parseObject(jsonbBytes, BigInteger1.class);
        assertEquals(BigInteger.ONE, vo.getId());
    }

    @Test
    public void test_bool_false() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", false));
        BigInteger1 vo = JSONB.parseObject(jsonbBytes, BigInteger1.class);
        assertEquals(BigInteger.ZERO, vo.getId());
    }
}
