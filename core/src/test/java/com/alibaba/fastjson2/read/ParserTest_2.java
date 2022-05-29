package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest_2 {
    String str = " { \"序号\" : 123 , \"名称\": \"计算平台\" } ";

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
    public void test_0_utf16_is() throws Exception {
        JSONReader lexer = JSONReader.of(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_16)), StandardCharsets.UTF_16);
        lexerTest(lexer);
    }

    void lexerTest(JSONReader lexer) {
        assertEquals('{', lexer.current());
        lexer.next();
        assertEquals(Fnv.hashCode64("序号"),
                lexer.readFieldNameHashCode());
        assertEquals("序号", lexer.getFieldName());
        assertEquals(123, lexer.readInt32Value());

        assertEquals(Fnv.hashCode64("名称"), lexer.readFieldNameHashCode());
        assertEquals("名称", lexer.getFieldName());
        assertEquals("计算平台",
                lexer.readString());

        assertEquals('}', lexer.current());
        lexer.next();
        assertEquals(0x1A, lexer.current());
    }
}
