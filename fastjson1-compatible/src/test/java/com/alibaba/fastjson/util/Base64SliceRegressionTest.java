package com.alibaba.fastjson.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Base64SliceRegressionTest {
    @Test
    public void mimeEncodedSlices() {
        for (int length : new int[]{58, 114, 115, 200}) {
            byte[] expected = new byte[length];
            for (int i = 0; i < length; i++) {
                expected[i] = (byte) i;
            }
            String encoded = java.util.Base64.getMimeEncoder().encodeToString(expected);
            String padded = "!!" + encoded + "??";
            assertArrayEquals(expected, Base64.decodeFast(padded.toCharArray(), 2, encoded.length()));
            assertArrayEquals(expected, Base64.decodeFast(padded, 2, encoded.length()));
            assertArrayEquals(expected, Base64.decodeFast(padded));
            assertArrayEquals(expected, IOUtils.decodeBase64(padded));
            assertArrayEquals(expected, Base64.decodeFast(padded.toCharArray(), 0, padded.length()));
            assertArrayEquals(expected, Base64.decodeFast(padded, 0, padded.length()));
        }
    }
}
