package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue7685 {
    @Test
    public void test() {
        String str = "{\"Test\":[{\"message\":\"中\\n\"},{\"message\":\"s Howard), dedicated to dinosaur conservation, was invited by Benjamin Lockwood. Benjamin and his subordinate Eli Mills (played by Rafi Spo) hope that she and relevant technicians will go to Isla Nublar to save the dinosaurs there and relocate them to a new habitat. Claire, who is infected, manages to bring Owen (played by Chris Pratt) along to go, while Owen hopes to rescue the velociraptor Blue that he has personally tamed. When they set foot on this isolated island, they found that things were not that simple.\\nVolcanoes are restless and ready to erupt, while ugly conspiracies are quietly brewing amidst chaos.\"}]}";
        JSONObject result = JSON.parseObject(str);
        assertNotNull(result);
        assertEquals(2, result.getJSONArray("Test").size());
    }

    @Test
    public void testVariousLengths() {
        for (int len = 1; len <= 600; len++) {
            JSONObject result = JSON.parseObject(payload(len));
            assertNotNull(result);
            assertEquals(expectedMessage(len), messageOf(result));
        }
    }

    /**
     * Same payloads as {@link #testVariousLengths()}, but parsed from UTF-8 bytes so the
     * escape handling in JSONReaderUTF8.readString is exercised as well — that reader got
     * the same strBuf growth fix and is not reached through the String entry point, which
     * routes multi-byte input to JSONReaderUTF16.
     */
    @Test
    public void testVariousLengthsUTF8() {
        for (int len = 1; len <= 600; len++) {
            assertMessageRoundTripUTF8(len);
        }
        // strBuf is pooled process-wide, so a preceding test may leave a buffer that already
        // covers the lengths above; this one is past any capacity another test can have grown it to.
        assertMessageRoundTripUTF8(100_000);
    }

    /**
     * Covers the initial {@code strBuf} allocation, {@code new char[stroff + 512]}: the escape is
     * preceded by more than 512 characters that have already been scanned into {@code stroff}, so
     * a plain {@code new char[512]} is too small for the pre-copy into the fresh buffer.
     *
     * <p>Unlike {@link #testVariousLengths()}, the escape must be in the <em>first</em> string
     * value of the document — once {@code strBuf} has been allocated, later strings take the
     * growth path instead. JSONReaderUTF16 keeps strBuf per reader instance, so every parse
     * starts from null; the leading multi-byte character is what routes the String entry point
     * to that reader rather than to the ASCII/UTF-8 one.
     */
    @Test
    public void testLongPrefixBeforeFirstEscape() {
        for (int len : new int[]{500, 511, 512, 513, 600, 4096}) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"message\":\"中");
            repeat(sb, 'a', len);
            sb.append("\\n\"}");

            String expected = repeat(new StringBuilder(len + 2).append('中'), 'a', len)
                    .append('\n')
                    .toString();
            assertEquals(expected, JSON.parseObject(sb.toString()).getString("message"));
        }
    }

    /**
     * Round-trip sanity check for the JSONB reader. It does <em>not</em> cover the strBuf fix:
     * JSONReaderJSONB has no strBuf and reads string values by length prefix rather than by
     * scanning for escapes, so it cannot reach the code path issue #7685 is about.
     */
    @Test
    public void testVariousLengthsJSONB() {
        for (int len = 1; len <= 600; len++) {
            String message = "中\n" + expectedMessage(len);

            JSONObject object = new JSONObject().fluentPut("message", message);
            JSONObject parsed = (JSONObject) JSONB.parse(JSONB.toBytes(object));
            assertNotNull(parsed);
            assertEquals(message, parsed.getString("message"));
        }
    }

    private void assertMessageRoundTripUTF8(int len) {
        byte[] utf8 = payload(len).getBytes(StandardCharsets.UTF_8);
        JSONObject result = JSON.parseObject(utf8);
        assertNotNull(result);
        assertEquals(expectedMessage(len), messageOf(result));
    }

    /**
     * {@code {"Test":[{"message":"中\n"},{"message":"a*len \n b*len"}]}} — the leading multi-byte
     * value forces the String entry point onto JSONReaderUTF16, and the second value is the one
     * that has to grow strBuf.
     */
    private static String payload(int len) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"Test\":[{\"message\":\"中\\n\"},{\"message\":\"");
        repeat(sb, 'a', len);
        sb.append("\\n");
        repeat(sb, 'b', len);
        sb.append("\"}]}");
        return sb.toString();
    }

    private static String expectedMessage(int len) {
        StringBuilder sb = new StringBuilder(len * 2 + 1);
        repeat(sb, 'a', len);
        sb.append('\n');
        repeat(sb, 'b', len);
        return sb.toString();
    }

    private static String messageOf(JSONObject result) {
        return result.getJSONArray("Test")
                .getJSONObject(1)
                .getString("message");
    }

    private static StringBuilder repeat(StringBuilder sb, char c, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb;
    }
}
