package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A JSONB string frame carries its length on the wire. When the length exceeds the bytes that
 * remain in the frame, JSONReaderJSONB used to copy that many bytes straight out of the backing
 * array, so with the slice API (offset/length shorter than the array) the extra bytes came from
 * whatever followed the frame - disclosing neighboring data. These tests pin the bound.
 */
public class JSONBStringLengthTest {
    // type + one-byte length + two content bytes belong to the frame; everything after is "neighbor"
    private static byte[] frame(byte strType) {
        byte[] buf = new byte[64];
        buf[0] = strType;
        buf[1] = 40; // declared length, far past the 2 content bytes left in the frame
        Arrays.fill(buf, 2, buf.length, (byte) 'S'); // stand-in for adjacent buffer data
        return buf;
    }

    private static final int FRAME_LEN = 4;

    @Test
    public void asciiOverLength() {
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(frame(BC_STR_ASCII), 0, FRAME_LEN, String.class));
    }

    @Test
    public void utf8OverLength() {
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(frame(BC_STR_UTF8), 0, FRAME_LEN, String.class));
    }

    @Test
    public void utf16OverLength() {
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(frame(BC_STR_UTF16), 0, FRAME_LEN, String.class));
    }

    @Test
    public void utf16LEOverLength() {
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(frame(BC_STR_UTF16LE), 0, FRAME_LEN, String.class));
    }

    @Test
    public void utf16BEOverLength() {
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(frame(BC_STR_UTF16BE), 0, FRAME_LEN, String.class));
    }

    @Test
    public void gb18030OverLength() {
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(frame(BC_STR_GB18030), 0, FRAME_LEN, String.class));
    }

    @Test
    public void fieldNameOverLength() {
        // BC_OBJECT, then an over-length ASCII field name, then a neighbor region
        byte[] buf = new byte[64];
        buf[0] = BC_OBJECT;
        buf[1] = BC_STR_ASCII;
        buf[2] = 40;
        Arrays.fill(buf, 3, buf.length, (byte) 'S');
        assertThrows(JSONException.class, () -> JSONB.parseObject(buf, 0, 5, JSONObject.class));
    }

    @Test
    public void getStringOverLength() {
        // readValueHashCode records the wire length; getString must not copy past the frame end
        byte[] buf = new byte[64];
        buf[0] = BC_STR_UTF16;
        buf[1] = 40;
        Arrays.fill(buf, 2, buf.length, (byte) 'S');
        JSONReader reader = JSONReader.ofJSONB(buf, 0, FRAME_LEN);
        reader.readValueHashCode();
        assertThrows(JSONException.class, reader::getString);
    }

    @Test
    public void exactLengthSliceParses() {
        // a frame whose declared length exactly fits still parses through the slice API
        byte[] buf = new byte[64];
        buf[0] = BC_STR_ASCII;
        buf[1] = 5;
        byte[] hello = "hello".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(hello, 0, buf, 2, hello.length);
        assertEquals("hello", JSONB.parseObject(buf, 0, 7, String.class));
    }

    @Test
    public void validStringsRoundTrip() {
        String[] strings = {
                "",
                "hello",
                "中文字符串",
                "发展特色富民产业，扎扎实实把乡村振兴战略实施好"
        };
        for (String s : strings) {
            assertEquals(s, JSONB.parseObject(JSONB.toBytes(s), String.class));
            assertEquals(s, JSONB.parseObject(JSONB.toBytes(s, StandardCharsets.UTF_16), String.class));
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 100_000; i++) {
            buf.append((char) ('a' + (i % 26)));
        }
        String large = buf.toString();
        assertEquals(large, JSONB.parseObject(JSONB.toBytes(large), String.class));
    }
}
