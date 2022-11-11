package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSONScannerTest {
    @Test
    public void test() {
        JSONScanner lexer = new JSONScanner("{}");
        assertFalse(lexer.isBlankInput());
        lexer.nextToken();
        assertEquals('}', lexer.getCurrent());
    }

    @Test
    public void test1() {
        JSONScanner lexer = new JSONScanner("{}", JSON.DEFAULT_PARSER_FEATURE);
        lexer.nextToken(JSONToken.LBRACE);
        assertEquals('}', lexer.getCurrent());
        lexer.config(Feature.OrderedField, true);
        assertTrue(lexer.isOrderedField());
    }

    @Test
    public void test2() {
        JSONScanner lexer = new JSONScanner("{\"id\":123}");
        lexer.nextToken();
        lexer.nextToken();
        assertEquals(':', lexer.getCurrent());
        assertEquals("id", lexer.stringVal());
        lexer.nextToken();
        lexer.nextToken();
        assertEquals(123, lexer.intValue());
        assertEquals(123L, lexer.longValue());
        assertEquals('}', lexer.getCurrent());
    }

    @Test
    public void test_true() {
        JSONScanner lexer = new JSONScanner("{\"id\":true}");
        lexer.nextToken();
        lexer.nextToken();
        assertEquals(':', lexer.getCurrent());
        assertEquals("id", lexer.stringVal());
        lexer.nextToken();
        lexer.nextToken();
        assertEquals('}', lexer.getCurrent());
        lexer.nextToken();
        assertTrue(lexer.isEOF());
    }

    @Test
    public void test_false() {
        JSONScanner lexer = new JSONScanner("{\"id\":false}");
        lexer.nextToken();
        lexer.nextToken();
        assertEquals(':', lexer.getCurrent());
        assertEquals("id", lexer.stringVal());
        lexer.nextToken();
        lexer.nextToken();
        assertEquals('}', lexer.getCurrent());
        lexer.nextToken();
        assertTrue(lexer.isEOF());
    }

    @Test
    public void test4_null() {
        JSONScanner lexer = new JSONScanner("{\"id\":null}");
        lexer.nextToken();
        lexer.nextToken();
        assertEquals(':', lexer.getCurrent());
        assertEquals("id", lexer.stringVal());
        lexer.nextToken();
        lexer.nextToken();
        assertEquals('}', lexer.getCurrent());
        lexer.nextToken();
        assertTrue(lexer.isEOF());
    }

    @Test
    public void test4_string() {
        JSONScanner lexer = new JSONScanner("{\"id\":\"abc\"}");
        lexer.nextToken();
        lexer.nextToken();
        assertEquals(':', lexer.getCurrent());
        assertEquals("id", lexer.stringVal());
        lexer.nextToken();
        lexer.nextToken();
        assertEquals("abc", lexer.stringVal());
        assertEquals('}', lexer.getCurrent());
        lexer.nextToken();
        assertTrue(lexer.isEOF());
    }
}
