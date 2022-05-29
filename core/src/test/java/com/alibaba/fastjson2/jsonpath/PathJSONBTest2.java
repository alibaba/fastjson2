package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathJSONBTest2 {
    private String str;
    private byte[] utf8Bytes;
    private byte[] jsonbBytes;
    private JSONObject rootObject;

    public PathJSONBTest2() throws Exception {
        InputStream is = PathJSONBTest2.class.getClassLoader().getResourceAsStream("data/path_02.json");
        str = IOUtils.toString(is, "UTF-8");
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
        rootObject = JSON.parseObject(str);
        jsonbBytes = JSONB.toBytes(rootObject);
    }

    @Test
    public void test_0() throws Exception {
        JSONPath path = JSONPath.of("$.store.book[*].author");
        JSONReader parser = JSONReader.ofJSONB(jsonbBytes);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"), result);
    }

    @Test
    public void test_1() throws Exception {
        JSONPath path = JSONPath.of("$..author");
        JSONReader parser = JSONReader.ofJSONB(jsonbBytes);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"), result);
    }
}
