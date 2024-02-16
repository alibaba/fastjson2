package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONReaderJSONBTest2 {
    @Test
    public void name3() {
        String[] names = new String[]{
                "uri", "¹²³"
        };
        for (String name : names) {
            assertEquals(3, name.length());
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
        }
    }

    @Test
    public void name4() {
        String[] names = new String[]{
                "size", "¹²³©"
        };
        for (String name : names) {
            assertEquals(4, name.length());
            byte[] latin1 = name.getBytes(StandardCharsets.ISO_8859_1);
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            byte name1 = latin1[3];
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match4(name1));
        }
    }

    @Test
    public void name5() {
        String[] names = new String[]{
                "title", "width", "¹²³©®"
        };
        for (String name : names) {
            assertEquals(5, name.length());
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            int name1 = UNSAFE.getShort(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 5);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match5(name1));
        }
    }

    @Test
    public void name6() {
        String[] names = new String[]{
                "player", "height", "images", "format", "¹²³©®½"
        };
        for (String name : names) {
            assertEquals(6, name.length());
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            byte[] latin1 = name.getBytes(StandardCharsets.ISO_8859_1);
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            int name1 = UNSAFE.getShort(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 5) & 0xFFFF | (latin1[5] << 16);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match6(name1));
        }
    }

    @Test
    public void name7() {
        String[] names = new String[]{
                "bitrate", "persons", "¹²³©®½¼"
        };
        for (String name : names) {
            assertEquals(7, name.length());
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            int name1 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 5);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match7(name1));
        }
    }

    @Test
    public void name8() {
        String[] names = new String[]{
                "duration", "a2345678", "¹²³©®½¼¾"
        };
        for (String name : names) {
            assertEquals(8, name.length());
            byte[] latin1 = name.getBytes(StandardCharsets.ISO_8859_1);
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            int name1 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 5);
            byte name2 = latin1[7];
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match8(name1, name2));
        }
    }

    @Test
    public void name9() {
        String[] names = new String[]{
                "a23456789", "¹²³©®½¼¾£"
        };
        for (String name : names) {
            assertEquals(9, name.length());
            byte[] latin1 = name.getBytes(StandardCharsets.ISO_8859_1);
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 5) & 0xFFFFFFFFL | (long) UNSAFE.getShort(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 9) << 32;
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match9(name1), name);
        }
    }

    @Test
    public void name10() {
        String[] names = new String[]{
                "a234567890", "¹²³©®½¼¾£¢"
        };
        for (String name : names) {
            assertEquals(10, name.length());
            byte[] latin1 = name.getBytes(StandardCharsets.ISO_8859_1);
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 5) & 0xFFFFFFFFL
                    | ((UNSAFE.getShort(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 9) & 0xFFFFL) << 32)
                    | ((long) latin1[9] << 48);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match10(name1), name);
        }
    }

    @Test
    public void name11() {
        String[] names = new String[]{
                "a2345678901", "¹²³©®½¼¾£¢¹"
        };
        for (String name : names) {
            assertEquals(11, name.length());
            byte[] latin1 = name.getBytes(StandardCharsets.ISO_8859_1);
            byte[] jsonbBytes = JSONObject.of(name, 1).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + 5);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match11(name1));
        }
    }
}
