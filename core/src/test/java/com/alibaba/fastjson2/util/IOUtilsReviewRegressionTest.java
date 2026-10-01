package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IOUtilsReviewRegressionTest {
    @Test
    public void digitPairReadsBothCharacters() {
        assertEquals(true, IOUtils.isDigit2("!09!".toCharArray(), 1));
        assertEquals(false, IOUtils.isDigit2("!0x!".toCharArray(), 1));
        assertEquals(false, IOUtils.isDigit2("!x0!".toCharArray(), 1));
    }

    @Test
    public void truncatedUtf8IsRejectedByBothDecoders() {
        for (byte[] input : new byte[][]{{(byte) 0xc2}, {'a', (byte) 0xdf}, {(byte) 0xe2, (byte) 0x82}}) {
            assertEquals(-1, IOUtils.decodeUTF8(input, 0, input.length, new byte[16]));
            assertEquals(-1, IOUtils.decodeUTF8(input, 0, input.length, new char[8]));
        }
    }
}
