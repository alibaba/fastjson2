package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verify that malformed JSON ending with whitespace after a field name
 * throws JSONException instead of ArrayIndexOutOfBoundsException.
 *
 * @see <a href="https://github.com/alibaba/fastjson2/issues/7826">Issue #7826</a>
 */
public class Issue7826 {
    @Test
    public void testFieldNameTrailingSpace() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\" "));
    }

    @Test
    public void testFieldNameTrailingMultipleSpaces() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\"   "));
    }

    @Test
    public void testFieldNameTrailingTab() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\"\t"));
    }

    @Test
    public void testFieldNameTrailingNewline() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\"\n"));
    }

    @Test
    public void testColonTrailingWhitespace() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\":  "));
    }

    @Test
    public void testValidInputStillWorks() {
        // Normal input should not throw
        JSON.parse("{\"a\":1}");
    }
}
