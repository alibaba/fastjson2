package com.alibaba.fastjson.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class IOUtilsTest {
    @Test
    public void close() {
        IOUtils.close(null);
        IOUtils.close(() -> {
            throw new RuntimeException();
        });
        IOUtils.close(() -> {});
    }

    @Test
    public void test() {
        assertEquals(2, IOUtils.stringSize(12));
        assertEquals(2, IOUtils.stringSize(12L));

        char[] chars = new char[2];

        IOUtils.getChars(12, 2, chars);
        assertEquals("12", new String(chars));

        IOUtils.getChars(12L, 2, chars);
        assertEquals("12", new String(chars));

        IOUtils.getChars((byte) 12, 2, chars);
        assertEquals("12", new String(chars));
    }

    @Test
    public void isValidJsonpQueryParam() {
        assertTrue(IOUtils.isValidJsonpQueryParam("abc"));
        assertTrue(IOUtils.isValidJsonpQueryParam("abc.ff"));
        assertFalse(IOUtils.isValidJsonpQueryParam("*"));
        assertFalse(IOUtils.isValidJsonpQueryParam(""));
        assertFalse(IOUtils.isValidJsonpQueryParam(null));
    }

    @Test
    public void decodeUTF8() {
        String str = "中国";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        char[] chars = new char[utf8.length];
        int size = IOUtils.decodeUTF8(utf8, 0, utf8.length, chars);
        assertEquals(str, new String(chars, 0, size));
    }
}
