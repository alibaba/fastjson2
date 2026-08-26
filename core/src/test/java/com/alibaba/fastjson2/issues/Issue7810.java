package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verify that {@code \x} escapes with digits outside the hex table no longer crash
 * with {@link ArrayIndexOutOfBoundsException} in {@code JSONReader.char2()}.
 *
 * <p>{@code DIGITS2} only covers {@code 0}..{@code 'f'}. Any escape character past that
 * (or a byte {@code >= 0x80} arriving as a negative {@code int} in the byte-based
 * readers) previously indexed the array out of bounds. Such characters are not hex
 * digits either, so they now decode to the same value the table already gives to
 * in-range non-hex characters (0), keeping existing semantics intact.
 *
 * @see <a href="https://github.com/alibaba/fastjson2/issues/7810">Issue #7810</a>
 */
public class Issue7810 {
    @Test
    public void testCharsPastTableDoNotCrash() {
        // 'z' (122), 'g' (103), '{' (123) all index past DIGITS2's last entry 'f' (102)
        assertEquals("\u0000", JSON.parse("\"\\xzz\""));
        assertEquals("\u0000", JSON.parse("\"\\xgg\""));
        assertEquals("\u0000", JSON.parse("\"\\x{{\""));
    }

    @Test
    public void testNegativeByteDoesNotCrash() {
        // byte 0xFF read as a signed byte arrives as -1 and indexes below the table
        assertEquals("\u0000", JSON.parse("\"\\x\u00ff\u00ff\""));
        assertEquals("\u0000", JSON.parse("\"\\x\u00ffz\""));
    }

    @Test
    public void testAllContainerPaths() {
        assertEquals("\u0000", JSON.parse("[\"\\xzz\"]").toString());
        assertEquals("\u0000", JSON.parse("{\"a\":\"\\xzz\"}").get("a"));
        assertEquals("\u0000", JSON.parse("{\"\\xzz\":1}").keySet().iterator().next());
    }

    @Test
    public void testValidHexEscapesUnchanged() {
        assertEquals("A", JSON.parse("\"\\x41\""));
        assertEquals("\u00ff", JSON.parse("\"\\xff\""));
        assertEquals("\u0000", JSON.parse("\"\\x00\""));
    }

    @Test
    public void testInRangeNonHexUnchanged() {
        // Characters inside the table that are not hex digits already decoded to 0
        assertEquals("\u0000", JSON.parse("\"\\x::\""));
        assertEquals("\u0000", JSON.parse("\"\\x@@\""));
    }

    @Test
    public void testRegularInputStillWorks() {
        assertEquals("A", JSON.parse("\"\\u0041\""));
        assertEquals("{\"a\":1}", JSON.parse("{\"a\":1}").toString());
        assertEquals("[1,2,3]", JSON.parse("[1,2,3]").toString());
    }

    @Test
    public void testTruncatedEscapeStillThrows() {
        // A trailing backslash with no escape char should remain a clean JSONException
        assertThrows(JSONException.class, () -> JSON.parse("{\"\\"));
    }
}