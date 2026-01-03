package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Issue3928 {
    @Test
    public void testIssue() {
        Map<String, String> map = of("\uD83D\uDE07\\", "");

        String jsonString = JSON.toJSONString(map);
        assertEquals(map, JSON.parseObject(jsonString, Map.class));

        byte[] jsonBytes = JSON.toJSONBytes(map, StandardCharsets.UTF_8);
        assertEquals(map, JSON.parseObject(jsonBytes, Map.class));
    }

    @Test
    public void emojiWithBackslash_roundTripBytes() {
        Map<String, String> map = of("😇\\", "");
        byte[] jsonBytes = JSON.toJSONBytes(map, StandardCharsets.UTF_8);
        assertEquals(map, JSON.parseObject(jsonBytes, Map.class));
    }

    @Test
    public void emojiWithEscapedUnicode_roundTripBytes() {
        Map<String, String> map = of("😇\\u0041", "v");
        byte[] jsonBytes = JSON.toJSONBytes(map, StandardCharsets.UTF_8);
        assertEquals(map, JSON.parseObject(jsonBytes, Map.class));
    }

    @Test
    public void escapedUnicodeNonAscii_roundTripBytes() {
        Map<String, String> map = of("\\u4F60\\u597D", "v");
        byte[] jsonBytes = JSON.toJSONBytes(map, StandardCharsets.UTF_8);
        assertEquals(map, JSON.parseObject(jsonBytes, Map.class));
    }

    @Test
    public void mixedAsciiAndEmojiAndBackslash_roundTripBytes() {
        Map<String, String> map = of("key😇\\tail", "v");
        byte[] jsonBytes = JSON.toJSONBytes(map, StandardCharsets.UTF_8);
        assertEquals(map, JSON.parseObject(jsonBytes, Map.class));
    }

    @Test
    public void doubleBackslashTail_roundTripBytes() {
        Map<String, String> map = of("😇\\\\", "v");
        byte[] jsonBytes = JSON.toJSONBytes(map, StandardCharsets.UTF_8);
        assertEquals(map, JSON.parseObject(jsonBytes, Map.class));
    }

    static Map<String, String> of(String key, String value) {
        HashMap<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
