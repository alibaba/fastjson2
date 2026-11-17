package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("util")
public class DateUtilsTestErrorHandling {
    // test needs positive offset for the problem to surface
    private static final String invalidString = ">>1234567890<<";
    private static final int offset = invalidString.indexOf('1');

    @Test
    void parseLocalDateTime12Bytes() {
        byte[] invalidBytes = invalidString.getBytes(StandardCharsets.UTF_8);

        assertThrows(DateTimeParseException.class, () -> DateUtils.parseLocalDateTime12(invalidBytes, offset));
    }

    @Test
    void parseLocalDateTime12Chars() {
        int len = invalidString.length();
        char[] invalidChars = new char[len];
        invalidString.getChars(0, len, invalidChars, 0);

        assertThrows(DateTimeParseException.class, () -> DateUtils.parseLocalDateTime12(invalidChars, offset));
    }
}
