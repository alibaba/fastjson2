package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_BYTE_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;
import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderJSONBTest2 {
    char[] ascii = "a234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890".toCharArray();
    byte[] latin = "¹²³©®½¼¾£¢¹²³©®½¼¾£¢¹²³©®½¼¾£¢¹²³©®½¼¾£¢¹²³©®½¼¾£¢¹²³©®½¼¾£¢¹²³©®½¼¾£¢¹²³©®½¼¾£¢¹²³©®½¼¾£¢¹²³©®½¼¾£¢".getBytes(StandardCharsets.ISO_8859_1);
    final int value = 1;

    @Test
    public void name3() {
        String[] names = new String[]{
                "uri", "¹²³"
        };
        for (String name : names) {
            assertEquals(3, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match3());
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
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
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            byte name1 = latin1[3];
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match4(name1));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name5() {
        String[] names = new String[]{
                "title", "width", "¹²³©®"
        };
        for (String name : names) {
            assertEquals(5, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            int name1 = UNSAFE.getShort(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match5(name1));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name6() {
        String[] names = new String[]{
                "player", "height", "images", "format", "¹²³©®½"
        };
        for (String name : names) {
            assertEquals(6, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            int name1 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5) & 0xFFFFFF;
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match6(name1));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name7() {
        String[] names = new String[]{
                "bitrate", "persons", "¹²³©®½¼"
        };
        for (String name : names) {
            assertEquals(7, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            int name1 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match7(name1));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
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
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            int name1 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            byte name2 = latin1[7];
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match8(name1, name2));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name9() {
        String[] names = new String[]{
                "a23456789", "¹²³©®½¼¾£"
        };
        for (String name : names) {
            assertEquals(9, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5) & 0xFFFFFFFFFFFFL;
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match9(name1), name);
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name10() {
        String[] names = new String[]{
                "a234567890", "¹²³©®½¼¾£¢"
        };
        for (String name : names) {
            assertEquals(10, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5) & 0xFFFFFFFFFFFFFFL;
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match10(name1), name);
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name11() {
        String[] names = new String[]{
                "a2345678901", "¹²³©®½¼¾£¢¹"
        };
        for (String name : names) {
            assertEquals(11, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match11(name1));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name12() {
        int nameLength = 12;
        String[] names = new String[]{
                new String(ascii, 0, nameLength),
                new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
        };
        for (String name : names) {
            assertEquals(nameLength, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            byte[] latin1 = name.getBytes(StandardCharsets.ISO_8859_1);
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match12(name1, latin1[nameLength - 1]));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name13() {
        int nameLength = 13;
        String[] names = new String[]{
                new String(ascii, 0, nameLength),
                new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
        };
        for (String name : names) {
            assertEquals(nameLength, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            int name2 = UNSAFE.getShort(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 13);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match13(name1, name2));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name14() {
        int nameLength = 14;
        String[] names = new String[]{
                new String(ascii, 0, nameLength),
                new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
        };
        for (String name : names) {
            assertEquals(nameLength, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            int name2 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 13) & 0xFFFFFF;
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match14(name1, name2));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name15() {
        int nameLength = 15;
        String[] names = new String[]{
                new String(ascii, 0, nameLength),
                new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
        };
        for (String name : names) {
            assertEquals(nameLength, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            int name2 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 13);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match15(name1, name2));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name16() {
        int nameLength = 16;
        String[] names = new String[]{
                new String(ascii, 0, nameLength),
                new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
        };
        for (String name : names) {
            assertEquals(nameLength, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            int name2 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 13);
            byte name3 = jsonbBytes[17];
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match16(name1, name2, name3));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name17() {
        int nameLength = 17;
        String[] names = new String[]{
                new String(ascii, 0, nameLength),
                new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
        };
        for (String name : names) {
            assertEquals(nameLength, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            long name2 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 13) & 0xFFFF_FFFF_FFFFL;
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match17(name1, name2));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name18() {
        int nameLength = 18;
        String[] names = new String[]{
                new String(ascii, 0, nameLength),
                new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
        };
        for (String name : names) {
            assertEquals(nameLength, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            long name2 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 13) & 0xFFFF_FFFF_FFFF_FFL;
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match18(name1, name2));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void name19() {
        int nameLength = 19;
        String[] names = new String[]{
                new String(ascii, 0, nameLength),
                new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
        };
        for (String name : names) {
            assertEquals(nameLength, name.length());
            byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
            int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
            long name1 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 5);
            long name2 = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 13);
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(name0, jsonReader.getRawInt());
            assertTrue(jsonReader.nextIfName4Match19(name1, name2));
            assertEquals(value, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
        }
    }

    @Test
    public void nameX() throws Exception {
        for (int nameLength = 3; nameLength < 44; nameLength++) {
            String[] names = new String[]{
                    new String(ascii, 0, nameLength),
                    new String(latin, 0, nameLength, StandardCharsets.ISO_8859_1)
            };

            for (String name : names) {
                assertEquals(nameLength, name.length());
                byte[] jsonbBytes = JSONObject.of(name, value).toJSONBBytes();
                int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);
                JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
                assertTrue(jsonReader.nextIfObjectStart());
                assertEquals(name0, jsonReader.getRawInt());

                Method method = null;
                String methodName = "nextIfName4Match" + nameLength;
                for (Method m : JSONReader.class.getMethods()) {
                    if (m.getName().equals(methodName)) {
                        method = m;
                        break;
                    }
                }
                assertNotNull(method);

                Object[] args = new Object[method.getParameterCount()];
                Class<?>[] parameterTypes = method.getParameterTypes();
                int byteIndex = 3;
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> paramType = parameterTypes[i];
                    if (paramType == byte.class) {
                        args[i] = jsonbBytes[byteIndex + 2];
                        byteIndex++;
                    } else if (paramType == int.class) {
                        int arg;
                        if (nameLength - byteIndex == 2) {
                            arg = UNSAFE.getShort(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + byteIndex + 2);
                            byteIndex += 2;
                        } else if (nameLength - byteIndex == 3) {
                            arg = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + byteIndex + 2) & 0xFFFFFF;
                            byteIndex += 3;
                        } else {
                            arg = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + byteIndex + 2);
                            byteIndex += 4;
                        }
                        args[i] = arg;
                    } else if (paramType == long.class) {
                        long arg;
                        if (nameLength - byteIndex == 6) {
                            arg = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + byteIndex + 2) & 0xFFFF_FFFF_FFFFL;
                            byteIndex += 6;
                        } else if (nameLength - byteIndex == 7) {
                            arg = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + byteIndex + 2) & 0xFF_FFFF_FFFF_FFFFL;
                            byteIndex += 7;
                        } else {
                            arg = UNSAFE.getLong(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + byteIndex + 2);
                            byteIndex += 8;
                        }
                        args[i] = arg;
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }

                assertEquals(Boolean.TRUE, method.invoke(jsonReader, args), methodName);

                assertEquals(value, jsonReader.readInt32Value());
                assertTrue(jsonReader.nextIfObjectEnd());
            }
        }
    }

    @Test
    public void readLength() {
        StringBuilder buf = new StringBuilder(1024 * 1024);
        int[] lengths = new int[10];
        for (int i = 0, x = 1; i < lengths.length; i++, x *= 4) {
            lengths[i] = x;
        }
        for (int length : lengths) {
            for (int i = 0; i < length; i++) {
                buf.append('0');
            }
            String str = buf.toString();
            byte[] jsonBytes = JSONB.toBytes(str);
            JSONReaderJSONB jsonReader = (JSONReaderJSONB) JSONReader.ofJSONB(jsonBytes);
            if (jsonReader.nextIfMatch(JSONB.Constants.BC_STR_ASCII)) {
                assertEquals(
                        str.length(),
                        jsonReader.readLength());
            }
            assertFalse(jsonReader.isArray());
        }

        for (int length : lengths) {
            int[] array = new int[length];
            byte[] jsonBytes = JSONB.toBytes(array);
            JSONReaderJSONB jsonReader = (JSONReaderJSONB) JSONReader.ofJSONB(jsonBytes);
            if (jsonReader.nextIfMatch(JSONB.Constants.BC_ARRAY)) {
                assertEquals(
                        length,
                        jsonReader.readLength());
            }
        }

        {
            byte[] jsonBytes = JSONB.toBytes(1024 * 1024 * 512);
            JSONReaderJSONB jsonReader = (JSONReaderJSONB) JSONReader.ofJSONB(jsonBytes);
            assertThrows(JSONException.class, () -> jsonReader.readLength());
        }
        {
            byte[] jsonBytes = JSONB.toBytes("xxx");
            JSONReaderJSONB jsonReader = (JSONReaderJSONB) JSONReader.ofJSONB(jsonBytes);
            assertThrows(JSONException.class, () -> jsonReader.readLength());
        }
    }
}
