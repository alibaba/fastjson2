package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONWriterJSONBTest {
    @Test
    public void test_startObject() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.startObject();
            jsonWriter.endObject();
        }
    }

    @Test
    public void test_startArray() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.startArray(1);
        }
    }

    @Test
    public void test_writeRaw() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeRaw(JSONB.Constants.BC_NULL);
        }
    }

    @Test
    public void test_writeRaw_1() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeRaw(new byte[]{JSONB.Constants.BC_NULL});
        }
    }

    @Test
    public void test_writeMillis() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeMillis(1);
        }
    }

    @Test
    public void notSupported() {
        JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
        assertThrows(JSONException.class, () -> jsonWriter.startArray());
        assertThrows(JSONException.class, () -> jsonWriter.writeRaw(""));
        assertThrows(JSONException.class, () -> jsonWriter.writeComma());
        assertThrows(JSONException.class, () -> jsonWriter.write0('A'));
        assertThrows(JSONException.class, () -> jsonWriter.writeDateTimeISO8601(2001, 1, 1, 12, 13, 14, 0, 0));
        assertThrows(JSONException.class, () -> jsonWriter.writeDateYYYMMDD10(2001, 1, 1));
        assertThrows(JSONException.class, () -> jsonWriter.writeTimeHHMMSS8(12, 13, 14));
        assertThrows(JSONException.class, () -> jsonWriter.writeBase64(new byte[0]));
        assertThrows(JSONException.class, () -> jsonWriter.writeRaw('A'));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new byte[0]));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new byte[0], 0, 0));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new char[0]));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new char[0], 0, 0));
        assertThrows(JSONException.class, () -> jsonWriter.writeColon());
        assertThrows(JSONException.class, () -> jsonWriter.flushTo(null, null));
    }

    @Test
    public void writeDateTime19() {
        JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
        jsonWriter.writeDateTime19(2013, 5, 6, 12, 13, 14);
        assertEquals("\"2013-05-06 12:13:14\"", JSONB.toJSONString(jsonWriter.getBytes()));
    }

    @Test
    public void writeString() {
        JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
        jsonWriter.writeString('A');
        assertEquals("\"A\"", JSONB.toJSONString(jsonWriter.getBytes()));
    }

    @Test
    public void startArray() {
        Integer[] array = new Integer[]{1, 2, 3};
        JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
        jsonWriter.startArray(array, array.length);
        for (Integer item : array) {
            jsonWriter.writeInt32(item);
        }
        assertEquals("[\n" +
                "\t1,\n" +
                "\t2,\n" +
                "\t3\n" +
                "]", JSONB.toJSONString(jsonWriter.getBytes()));
    }
}
