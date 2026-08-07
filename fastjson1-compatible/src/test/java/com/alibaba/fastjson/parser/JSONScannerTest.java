package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
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
        assertEquals(JSONToken.LBRACE, lexer.token());
        lexer.nextToken();
        assertEquals(JSONToken.LITERAL_STRING, lexer.token());
        assertEquals(':', lexer.getCurrent());
        assertEquals("id", lexer.stringVal());
        lexer.nextToken();
        assertEquals(JSONToken.COLON, lexer.token());
        lexer.nextToken();
        assertEquals(JSONToken.LITERAL_INT, lexer.token());
        assertEquals(123, lexer.intValue());
        assertEquals(123L, lexer.longValue());
        assertEquals('}', lexer.getCurrent());
        lexer.nextToken();
        assertEquals(JSONToken.RBRACE, lexer.token());
        lexer.nextToken();
        assertEquals(JSONToken.EOF, lexer.token());
    }

    @Test
    public void testNumber() {
        JSONScanner lexer = new JSONScanner("{\"id\":123.34}");
        lexer.nextToken();
        assertEquals(JSONToken.LBRACE, lexer.token());
        lexer.nextToken();
        assertEquals(JSONToken.LITERAL_STRING, lexer.token());
        assertEquals(':', lexer.getCurrent());
        assertEquals("id", lexer.stringVal());
        lexer.nextToken();
        assertEquals(JSONToken.COLON, lexer.token());
        lexer.nextToken();
        assertEquals(JSONToken.LITERAL_FLOAT, lexer.token());
        assertEquals(new BigDecimal("123.34"), lexer.decimalValue());
        assertEquals('}', lexer.getCurrent());
        lexer.nextToken();
        assertEquals(JSONToken.RBRACE, lexer.token());
        lexer.nextToken();
        assertEquals(JSONToken.EOF, lexer.token());
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

    @Test
    public void testArray() {
        JSONScanner lexer = new JSONScanner("[]");
        lexer.nextToken();
        assertEquals(JSONToken.LBRACKET, lexer.token);
        lexer.nextToken();
        assertEquals(JSONToken.RBRACKET, lexer.token);
        lexer.nextToken();
        assertTrue(lexer.isEOF());
    }

    @Test
    public void isEnabled() {
        JSONScanner lexer = new JSONScanner("{\"id\":false}", 0);
        assertTrue(lexer.isEnabled(Feature.OrderedField));

        lexer.config(Feature.AllowUnQuotedFieldNames, true);
        assertTrue(lexer.isEnabled(Feature.AllowUnQuotedFieldNames));

        lexer.config(Feature.AllowSingleQuotes, true);
        assertTrue(lexer.isEnabled(Feature.AllowSingleQuotes));

        lexer.config(Feature.UseBigDecimal, true);
        assertTrue(lexer.isEnabled(Feature.UseBigDecimal));

        lexer.config(Feature.SupportArrayToBean, true);
        assertTrue(lexer.isEnabled(Feature.SupportArrayToBean));

        lexer.config(Feature.NonStringKeyAsString, true);
        assertTrue(lexer.isEnabled(Feature.NonStringKeyAsString));

        lexer.config(Feature.ErrorOnEnumNotMatch, true);
        assertTrue(lexer.isEnabled(Feature.ErrorOnEnumNotMatch));

        lexer.config(Feature.SupportClassForName, true);
        assertTrue(lexer.isEnabled(Feature.SupportClassForName));

        lexer.config(Feature.ErrorOnNotSupportAutoType, true);
        assertTrue(lexer.isEnabled(Feature.ErrorOnNotSupportAutoType));

        lexer.config(Feature.UseNativeJavaObject, true);
        assertTrue(lexer.isEnabled(Feature.UseNativeJavaObject));

        lexer.config(Feature.DisableFieldSmartMatch, true);
        assertTrue(lexer.isEnabled(Feature.DisableFieldSmartMatch));

        lexer.config(Feature.SupportAutoType, true);
        assertTrue(lexer.isEnabled(Feature.SupportAutoType));
    }
}
