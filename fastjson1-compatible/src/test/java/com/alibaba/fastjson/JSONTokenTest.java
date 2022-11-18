package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.JSONToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTokenTest {
    @Test
    public void test_0() throws Exception {
        new JSONToken();
        assertEquals("int", JSONToken.name(JSONToken.LITERAL_INT));
        assertEquals("float", JSONToken.name(JSONToken.LITERAL_FLOAT));
        assertEquals("string", JSONToken.name(JSONToken.LITERAL_STRING));
        assertEquals("iso8601", JSONToken.name(JSONToken.LITERAL_ISO8601_DATE));
        assertEquals("true", JSONToken.name(JSONToken.TRUE));
        assertEquals("false", JSONToken.name(JSONToken.FALSE));
        assertEquals("null", JSONToken.name(JSONToken.NULL));
        assertEquals("new", JSONToken.name(JSONToken.NEW));
        assertEquals("(", JSONToken.name(JSONToken.LPAREN));
        assertEquals(")", JSONToken.name(JSONToken.RPAREN));
        assertEquals("{", JSONToken.name(JSONToken.LBRACE));
        assertEquals("}", JSONToken.name(JSONToken.RBRACE));
        assertEquals("[", JSONToken.name(JSONToken.LBRACKET));
        assertEquals("]", JSONToken.name(JSONToken.RBRACKET));
        assertEquals(",", JSONToken.name(JSONToken.COMMA));
        assertEquals(":", JSONToken.name(JSONToken.COLON));
        assertEquals("ident", JSONToken.name(JSONToken.IDENTIFIER));
        assertEquals("fieldName", JSONToken.name(JSONToken.FIELD_NAME));
        assertEquals("EOF", JSONToken.name(JSONToken.EOF));
        assertEquals("Unknown", JSONToken.name(Integer.MAX_VALUE));
        assertEquals("Set", JSONToken.name(JSONToken.SET));
        assertEquals("TreeSet", JSONToken.name(JSONToken.TREE_SET));
        assertEquals("undefined", JSONToken.name(JSONToken.UNDEFINED));
        assertEquals("error", JSONToken.name(JSONToken.ERROR));
        assertEquals(";", JSONToken.name(JSONToken.SEMI));
        assertEquals(".", JSONToken.name(JSONToken.DOT));
        assertEquals("hex", JSONToken.name(JSONToken.HEX));
    }
}
