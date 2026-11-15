package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * When a quoted token sits at the end of the input, readString0 must not read past the
 * end of the buffer while skipping the whitespace that follows the closing quote.
 */
public class JSONReaderReadString0BoundsTest {
    @Test
    public void testAsciiStringAtEnd() {
        // JSONReaderASCII
        assertThrows(JSONException.class, () -> JSON.parseObject("\" abc\"", Integer.class));
        assertThrows(JSONException.class, () -> JSON.parseObject("\" abc\" ", Integer.class));
        assertThrows(JSONException.class, () -> JSON.parseObject("\" 3 \"", Integer.class));
        assertThrows(JSONException.class, () -> JSON.parseObject("\" 3 \"\n", Integer.class));
    }

    @Test
    public void testUtf8ReaderAtEnd() {
        // JSONReaderUTF8 (non-ASCII UTF-8 bytes)
        assertThrows(JSONException.class, () -> JSON.parseObject("\"中\"".getBytes(StandardCharsets.UTF_8), Integer.class));
        assertThrows(JSONException.class, () -> JSON.parseObject("\"中\" ".getBytes(StandardCharsets.UTF_8), Integer.class));
    }

    @Test
    public void testUtf16ReaderAtEnd() {
        // JSONReaderUTF16
        assertThrows(JSONException.class, () -> JSON.parseObject("\"中\"", Integer.class));
        assertThrows(JSONException.class, () -> JSON.parseObject("\"中\" ", Integer.class));
    }

    @Test
    public void testQuotedNumbersStillParse() {
        assertEquals(123, JSON.parseObject("\"123\"", Integer.class));
        assertEquals(123, JSON.parseObject("\"123\" ", Integer.class));
        assertEquals(1.5D, JSON.parseObject("\"1.5\" ", Double.class));
        assertEquals(123, JSON.parseObject("\"123\"\n".getBytes(StandardCharsets.UTF_8), Integer.class));
    }
}
