package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.support.airlift.JSONFunctions;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFunctionsTest {
    private String str;
    private byte[] utf8Bytes;

    public JSONFunctionsTest() throws Exception {
        InputStream is = JSONFunctionsTest.class.getClassLoader().getResourceAsStream("data/path_01.json");
        str = IOUtils.toString(is, "UTF-8");
        utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void testExtract() throws Exception {
        JSONPath path = JSONPath.of("$.id");
        Slice json = Slices.wrappedBuffer(utf8Bytes);
        Slice result = JSONFunctions.jsonExtract(json, path);
        assertEquals("1", result.toStringUtf8());
    }
}
