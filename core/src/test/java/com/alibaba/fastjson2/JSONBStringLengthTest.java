package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A JSONB string frame carries its length on the wire. When the length exceeds the bytes that
 * remain in the frame, JSONReaderJSONB used to copy that many bytes straight out of the backing
 * array, so with the slice API (offset/length shorter than the array) the extra bytes came from
 * whatever followed the frame - disclosing neighboring data.
 *
 * <p>Every case asserts the frame-bound message rather than just the exception type, because a
 * duplicate check further downstream also throws JSONException and would satisfy a bare
 * assertThrows. The cases are grouped by the guard they reach: typed value reads, untyped
 * readAny(), the field-name readers in both their wire-length and length-in-type-byte forms,
 * and getString(). Negative wire lengths are pinned separately, at the sites that copy without
 * resolving a symbol reference first.
 */
public class JSONBStringLengthTest {
    // type + one-byte length + two content bytes belong to the frame; everything after is "neighbor"
    private static final int FRAME_LEN = 4;
    private static final String OVER_LENGTH = "string length out of range: 40, available: 2";
    private static final String NEGATIVE_LENGTH = "string length out of range: -16, available: 2";

    private static final byte[] STRING_TYPES = {
            BC_STR_ASCII, BC_STR_UTF8, BC_STR_UTF16, BC_STR_UTF16LE, BC_STR_UTF16BE, BC_STR_GB18030
    };

    /**
     * Types whose length is copied straight out of the backing array with no symbol reference
     * resolved ahead of it, so a negative wire length has to be rejected where it is read.
     * BC_STR_ASCII is absent on purpose: a negative length there is a symbol ordinal.
     */
    private static final byte[] DIRECT_COPY_TYPES = {
            BC_STR_UTF8, BC_STR_UTF16, BC_STR_UTF16LE, BC_STR_UTF16BE, BC_STR_GB18030
    };

    private static byte[] frame(byte strType) {
        return frame(strType, (byte) 40); // declared length, far past the 2 content bytes left in the frame
    }

    private static byte[] frame(byte strType, byte len) {
        byte[] buf = new byte[64];
        buf[0] = strType;
        buf[1] = len;
        Arrays.fill(buf, 2, buf.length, (byte) 'S'); // stand-in for adjacent buffer data
        return buf;
    }

    private static JSONReaderJSONB readerOf(byte[] buf, int frameLen) {
        return (JSONReaderJSONB) JSONReader.ofJSONB(buf, 0, frameLen);
    }

    private static void assertMessage(String expected, Executable call) {
        JSONException ex = assertThrows(JSONException.class, call);
        assertEquals(expected, ex.getMessage());
    }

    @Test
    public void asciiOverLength() {
        assertMessage(OVER_LENGTH, () -> JSONB.parseObject(frame(BC_STR_ASCII), 0, FRAME_LEN, String.class));
    }

    @Test
    public void utf8OverLength() {
        assertMessage(OVER_LENGTH, () -> JSONB.parseObject(frame(BC_STR_UTF8), 0, FRAME_LEN, String.class));
    }

    @Test
    public void utf16OverLength() {
        assertMessage(OVER_LENGTH, () -> JSONB.parseObject(frame(BC_STR_UTF16), 0, FRAME_LEN, String.class));
    }

    @Test
    public void utf16LEOverLength() {
        assertMessage(OVER_LENGTH, () -> JSONB.parseObject(frame(BC_STR_UTF16LE), 0, FRAME_LEN, String.class));
    }

    @Test
    public void utf16BEOverLength() {
        assertMessage(OVER_LENGTH, () -> JSONB.parseObject(frame(BC_STR_UTF16BE), 0, FRAME_LEN, String.class));
    }

    @Test
    public void gb18030OverLength() {
        assertMessage(OVER_LENGTH, () -> JSONB.parseObject(frame(BC_STR_GB18030), 0, FRAME_LEN, String.class));
    }

    /**
     * ObjectReaderImplObject diverts the string types to readString(), so readAny()'s own string
     * cases are only reachable through an untyped read.
     */
    @Test
    public void readAnyOverLength() {
        for (byte strType : STRING_TYPES) {
            assertMessage(OVER_LENGTH, () -> readerOf(frame(strType), FRAME_LEN).readAny());
        }
    }

    @Test
    public void fieldNameOverLength() {
        // BC_OBJECT, then an over-length ASCII field name, then a neighbor region
        byte[] buf = new byte[64];
        buf[0] = BC_OBJECT;
        buf[1] = BC_STR_ASCII;
        buf[2] = 40;
        Arrays.fill(buf, 3, buf.length, (byte) 'S');
        assertMessage(OVER_LENGTH, () -> JSONB.parseObject(buf, 0, 5, JSONObject.class));
    }

    @Test
    public void fieldNameOverLengthPerType() {
        // readFieldName reads the type byte itself, so the name type can start the frame
        for (byte strType : STRING_TYPES) {
            assertMessage(OVER_LENGTH, () -> readerOf(frame(strType), FRAME_LEN).readFieldName());
        }
    }

    /**
     * JSONBWriter emits the fixed-length form for every key up to STR_ASCII_FIX_LEN, so this is the
     * common encoding for object keys; its length lives in the type byte rather than on the wire.
     */
    @Test
    public void fieldNameFixOverLength() {
        byte[] buf = new byte[64];
        buf[0] = BC_OBJECT;
        buf[1] = (byte) (BC_STR_ASCII_FIX_MIN + 8);
        Arrays.fill(buf, 2, buf.length, (byte) 'S');
        assertMessage("string length out of range: 8, available: 2",
                () -> JSONB.parseObject(buf, 0, FRAME_LEN, JSONObject.class));
    }

    @Test
    public void fieldNameFixFastPathOverLength() {
        // the one- and two-character forms have their own fast paths and index bytes[offset] directly
        for (int strlen = 1; strlen <= 2; strlen++) {
            byte[] buf = new byte[64];
            buf[0] = BC_OBJECT;
            buf[1] = (byte) (BC_STR_ASCII_FIX_MIN + strlen);
            Arrays.fill(buf, 2, buf.length, (byte) 'S');
            assertMessage("string length out of range: " + strlen + ", available: 0",
                    () -> JSONB.parseObject(buf, 0, 2, JSONObject.class));
        }
    }

    @Test
    public void getStringOverLength() {
        // readValueHashCode records the wire length; getString must not copy past the frame end
        assertMessage(OVER_LENGTH, () -> {
            JSONReader reader = readerOf(frame(BC_STR_UTF16), FRAME_LEN);
            reader.readValueHashCode();
            reader.getString();
        });
    }

    /**
     * A negative wire length is a symbol ordinal only where the caller resolves one. At the sites
     * that copy directly it used to reach the copy and raise NegativeArraySizeException,
     * StringIndexOutOfBoundsException or NullPointerException, none of which a caller filtering
     * malformed JSONB by JSONException would catch.
     */
    @Test
    public void readAnyNegativeLengthRejected() {
        for (byte strType : DIRECT_COPY_TYPES) {
            assertMessage(NEGATIVE_LENGTH,
                    () -> readerOf(frame(strType, BC_INT32_NUM_MIN), FRAME_LEN).readAny());
        }
    }

    @Test
    public void valueReadNegativeLengthRejected() {
        // BC_STR_UTF16 is excluded: readString(Charset) resolves a negative length as a symbol
        for (byte strType : new byte[]{BC_STR_UTF8, BC_STR_UTF16LE, BC_STR_UTF16BE, BC_STR_GB18030}) {
            assertMessage(NEGATIVE_LENGTH,
                    () -> JSONB.parseObject(frame(strType, BC_INT32_NUM_MIN), 0, FRAME_LEN, String.class));
        }
    }

    @Test
    public void negativeLengthDoesNotRewindCursor() {
        // readStringUTF8 used to return "" and then apply offset += strlen, leaving the cursor
        // behind where it started
        JSONReaderJSONB reader = readerOf(frame(BC_STR_UTF8, BC_INT32_NUM_MIN), FRAME_LEN);
        assertMessage(NEGATIVE_LENGTH, reader::readString);
        assertEquals(2, reader.offset);
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
    public void exactLengthFieldNamesParse() {
        // names of 1, 2 and 8 characters cover both fixed-length fast paths and the general arm
        JSONObject object = new JSONObject();
        object.put("a", 1);
        object.put("ab", 2);
        object.put("abcdefgh", 3);

        byte[] jsonb = JSONB.toBytes(object);
        byte[] buf = Arrays.copyOf(jsonb, jsonb.length + 32);
        Arrays.fill(buf, jsonb.length, buf.length, (byte) 'S');
        assertEquals(object, JSONB.parseObject(buf, 0, jsonb.length, JSONObject.class));
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
