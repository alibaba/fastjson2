package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilsReviewRegressionTest {
    @Test
    public void escapeLatin1Characters() {
        byte[] output = new byte[100];
        byte[] input = "\u0080\u00ff".getBytes(StandardCharsets.ISO_8859_1);
        int end = StringUtils.writeLatin1Escaped(output, 0, input, (byte) '"', JSONWriter.Feature.EscapeNoneAscii.mask);
        assertEquals("\"\\u0080\\u00FF\"", new String(output, 0, end, StandardCharsets.UTF_8));
    }

    @Test
    public void escapeUtf16TwoByteCharacters() {
        String input = "\u0080\u00ff\u0100\u07ff\u0800";
        byte[] utf16 = new byte[input.length() * 2];
        for (int i = 0; i < input.length(); i++) {
            JDKUtils.UNSAFE.putChar(utf16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + i * 2L, input.charAt(i));
        }
        byte[] output = new byte[100];
        int end = StringUtils.writeUTF16(output, 0, utf16, (byte) '"', JSONWriter.Feature.EscapeNoneAscii.mask);
        assertEquals("\"\\u0080\\u00FF\\u0100\\u07FF\\u0800\"", new String(output, 0, end, StandardCharsets.UTF_8));
        end = StringUtils.writeUTF16(output, 0, utf16, (byte) '"', 0);
        assertEquals('"' + input + '"', new String(output, 0, end, StandardCharsets.UTF_8));
    }
}
