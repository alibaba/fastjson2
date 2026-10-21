package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONReaderUTF8TruncatedNullTest {
    @Test
    public void truncatedNullInArrayThrowsJSONException() {
        String[] inputs = {"[n", "[nu", "[nul", "[truen", "[true,n"};
        for (String input : inputs) {
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            assertThrows(JSONException.class, () -> JSON.parse(bytes), input);
        }
    }

    @Test
    public void directReadNullChecksRemainingInput() {
        byte[] bytes = "n".getBytes(StandardCharsets.UTF_8);
        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            assertThrows(JSONException.class, jsonReader::readNull);
        }
    }

    @Test
    public void truncatedArrayAfterCompleteNullIsRejected() {
        byte[] bytes = "[null".getBytes(StandardCharsets.UTF_8);
        assertThrows(JSONException.class, () -> JSON.parse(bytes));
    }

    @Test
    public void completeNullStillParses() {
        assertEquals("[null]", JSON.parse("[null]").toString());
        assertEquals("[true,null]", JSON.parse("[true,null]").toString());
    }
}
