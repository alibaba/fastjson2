package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONReaderInfoTest {
    @Test
    public void test0() {
        String str = "{\n" +
                "    \"id\":123,\n" +
                "    \"name\":\"abc\"{}\n" +
                "}";

        Exception error = null;
        try {
            JSON.parseObject(str);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("offset 33"));
        assertTrue(error.getMessage().contains("character {"));
        assertTrue(error.getMessage().contains("line 3"));
        assertTrue(error.getMessage().contains("column 19"));
    }

    @Test
    public void testUTF8() {
        String str = "{\n" +
                "    \"id\":123,\n" +
                "    \"name\":\"abc\"{}\n" +
                "}";
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        Exception error = null;
        try {
            JSON.parseObject(utf8Bytes);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("offset 33"));
        assertTrue(error.getMessage().contains("character {"));
        assertTrue(error.getMessage().contains("line 3"));
        assertTrue(error.getMessage().contains("column 19"));
    }
}
