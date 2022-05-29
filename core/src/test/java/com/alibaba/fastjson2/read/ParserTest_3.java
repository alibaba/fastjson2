package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest_3 {
    String str = " { \"Object\\u0020Id\" : -123 , \"Full \\\" Name\": \"Steve \\\" Jobs\" } ";

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
    public void test_0_utf16_reader() throws Exception {
        try (JSONReader lexer = JSONReader.of(new StringReader(str))) {
            lexerTest(lexer);
        }
    }

    void lexerTest(JSONReader lexer) {
        assertEquals('{', lexer.current());
        lexer.next();
        assertEquals(Fnv.hashCode64("Object Id"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("object id"),
                lexer.getNameHashCodeLCase());
        assertEquals("Object Id", lexer.getFieldName());
        assertEquals(-123,
                lexer.readInt32Value());

        assertEquals(Fnv.hashCode64("Full \" Name"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("full \" name"), lexer.getNameHashCodeLCase());
        assertEquals("Full \" Name", lexer.getFieldName());
        assertEquals("Steve \" Jobs", lexer.readString());

        assertEquals('}', lexer.current());
        lexer.next();
        assertEquals(0x1A, lexer.current());
    }
}
