package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONReaderUTF16Test {
    @Test
    public void truncatedEscape() {
        String[] inputs = {
                "\"\\",
                "{\"a\":\"\\",
                "[\"\\",
                "\"ab\\"
        };

        for (String input : inputs) {
            assertThrows(JSONException.class, () -> JSON.parse(input));
            assertThrows(JSONException.class, () -> JSON.parse(input.toCharArray()));
            assertThrows(JSONException.class, () -> JSON.parse(input.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
