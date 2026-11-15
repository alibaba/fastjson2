package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
class Issue7809 {
    @Test
    public void backslashAtEndOfFieldName() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"\\"));
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\\"));
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\":1,\"b\\"));
        assertThrows(JSONException.class, () -> JSON.parse("{\"\\".getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\\".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void backslashAtEndOfLongFieldName() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"abcdefgh\\"));
        assertThrows(JSONException.class, () -> JSON.parse("{\"abcdefghi\\"));
        assertThrows(JSONException.class, () -> JSON.parse("{\"abcdefghij\\"));
        assertThrows(JSONException.class, () -> JSON.parse("{\"abcdefgh\\".getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parse("{\"abcdefghij\\".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void longFieldNamesStillParse() {
        // lengths 8, 9 and 10 hit the nameValue0 / nameValue1 fast paths
        assertEquals(1, JSON.parseObject("{\"abcdefgh\":1}").getIntValue("abcdefgh"));
        assertEquals(1, JSON.parseObject("{\"abcdefghi\":1}").getIntValue("abcdefghi"));
        assertEquals(1, JSON.parseObject("{\"abcdefghij\":1}").getIntValue("abcdefghij"));
        assertEquals(1, JSON.parseObject("{\"abcdefgh\":1}".getBytes(StandardCharsets.UTF_8)).getIntValue("abcdefgh"));
        assertEquals(1, JSON.parseObject("{\"abcdefghij\":1}".getBytes(StandardCharsets.UTF_8)).getIntValue("abcdefghij"));
    }

    @Test
    public void escapedFieldNamesStillParse() {
        JSONObject o1 = JSON.parseObject("{\"a\\\"b\":1}");
        assertEquals(1, o1.getIntValue("a\"b"));

        JSONObject o2 = JSON.parseObject("{\"a\\\\b\":1}");
        assertEquals(1, o2.getIntValue("a\\b"));

        JSONObject o3 = JSON.parseObject("{\"a\\u0041b\":1}");
        assertEquals(1, o3.getIntValue("aAb"));

        JSONObject o4 = JSON.parseObject("{\"a\\tb\":1}");
        assertEquals(1, o4.getIntValue("a\tb"));

        assertEquals(1, JSON.parseObject("{\"a\":1}").getIntValue("a"));
        assertEquals(0, JSON.parseObject("{}").size());
    }
}
