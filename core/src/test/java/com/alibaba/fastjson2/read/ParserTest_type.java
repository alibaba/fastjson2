package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest_type {
    String str = "{\n" +
            "\t\"v0\" : 1,\n" +
            "\t\"v1\" : 12.34,\n" +
            "\t\"v2\" : true,\n" +
            "\t\"v3\" : false,\n" +
            "\t\"v4\" : null,\n" +
            "\t\"v5\" : [],\n" +
            "\t\"v6\" : [1, 12.34, true, false, null, {}, []],\n" +
            "\t\"v7\" : {}\n" +
            "}";

    @Test
    public void test_0_ascii_bytes() {
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        JSONReader lexer = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf8() {
        JSONReader lexer = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16() {
        JSONReader lexer = JSONReader.of(str.toCharArray());
        lexerTest(lexer);
    }

    @Test
    public void test_0_string() {
        JSONReader lexer = JSONReader.of(str);
        lexerTest(lexer);
    }

    @Test
    public void test_0_stringExtract() throws Exception {
        JSONReader.Context ctx = JSONFactory.createReadContext();
        Class<?> clazz = Class.forName("com.alibaba.fastjson2.JSONReaderStr");
        Constructor<?> constructor = clazz.getDeclaredConstructor(JSONReader.Context.class, String.class, int.class, int.class);
        constructor.setAccessible(true);
        JSONReader lexer = (JSONReader) constructor.newInstance(ctx, str, 0, str.length());
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16_bytes() {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_16);
        JSONReader lexer = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_16);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf8_is() {
        JSONReader lexer = JSONReader.of(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16_is() {
        JSONReader lexer = JSONReader.of(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_16)), StandardCharsets.UTF_16);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16_reader() {
        JSONReader lexer = JSONReader.of(new StringReader(str));
        lexerTest(lexer);
    }

    void lexerTest(JSONReader lexer) {
        Map map = lexer.readObject();
        assertEquals(8, map.size());
    }
}
