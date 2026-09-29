package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
public class UnicodeEscapeStrictnessTest {
    // Before the fix, hexDigit4() decoded the four bytes/chars of a \\uXXXX escape
    // with a branchless bit-twiddle that assumed valid hex, so a non-hex digit was
    // silently turned into a garbage char instead of being rejected. That let a
    // strict validator and fastjson2 disagree on the same input (parser
    // differential). RFC 8259 requires exactly four hex digits after \\u.

    @Test
    public void validEscapesStillDecode() {
        assertEquals("A", JSON.parseObject("{\"a\":\"\\u0041\"}").getString("a"));
        assertEquals("\u00ff", JSON.parseObject("{\"a\":\"\\u00ff\"}").getString("a"));
        assertEquals("\u00ff", JSON.parseObject("{\"a\":\"\\u00FF\"}").getString("a"));
        assertEquals("\u4e2d", JSON.parseObject("{\"a\":\"\\u4e2d\"}").getString("a"));
        // surrogate pair -> supplementary code point
        assertEquals("\uD83D\uDE00", JSON.parseObject("{\"a\":\"\\uD83D\\uDE00\"}").getString("a"));
        // byte[] path (UTF-8)
        byte[] utf8 = "{\"a\":\"x\\u0041y\"}".getBytes(StandardCharsets.UTF_8);
        assertEquals("xAy", JSON.parseObject(utf8).getString("a"));
    }

    @Test
    public void invalidHexRejectedString() {
        // char[]/UTF-16 backing (string content forces a non-latin1 char)
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"a\":\"\\u4e2d\\uZZ2d\"}"));
        // latin1/ASCII backing (byte[] reader)
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"a\":\"\\uZZZZ\"}"));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"a\":\"\\u00GG\"}"));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"a\":\"\\u!!!!\"}"));
        // one bad digit in each position
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"a\":\"\\uG123\"}"));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"a\":\"\\u1G23\"}"));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"a\":\"\\u12G3\"}"));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"a\":\"\\u123G\"}"));
    }

    @Test
    public void invalidHexRejectedBytes() {
        assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"a\":\"\\uZZZZ\"}".getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"a\":\"\\u12G4\"}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void hexDigit4Bytes() {
        assertEquals(0x1a2b, IOUtils.hexDigit4("1a2b".getBytes(StandardCharsets.US_ASCII), 0));
        assertEquals(0xABCD, IOUtils.hexDigit4("ABCD".getBytes(StandardCharsets.US_ASCII), 0));
        assertThrows(JSONException.class, () -> IOUtils.hexDigit4("12G4".getBytes(StandardCharsets.US_ASCII), 0));
        assertThrows(JSONException.class, () -> IOUtils.hexDigit4("zzzz".getBytes(StandardCharsets.US_ASCII), 0));
    }

    @Test
    public void hexDigit4Chars() {
        assertEquals(0x1a2b, IOUtils.hexDigit4("1a2b".toCharArray(), 0));
        assertEquals(0xABCD, IOUtils.hexDigit4("ABCD".toCharArray(), 0));
        assertThrows(JSONException.class, () -> IOUtils.hexDigit4("12G4".toCharArray(), 0));
        assertThrows(JSONException.class, () -> IOUtils.hexDigit4("zzzz".toCharArray(), 0));
    }
}
