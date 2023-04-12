package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest_bigInt {
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
        assertEquals(Fnv.hashCode64("id"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("id"), lexer.getNameHashCodeLCase());
        assertEquals("id", lexer.getFieldName());
        assertEquals(BigInteger.valueOf(123456789),
                lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v0"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v0"), lexer.getNameHashCodeLCase());
        assertEquals("v0", lexer.getFieldName());
        assertEquals(BigInteger.valueOf(123),
                lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v1"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v1"), lexer.getNameHashCodeLCase());
        assertEquals("v1", lexer.getFieldName());
        assertEquals(BigInteger.valueOf(123456789012345678L),
                lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v2"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v2"), lexer.getNameHashCodeLCase());
        assertEquals("v2", lexer.getFieldName());
        assertEquals(new BigInteger("123456789012345678901234567890"),
                lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v3"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v3"), lexer.getNameHashCodeLCase());
        assertEquals("v3", lexer.getFieldName());
        assertEquals(BigInteger.valueOf(123),
                lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v4"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v4"), lexer.getNameHashCodeLCase());
        assertEquals("v4", lexer.getFieldName());
        assertEquals(BigInteger.valueOf((long) 123e4),
                lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v5"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v5"), lexer.getNameHashCodeLCase());
        assertEquals("v5", lexer.getFieldName());

        JSONException error = null;
        try {
            lexer.readBigInteger();
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);

        assertEquals(Fnv.hashCode64("v6"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v6"), lexer.getNameHashCodeLCase());
        assertEquals("v6", lexer.getFieldName());
        assertNull(lexer
                .readBigInteger());

        assertEquals(Fnv.hashCode64("v7"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v7"), lexer.getNameHashCodeLCase());
        assertEquals("v7", lexer.getFieldName());
        assertNull(lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v8"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v8"), lexer.getNameHashCodeLCase());
        assertEquals("v8", lexer.getFieldName());
        assertEquals(BigInteger.ONE,
                lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v9"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v9"), lexer.getNameHashCodeLCase());
        assertEquals("v9", lexer.getFieldName());
        assertEquals(BigInteger.ZERO, lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v10"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v10"), lexer.getNameHashCodeLCase());
        assertEquals("v10", lexer.getFieldName());
        assertEquals(BigInteger.valueOf(123),
                lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v11"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v11"), lexer.getNameHashCodeLCase());
        assertEquals("v11", lexer.getFieldName());
        assertNull(lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v12"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v12"), lexer.getNameHashCodeLCase());
        assertEquals("v12", lexer.getFieldName());
        assertEquals(BigInteger.valueOf(1234), lexer
                .readBigInteger());

        assertEquals(Fnv.hashCode64("v13"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v13"), lexer.getNameHashCodeLCase());
        assertEquals("v13", lexer.getFieldName());
        assertEquals(BigInteger.valueOf(12), lexer.readBigInteger());

        assertEquals(Fnv.hashCode64("v14"), lexer.readFieldNameHashCode());
        assertEquals(Fnv.hashCode64LCase("v14"), lexer.getNameHashCodeLCase());
        assertEquals("v14", lexer.getFieldName());
        assertNull(lexer.readBigInteger());

        assertEquals('}', lexer.current());
        lexer.next();
        assertEquals(0x1A, lexer.current());
    }
}
