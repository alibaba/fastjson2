package com.alibaba.fastjson2.issues_7900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
public class Issue7942 {
    static String[] truncated() {
        return new String[]{
                "{\"",
                "{\"中",
                "{\"中a",
                "{\"中文",
                "{\"abc中",
                "{\"中\\\""
        };
    }

    @Test
    public void truncatedQuotedFieldName() {
        for (String input : truncated()) {
            assertThrows(JSONException.class, () -> JSONReader.of(input).readAny(), input);
            assertThrows(JSONException.class, () -> JSONReader.of(input.toCharArray()).readAny(), input);
            assertThrows(JSONException.class, () -> JSONReader.of(input).read(JSONObject.class), input);
            assertThrows(JSONException.class, () -> JSONReader.of(input.toCharArray()).read(JSONObject.class), input);
        }
    }

    @Test
    public void truncatedQuotedFieldNameFromBytes() {
        // JSON.parse(byte[]) selects the UTF-8 reader when the input is not pure ASCII,
        // and escaped field names reach the same fallback loop via readFieldNameHashCode0()
        for (String input : truncated()) {
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            assertThrows(JSONException.class, () -> JSON.parse(bytes), input);
            assertThrows(JSONException.class, () -> JSONReader.of(bytes).readAny(), input);
            assertThrows(JSONException.class, () -> JSONReader.of(ByteBuffer.wrap(bytes), StandardCharsets.UTF_8).readAny(), input);
        }
    }

    @Test
    public void completeFieldNamesStillParse() {
        // non-ASCII names go through the same fallback loop as the truncated ones above
        assertEquals(1, JSONReader.of("{\"中文\":1}").read(JSONObject.class).getIntValue("中文"));
        assertEquals(1, JSONReader.of("{\"中文字段名称\":1}").read(JSONObject.class).getIntValue("中文字段名称"));
        assertEquals(1, JSONReader.of("{\"abcdefghij\":1}").read(JSONObject.class).getIntValue("abcdefghij"));
        assertEquals(1, JSONReader.of("{\"a\\u0041b\":1}").read(JSONObject.class).getIntValue("aAb"));
        assertEquals(1, JSONReader.of("{\"a\\\"b\":1}").read(JSONObject.class).getIntValue("a\"b"));

        JSONObject emptyName = JSONReader.of("{\"\":1}").read(JSONObject.class);
        assertEquals(1, emptyName.getIntValue(""));

        JSONObject chars = JSONReader.of("{\"中文\":2}".toCharArray()).read(JSONObject.class);
        assertEquals(2, chars.getIntValue("中文"));

        // byte[] entry: escaped names that do end properly must still be hashed, not rejected
        assertEquals(1, JSON.parseObject("{\"中文\":1}".getBytes(StandardCharsets.UTF_8)).getIntValue("中文"));
        assertEquals(1, JSON.parseObject("{\"中\\\"q\":1}".getBytes(StandardCharsets.UTF_8)).getIntValue("中\"q"));
        assertEquals(1, JSON.parseObject("{\"中文\\\\b\":1}".getBytes(StandardCharsets.UTF_8)).getIntValue("中文\\b"));
        assertEquals(1, JSON.parseObject("{\"abc中\":1}".getBytes(StandardCharsets.UTF_8)).getIntValue("abc中"));
    }
}
