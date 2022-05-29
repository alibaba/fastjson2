package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathTest {
    private String str;
    private byte[] utf8Bytes;
    private JSONObject rootObject;

    public PathTest() throws Exception {
        InputStream is = PathTest.class.getClassLoader().getResourceAsStream("data/path_01.json");
        str = IOUtils.toString(is, "UTF-8");
        utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
        rootObject = JSON.parseObject(str);
    }

    @Test
    public void test_0() throws Exception {
        JSONPath path = JSONPath.of("$.id");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals(1, result);

        assertEquals(1, path.eval(rootObject));
    }

    @Test
    public void test_0_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.id");
        JSONReader parser = JSONReader.of(utf8Bytes, 0, utf8Bytes.length);
        Object result = path.extract(parser);
        assertEquals(1, result);
    }

    @Test
    public void test_1() throws Exception {
        JSONPath path = JSONPath.of("$.user_agent.platform");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals("Windows NT 6.1", result);

        assertEquals("Windows NT 6.1", path.eval(rootObject));
    }

    @Test
    public void test_1_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.user_agent.platform");
        JSONReader parser = JSONReader.of(utf8Bytes, 0, utf8Bytes.length);
        Object result = path.extract(parser);
        assertEquals("Windows NT 6.1", result);
    }

    @Test
    public void test_2() throws Exception {
        JSONPath path = JSONPath.of("$.tags[0]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals("summer-sale", result);

        assertEquals("summer-sale", path.eval(rootObject));
    }

    @Test
    public void test_2_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.tags[0]");
        JSONReader parser = JSONReader.of(utf8Bytes, 0, utf8Bytes.length);
        Object result = path.extract(parser);
        assertEquals("summer-sale", result);
    }

    @Test
    public void test_3() throws Exception {
        JSONPath path = JSONPath.of("$.tags[1]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals("sports", result);

        assertEquals("sports", path.eval(rootObject));
    }

    @Test
    public void test_3_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.tags[1]");
        JSONReader parser = JSONReader.of(utf8Bytes, 0, utf8Bytes.length);
        Object result = path.extract(parser);
        assertEquals("sports", result);
    }

    @Test
    public void test_4() throws Exception {
        JSONPath path = JSONPath.of("$.user_agent.*");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Mozilla/5.0", "Windows NT 6.1", "1024x4069"), result);

        assertEquals(new JSONArray("Mozilla/5.0", "Windows NT 6.1", "1024x4069"),
                path.eval(rootObject));
    }

    @Test
    public void test_4_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.user_agent.*");
        JSONReader parser = JSONReader.of(utf8Bytes, 0, utf8Bytes.length);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Mozilla/5.0", "Windows NT 6.1", "1024x4069"), result);
    }

    @Test
    public void test_5() throws Exception {
        JSONPath path = JSONPath.of("$['user_agent'][*]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Mozilla/5.0", "Windows NT 6.1", "1024x4069"), result);

        assertEquals(new JSONArray("Mozilla/5.0", "Windows NT 6.1", "1024x4069"),
                path.eval(rootObject));
    }

    @Test
    public void test_5_utf8() throws Exception {
        JSONPath path = JSONPath.of("$['user_agent'][*]");
        JSONReader parser = JSONReader.of(utf8Bytes, 0, utf8Bytes.length);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Mozilla/5.0", "Windows NT 6.1", "1024x4069"), result);
    }

//
//    @Test
//    public void test_6() throws Exception {
//        Path path = Path.of("$.events[1].EventProperties.ProductID");
//        Parser parser = Parser.of(str);
//        Object result = path.extract(parser);
//        assertEquals("xy123", result);
//    }
}
