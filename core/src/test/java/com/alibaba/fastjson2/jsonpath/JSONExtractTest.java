package com.alibaba.fastjson2.jsonpath;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONExtractTest {
    private String str;
    private byte[] utf8Bytes;

    public JSONExtractTest() throws Exception {
        InputStream is = JSONExtractTest.class.getClassLoader().getResourceAsStream("data/path_01.json");
        str = IOUtils.toString(is, "UTF-8");
        utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void test_extract_id() throws Exception {
        JSONExtract udf = new JSONExtract("$.id");

        JSONWritable result = udf.eval(utf8Bytes);
        assertEquals("\"1\"",
                result.toString());
    }

    @Test
    public void test_extract_StartTime() throws Exception {
        JSONExtract udf = new JSONExtract("$.StartTime");

        JSONWritable result = udf.eval(utf8Bytes);
        assertEquals("\"2020-01-06 17:00:00\"",
                result.toString());
    }
}
