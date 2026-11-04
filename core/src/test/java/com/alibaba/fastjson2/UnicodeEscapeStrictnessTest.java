package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
public class UnicodeEscapeStrictnessTest {
    // Before the fix, hexDigit4() decoded the four bytes/chars of a \\uXXXX escape
    // with a branchless bit-twiddle that assumed valid hex, so a non-hex digit was
    // silently turned into a garbage char instead of being rejected. That let a
    // strict validator and fastjson2 disagree on the same input (parser
    // differential). RFC 8259 requires exactly four hex digits after \\u.
    //
    // The skip surface (JSON.isValid / JSONValidator, which walk escapes without
    // decoding them) has to apply the same rule, otherwise the validator accepts
    // what the parser rejects and the differential just moves inside the library.

    // JSON text whose name is non-Latin-1, so JSONReader.of(String) picks the UTF16
    // coder on every JDK instead of JSONReaderASCII on JDK 9+. The \\uXXXX escape
    // itself is plain ASCII and does not affect the coder.
    private static final String UTF16_BAD = "{\"\u4e2d\":\"\\u4e2d\\uZZ2d\"}";
    private static final String UTF16_OK = "{\"\u4e2d\":\"\\u4e2d\"}";

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
        // char[]/UTF-16 path
        assertEquals("\u4e2d", JSON.parseObject(UTF16_OK).getString("\u4e2d"));
    }

    @Test
    public void invalidHexRejectedString() {
        // char[]/UTF-16 reader - the non-Latin-1 name forces coder == UTF16, so this
        // input reaches the UTF-16 escape path on every JDK, not only on JDK 8
        assertThrows(JSONException.class, () -> JSON.parseObject(UTF16_BAD));
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
    public void rangeBoundaries() {
        // just outside each of isHex's six range bounds
        for (char bad : "/:@`g".toCharArray()) {
            String doc = "{\"a\":\"\\u00" + bad + "0\"}";
            assertThrows(JSONException.class, () -> JSON.parseObject(doc), doc);
            assertThrows(JSONException.class, () -> JSON.parseObject(doc.getBytes(StandardCharsets.UTF_8)), doc);
            assertFalse(JSON.isValid(doc), doc);
        }
        // and just inside them
        for (char good : "09AFaf".toCharArray()) {
            String doc = "{\"a\":\"\\u00" + good + "0\"}";
            assertTrue(JSON.isValid(doc), doc);
            assertEquals(1, JSON.parseObject(doc).getString("a").length(), doc);
        }
    }

    @Test
    public void validatorsAgreeWithParser() {
        // JSON.isValid / JSONValidator skip escapes without decoding them, so they
        // have to apply the same rule; otherwise a pre-flight check passes a document
        // that then fails at parse time
        String bad = "{\"a\":\"\\uZZZZ\"}";
        assertFalse(JSON.isValid(bad));
        assertFalse(JSON.isValid(bad.getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValid(bad.toCharArray()));
        assertFalse(JSONValidator.from(bad).validate());
        assertFalse(JSONValidator.fromUtf8(bad.getBytes(StandardCharsets.UTF_8)).validate());
        // forged field name, plus values only ever reached by the skip path
        assertFalse(JSON.isValid("{\"\\uZZZZ\":1}"));
        assertFalse(JSON.isValid("{\"a\":{\"b\":\"\\uZZZZ\"}}"));
        assertFalse(JSON.isValid("[\"\\uZZZZ\"]"));
        // char[]/UTF-16 reader
        assertFalse(JSON.isValid(UTF16_BAD));
        assertFalse(JSONValidator.from(UTF16_BAD).validate());
        assertFalse(JSON.isValid("{\"\u4e2d\\uZZ2d\":1}"));

        // valid escapes still validate, and a truncated escape still fails
        assertTrue(JSON.isValid("{\"a\":\"\\u0041\"}"));
        assertTrue(JSON.isValid("{\"a\":\"\\u0041\"}".getBytes(StandardCharsets.UTF_8)));
        assertTrue(JSON.isValid("{\"\\u0041\":1}"));
        assertTrue(JSON.isValid(UTF16_OK));
        assertFalse(JSON.isValid("{\"a\":\"\\u12\"}"));
        assertFalse(JSON.isValid("{\"a\":\"\\u12\"}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void invalidHexEscapeX() {
        // \\x is a fastjson extension decoded by JSONReader.char2, which indexed
        // DIGITS2 unchecked: an in-range non-hex digit decoded to U+0000, and
        // anything past the table's last hex entry threw ArrayIndexOutOfBoundsException
        for (String bad : new String[]{"{\"a\":\"\\xGG\"}", "{\"a\":\"\\xzz\"}", "{\"a\":\"\\xgg\"}"}) {
            assertThrows(JSONException.class, () -> JSON.parseObject(bad), bad);
            assertThrows(JSONException.class,
                    () -> JSON.parseObject(bad.getBytes(StandardCharsets.UTF_8)), bad);
            assertFalse(JSON.isValid(bad), bad);
            assertFalse(JSON.isValid(bad.getBytes(StandardCharsets.UTF_8)), bad);
        }
        // char[]/UTF-16 reader
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"\u4e2d\":\"\\xGG\"}"));
        assertFalse(JSON.isValid("{\"\u4e2d\":\"\\xGG\"}"));
        // valid \\x still decodes and validates
        assertEquals("A", JSON.parseObject("{\"a\":\"\\x41\"}").getString("a"));
        assertEquals("A", JSON.parseObject("{\"a\":\"\\x41\"}".getBytes(StandardCharsets.UTF_8)).getString("a"));
        assertTrue(JSON.isValid("{\"a\":\"\\x41\"}"));
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

    @Test
    public void isHexBoundaries() {
        for (char ch : "0123456789ABCDEFabcdef".toCharArray()) {
            assertTrue(IOUtils.isHex(ch), String.valueOf(ch));
        }
        for (char ch : "/:@`gGZz!\u0000 ".toCharArray()) {
            assertFalse(IOUtils.isHex(ch), String.valueOf(ch));
        }
        assertFalse(IOUtils.isHex(-1));
    }
}
