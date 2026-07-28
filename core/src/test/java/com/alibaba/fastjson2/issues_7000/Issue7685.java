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
            StringBuilder sb = new StringBuilder();
            sb.append("{\"Test\":[{\"message\":\"中\\n\"},{\"message\":\"");
            for (int i = 0; i < len; i++) {
                sb.append('a');
            }
            sb.append("\\n");
            for (int i = 0; i < len; i++) {
                sb.append('b');
            }
            sb.append("\"}]}");

            JSONObject result = JSON.parseObject(sb.toString());
            assertNotNull(result);
            String message = result.getJSONArray("Test")
                    .getJSONObject(1)
                    .getString("message");
            assertEquals(len * 2 + 1, message.length());
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

    private void assertMessageRoundTripUTF8(int len) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"Test\":[{\"message\":\"中\\n\"},{\"message\":\"");
        for (int i = 0; i < len; i++) {
            sb.append('a');
        }
        sb.append("\\n");
        for (int i = 0; i < len; i++) {
            sb.append('b');
        }
        sb.append("\"}]}");

        byte[] utf8 = sb.toString().getBytes(StandardCharsets.UTF_8);
        JSONObject result = JSON.parseObject(utf8);
        assertNotNull(result);
        String message = result.getJSONArray("Test")
                .getJSONObject(1)
                .getString("message");
        assertEquals(len * 2 + 1, message.length());
    }

    @Test
    public void testVariousLengthsJSONB() {
        for (int len = 1; len <= 600; len++) {
            StringBuilder sb = new StringBuilder();
            sb.append("中\n");
            for (int i = 0; i < len; i++) {
                sb.append('a');
            }
            sb.append('\n');
            for (int i = 0; i < len; i++) {
                sb.append('b');
            }
            String message = sb.toString();

            JSONObject object = new JSONObject().fluentPut("message", message);
            JSONObject parsed = (JSONObject) JSONB.parse(JSONB.toBytes(object));
            assertNotNull(parsed);
            assertEquals(message, parsed.getString("message"));
        }
    }
}
