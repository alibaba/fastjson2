package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JSONBTest1 {
    String str = " { \n" +
            "     \"id\" : 123456789 ,\n" +
            "     \"v0\": \"123\", \n" +
            "     \"v1\":123456789012345678, \n" +
            "     \"v2\":123456789012345678901234567890, \n" +
            "     \"v3\": 123.45, \n" +
            "     \"v4\": 123e4, \n" +
            "     \"v5\":\"abcdefg\", \n" +
            "     \"v6\": null, \n" +
            "     \"v7\": \"null\" , \n" +
            "     \"v8\":true, \n" +
            "     \"v9\":false, \n" +
            "     \"v10\": {\"val\":123}, \n" +
            "     \"v11\": {}, \n" +
            "     \"v12\": [1234], \n" +
            "     \"v13\": [12.34] , \n" +
            "     \"v14\": [null] \n" +
            " } ";

    @Test
    public void test_0_ascii_bytes() {
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        JSONReader lexer = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
        jsonbTest(lexer);
    }

    @Test
    public void test_0_utf8() {
        JSONReader lexer = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
        jsonbTest(lexer);
    }

    @Test
    public void test_0_utf16() {
        JSONReader lexer = JSONReader.of(str.toCharArray());
        jsonbTest(lexer);
    }

    @Test
    public void test_0_string() {
        JSONReader lexer = JSONReader.of(str);
        jsonbTest(lexer);
    }

    @Test
    public void test_0_utf16_bytes() {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_16);
        JSONReader lexer = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_16);
        jsonbTest(lexer);
    }

    @Test
    public void test_0_utf8_is() {
        JSONReader lexer = JSONReader.of(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        jsonbTest(lexer);
    }

    @Test
    public void test_0_utf16_is() {
        JSONReader lexer = JSONReader.of(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_16)), StandardCharsets.UTF_16);
        jsonbTest(lexer);
    }

    @Test
    public void test_0_utf16_reader() {
        JSONReader lexer = JSONReader.of(new StringReader(str));
        jsonbTest(lexer);
    }

    void jsonbTest(JSONReader p) {
        Map<String, Object> object = p.readObject();

        JSONWriter writer = JSONWriter.ofJSONB();
        writer.writeAny(object);
        byte[] bytes = writer.getBytes();
        System.out.println("jsonb size : " + bytes.length);

        JSONReader jsonReader = JSONReader.ofJSONB(bytes, 0, bytes.length);
        Object jsonb = jsonReader.readAny();
        System.out.println(jsonb);
    }
}
