package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WriterSliceBrowserSecureReviewTest {
    @Test
    void characterSliceAndReaderHonorBrowserSecure() {
        char[] chars = "x<>()y".toCharArray();
        for (boolean quoted : new boolean[] {false, true}) {
            try (JSONWriter writer = JSONWriter.ofUTF16(JSONWriter.Feature.BrowserSecure)) {
                writer.writeString(chars, 1, 4, quoted);
                String escapes = "\\u003C\\u003E\\u0028\\u0029";
                assertEquals(quoted ? '"' + escapes + '"' : escapes, writer.toString());
            }
        }
        try (JSONWriter writer = JSONWriter.ofUTF16(JSONWriter.Feature.BrowserSecure)) {
            writer.writeString(new StringReader("<>()"));
            assertEquals("\"\\u003C\\u003E\\u0028\\u0029\"", writer.toString());
        }
    }
}
