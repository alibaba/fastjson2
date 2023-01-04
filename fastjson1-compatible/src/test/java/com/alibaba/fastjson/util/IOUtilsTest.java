package com.alibaba.fastjson.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
