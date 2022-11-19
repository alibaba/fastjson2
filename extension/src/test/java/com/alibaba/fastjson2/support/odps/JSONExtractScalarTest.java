package com.alibaba.fastjson2.support.odps;

import com.aliyun.odps.io.Text;
import com.aliyun.odps.io.Writable;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONExtractScalarTest {
    private String str;
    private byte[] utf8Bytes;

    public JSONExtractScalarTest() throws Exception {
        InputStream is = JSONExtractScalarTest.class.getClassLoader().getResourceAsStream("data/path_01.json");
        str = IOUtils.toString(is, "UTF-8");
        utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void test_extract_id() throws Exception {
        JSONExtractScalar udf = new JSONExtractScalar("$.id");

        Text intput = new Text(utf8Bytes);
        Writable result = udf.eval(intput);
        assertEquals("1",
                result.toString());
    }

    @Test
    public void test_null() throws Exception {
        JSONExtractScalar udf = new JSONExtractScalar("$.id");

        assertEquals(
                "null",
                udf.eval(
                        new Text("{\"id\":null}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "true",
                udf.eval(
                        new Text("{\"id\":true}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "false",
                udf.eval(
                        new Text("{\"id\":false}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "123",
                udf.eval(
                        new Text("{\"id\":123}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "1234567890",
                udf.eval(
                        new Text("{\"id\":1234567890}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "12.34",
                udf.eval(
                        new Text("{\"id\":12.34}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "12",
                udf.eval(
                        new Text("{\"id\": 12D}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "abc",
                udf.eval(
                        new Text("{\"id\":\"abc\"}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "ab\\c",
                udf.eval(
                        new Text("{\"id\":\"ab\\\\c\"}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "{}",
                udf.eval(
                        new Text("{\"id\":{}}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
        assertEquals(
                "[]",
                udf.eval(
                        new Text("{\"id\":[]}".getBytes(StandardCharsets.UTF_8))
                ).toString()
        );
    }

    @Test
    public void test_extract_StartTime() throws Exception {
        JSONExtractScalar udf = new JSONExtractScalar("$.StartTime");

        Text intput = new Text(utf8Bytes);
        Writable result = udf.eval(intput);
        assertEquals("2020-01-06 17:00:00",
                result.toString());
    }

    @Test
    public void test_extract_Duration() throws Exception {
        JSONExtractScalar udf = new JSONExtractScalar("$.Duration");

        Text intput = new Text(utf8Bytes);
        Writable result = udf.eval(intput);
        assertEquals("450",
                result.toString());
    }

    @Test
    public void testExtractValueConsumer() {
        JSONExtractScalar udf = new JSONExtractScalar("$.id");
        JSONExtractScalar.ExtractValueConsumer consumer = udf.new ExtractValueConsumer();

        consumer.accept(101);
        assertEquals(
                "101",
                udf.text.toString()
        );

        consumer.accept(123L);
        assertEquals(
                "123",
                udf.text.toString()
        );

        consumer.accept(Byte.MIN_VALUE);
        assertEquals(
                Byte.toString(Byte.MIN_VALUE),
                udf.text.toString()
        );

        consumer.accept(Integer.valueOf(123));
        assertEquals(
                "123",
                udf.text.toString()
        );
        consumer.accept(Long.valueOf(123));
        assertEquals(
                "123",
                udf.text.toString()
        );
        consumer.accept(BigInteger.valueOf(123));
        assertEquals(
                "123",
                udf.text.toString()
        );

        consumer.accept("abc");
        assertEquals(
                "abc",
                udf.text.toString()
        );
    }
}
