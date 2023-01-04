package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathTest2 {
    private String str;
    private JSONObject rootObject;

    public PathTest2() throws Exception {
        InputStream is = PathTest2.class.getClassLoader().getResourceAsStream("data/path_02.json");
        str = IOUtils.toString(is, "UTF-8");
        rootObject = JSON.parseObject(str);
    }

    @Test
    public void test_0() {
        JSONPath path = JSONPath.of("$.store.book[*].author");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"), result);

        assertEquals(Arrays.asList("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"), path.eval(rootObject));
    }

    @Test
    public void test_1() {
        JSONPath path = JSONPath.of("$..author");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals(Arrays.asList("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"), result);

        assertEquals(Arrays.asList("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"), path.eval(rootObject));
    }

    @Test
    public void test_2() {
        JSONPath path = JSONPath.of("$.store.*");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals(2, ((Collection) result).size());

        assertEquals(2, ((Collection) path.eval(rootObject)).size());
    }

    @Test
    public void test_3() {
        JSONPath path = JSONPath.of("$.store..price");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals("[8.95,12.99,8.99,22.99,19.95]", result.toString());
        assertEquals(5, ((Collection) result).size());

        assertEquals(5, ((Collection) path.eval(rootObject)).size());
    }

    @Test
    public void test_4() {
        JSONPath path = JSONPath.of("$..book[2]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99}";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_5() {
        JSONPath path = JSONPath.of("$..book[-2]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99}";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_6() {
        JSONPath path = JSONPath.of("$..book[0,1]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99}]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_7() {
        JSONPath path = JSONPath.of("$..book[:2]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99}]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_8() {
        JSONPath path = JSONPath.of("$..book[1:2]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99}]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_9() {
        JSONPath path = JSONPath.of("$..book[-2:]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_10() {
        JSONPath path = JSONPath.of("$..book[2:]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_11() {
        JSONPath path = JSONPath.of("$..book[?(@.isbn)]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_12() {
        JSONPath path = JSONPath.of("$.store.book[?(@.price < 10)]");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99}]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_21() {
        JSONPath path = JSONPath.of("$.store.book[?(@.isbn)].author");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[\"Herman Melville\",\"J. R. R. Tolkien\"]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_22() {
        JSONPath path = JSONPath.of("$.store.book[?(@.isbn)].author");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "[\"Herman Melville\",\"J. R. R. Tolkien\"]";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_23() {
        JSONPath path = JSONPath.of("$.store.book[0].author");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        String expected = "Nigel Rees";
        assertEquals(expected, result.toString());

        assertEquals(expected, path.eval(rootObject).toString());
    }

    @Test
    public void test_x1() {
        JSONPath path = JSONPath.of("$..book.length()");
        JSONReader parser = JSONReader.of(str);
        Object result = path.extract(parser);
        assertEquals(4, result);

        assertEquals(4, path.eval(rootObject));
    }
}
