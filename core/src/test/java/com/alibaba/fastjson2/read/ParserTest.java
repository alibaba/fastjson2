package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {
    String str = " { \"id\" : 123 , \"name\": \"jobs\" } ";

    @Test
    public void test_0_ascii_bytes() throws Exception {
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        JSONReader lexer = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf8() throws Exception {
        JSONReader lexer = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16() throws Exception {
        JSONReader lexer = JSONReader.of(str.toCharArray());
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16_bytes() throws Exception {
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
        assertEquals('{', lexer.current());
        lexer.next();
        assertEquals(Fnv.hashCode64LCase("id"), lexer.readFieldNameHashCode());
        assertEquals(123,
                lexer.readInt32Value());

        assertEquals(Fnv.hashCode64LCase("name"), lexer.readFieldNameHashCode());
        assertEquals("jobs",
                lexer.readString());

        assertEquals('}', lexer.current());
        lexer.next();
        assertEquals(0x1A, lexer.current());
    }
}
