package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
class Issue7808 {
    // JSON source text for the field name: a backslash+t escape (2 source chars,
    // unescaped by the parser to one TAB) followed by 3 raw latin1 bytes
    // (E0 AA AE) that the inherited UTF-8 field-name decoder misreads as a single
    // 3-byte character, undercounting nameLength against the actual byte span.
    // See gh-7808.
    private static final String KEY_SOURCE = "\\t\u00e0\u00aa\u00ae";
    // The field name after JSON unescaping: TAB + the 3 latin1 characters.
    private static final String KEY_PARSED = "\t\u00e0\u00aa\u00ae";

    @Test
    public void fieldNameWithEscapeAndNonAsciiInsideArray_doesNotThrow() {
        String json = "[{\"" + KEY_SOURCE + "\":1}]";

        JSONArray array = assertDoesNotThrow(() -> JSON.parseArray(json));
        JSONObject obj = array.getJSONObject(0);
        assertEquals(1, obj.size());
        assertEquals(1, obj.getIntValue(KEY_PARSED));
    }

    @Test
    public void nonAsciiWithoutEscape_stillParses() {
        // Control: same non-ASCII bytes, no escape -- takes the byte-count fast path.
        String json = "[{\"a\u00e0\u00aa\u00ae\":1}]";
        JSONObject obj = JSON.parseArray(json).getJSONObject(0);
        assertEquals(1, obj.getIntValue("a\u00e0\u00aa\u00ae"));
    }

    @Test
    public void escapeWithoutNonAscii_stillParses() {
        // Control: escape present, but no byte >= 0x80 -- nameLength was already correct.
        String json = "[{\"\\tabc\":1}]";
        JSONObject obj = JSON.parseArray(json).getJSONObject(0);
        assertEquals(1, obj.getIntValue("\tabc"));
    }

    @Test
    public void sameFieldNameAtTopLevel_stillParses() {
        // Control: not inside an array -- doesn't route through ObjectReaderImplObject.
        String json = "{\"" + KEY_SOURCE + "\":1}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals(1, obj.getIntValue(KEY_PARSED));
    }

    @Test
    public void multipleObjectsInArray_stillParse() {
        JSONArray array = JSON.parseArray("[{\"a\":1},{\"b\":2}]");
        assertEquals(1, array.getJSONObject(0).getIntValue("a"));
        assertEquals(2, array.getJSONObject(1).getIntValue("b"));
    }

    @Test
    public void unicodeEscapedFieldName_stillParses() {
        JSONObject obj = JSON.parseArray("[{\"\\u00e9\":1}]").getJSONObject(0);
        assertEquals(1, obj.getIntValue("\u00e9"));
    }

    @Test
    public void backslashEscapedFieldName_stillParses() {
        JSONObject obj = JSON.parseArray("[{\"a\\\\b\":1}]").getJSONObject(0);
        assertEquals(1, obj.getIntValue("a\\b"));
    }

    @Test
    public void quoteEscapedFieldName_stillParses() {
        JSONObject obj = JSON.parseArray("[{\"a\\\"b\":1}]").getJSONObject(0);
        assertEquals(1, obj.getIntValue("a\"b"));
    }

    @Test
    public void emptyFieldName_stillParses() {
        JSONObject obj = JSON.parseArray("[{\"\":1}]").getJSONObject(0);
        assertEquals(1, obj.getIntValue(""));
    }

    @Test
    public void controlCharacterEscapes_stillParse() {
        JSONObject obj = JSON.parseArray("[{\"\\n\\t\\r\":1}]").getJSONObject(0);
        assertEquals(1, obj.getIntValue("\n\t\r"));
    }
}
