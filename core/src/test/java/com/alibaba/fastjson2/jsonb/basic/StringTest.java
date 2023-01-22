package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.util.JDKUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringTest {
    /**
     * 0x49
     */
    @Test
    public void testEmpty() {
        byte[] bytes = JSONB.toBytes("");
        assertEquals(1, bytes.length);
        assertEquals(BC_STR_ASCII_FIX_0, bytes[0]);
    }

    /**
     * 0x49 ~ 0x78
     */
    @Test
    public void testFix_ascii_0_47() {
        for (int i = 0; i <= 47; i++) {
            char[] chars = new char[i];
            Arrays.fill(chars, 'A');

            String str = new String(chars);
            byte[] bytes = JSONB.toBytes(str);
            assertEquals(1 + i, bytes.length);
            assertEquals(BC_STR_ASCII_FIX_0 + i, bytes[0]);

            String parsed = (String) JSONB.parse(bytes);
            assertEquals(str, parsed);
        }
    }

    /**
     * 0x79
     */
    @Test
    public void testFix_ascii_48() {
        for (int i = 48; i < 128; i++) {
            char[] chars = new char[i];
            Arrays.fill(chars, 'A');

            String str = new String(chars);
            byte[] bytes = JSONB.toBytes(str);
            assertEquals(3 + i, bytes.length);
            assertEquals(BC_STR_ASCII, bytes[0]);

            String parsed = (String) JSONB.parse(bytes);
            assertEquals(str, parsed);
        }
    }

    /**
     * 0x7a
     */
    @Test
    public void testFix_utf8_48() {
        for (int i = 48; i < 128; i++) {
            char[] chars = new char[i];
            Arrays.fill(chars, 'A');
            chars[0] = '中';

            String str = new String(chars);
            byte[] bytes = JSONB.toBytes(str);
            assertEquals(5 + i, bytes.length);
            assertEquals(BC_STR_UTF8, bytes[0]);

            String parsed = (String) JSONB.parse(bytes);
            assertEquals(str, parsed);
        }
    }

    /**
     * 0x7b
     */
    @Test
    public void testFix_utf16_48() {
        for (int i = 48; i < 128; i++) {
            char[] chars = new char[i];
            Arrays.fill(chars, '中');

            String str = new String(chars);
            byte[] bytes = JSONB.toBytes(str);
            byte type = bytes[0];
            if (JDKUtils.JVM_VERSION == 8) {
                assertEquals(BC_STR_UTF8, type);
            } else {
                if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                    assertEquals(BC_STR_UTF16LE, type);
                }
            }

            String parsed = (String) JSONB.parse(bytes);
            assertEquals(str, parsed);
        }
    }
}
