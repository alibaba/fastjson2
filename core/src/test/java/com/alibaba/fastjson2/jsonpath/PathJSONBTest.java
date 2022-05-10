package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathJSONBTest {
    private String str;
    private byte[] utf8Bytes;
    private byte[] jsonbBytes;
    private JSONObject rootObject;

    public PathJSONBTest() throws Exception {
        InputStream is = PathJSONBTest.class.getClassLoader().getResourceAsStream("data/path_01.json");
        str = IOUtils.toString(is, "UTF-8");
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
        rootObject = JSON.parseObject(str);
        jsonbBytes = JSONB.toBytes(rootObject);
    }

    @Test
    public void test_0() throws Exception {
        JSONPath path = JSONPath.of("$.id");
        JSONReader parser = JSONReader.ofJSONB(jsonbBytes);

        Object result = path.extract(parser);
        assertEquals(1, result);
    }

    @Test
    public void test_1() throws Exception {
        JSONPath path = JSONPath.of("$.user_agent.platform");
        JSONReader parser = JSONReader.ofJSONB(jsonbBytes);
        Object result = path.extract(parser);
        assertEquals("Windows NT 6.1", result);
    }

    @Test
    public void test_2() throws Exception {
        JSONPath path = JSONPath.of("$.tags[0]");
        JSONReader parser = JSONReader.ofJSONB(jsonbBytes);
        Object result = path.extract(parser);
        assertEquals("summer-sale", result);
    }

    @Test
    public void test_3() throws Exception {
        JSONPath path = JSONPath.of("$.tags[1]");
        JSONReader parser = JSONReader.ofJSONB(jsonbBytes);
        Object result = path.extract(parser);
        assertEquals("sports", result);
    }

    @Test
    public void test_4() throws Exception {
        JSONPath path = JSONPath.of("$.user_agent.*");
        JSONReader parser = JSONReader.ofJSONB(jsonbBytes);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Mozilla/5.0", "Windows NT 6.1", "1024x4069"), result);
    }

    @Test
    public void test_5() throws Exception {
        JSONPath path = JSONPath.of("$['user_agent'][*]");
        JSONReader parser = JSONReader.ofJSONB(jsonbBytes);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Mozilla/5.0", "Windows NT 6.1", "1024x4069"), result);
    }
}
