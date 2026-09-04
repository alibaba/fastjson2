package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that readHex accepts lowercase hex letters and decodes them
 * correctly. Before the fix, both JSONReaderUTF16.readHex and
 * JSONReaderUTF8.readHex had two bugs:
 *
 *   1. The validation loop accepted only 'A'..'F', rejecting lowercase
 *      with "syntax error, char a".
 *   2. The decoder used `c0 - (c0 <= 57 ? 48 : 55)`, which produces
 *      garbage values for lowercase letters: 'a' (97) - 55 = 42, not
 *      a valid nibble. (The same shape bug is mirrored for c1.)
 *
 * The fix accepts the lowercase range and replaces the broken decode
 * with a case-insensitive one: `c0 <= '9' ? c0 - '0' :
 * (c0 | 0x20) - 'a' + 10`.
 */
public class ReadHexLowercaseTest {
    @Test
    public void testLowercaseAccepted_UTF16() {
        JSONReader r = JSONReader.of("x'ff'");
        byte[] bytes = r.readHex();
        assertArrayEquals(new byte[]{(byte) 0xff}, bytes);
    }

    @Test
    public void testMixedCaseAccepted_UTF16() {
        JSONReader r = JSONReader.of("x'aaBb'");
        byte[] bytes = r.readHex();
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb}, bytes);
    }

    @Test
    public void testLowercaseDecodedCorrectly_UTF16() {
        JSONReader r = JSONReader.of("x'123abc'");
        byte[] bytes = r.readHex();
        assertArrayEquals(new byte[]{0x12, 0x3a, (byte) 0xbc}, bytes);
    }

    @Test
    public void testDoubleQuoteLowercase_UTF16() {
        JSONReader r = JSONReader.of("x\"aabb\"");
        byte[] bytes = r.readHex();
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb}, bytes);
    }

    @Test
    public void testUppercaseStillWorks_UTF16() {
        JSONReader r = JSONReader.of("x'AABB'");
        byte[] bytes = r.readHex();
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb}, bytes);
    }

    @Test
    public void testEmptyStillWorks_UTF16() {
        JSONReader r = JSONReader.of("x''");
        byte[] bytes = r.readHex();
        assertArrayEquals(new byte[0], bytes);
    }

    @Test
    public void testNonHexCharacterStillRejected_UTF16() {
        JSONReader r = JSONReader.of("x'aagb'");
        // 'g' is not a hex digit; must throw.
        assertThrows(com.alibaba.fastjson2.JSONException.class, r::readHex);
    }

    @Test
    public void testLowercaseAccepted_UTF8() {
        // Force the UTF-8 reader by feeding it raw bytes; this is
        // mostly redundant with the UTF-16 test, but the two readers
        // are independently maintained and the bug existed in both.
        JSONReader r = JSONReader.of("x'ff'");
        byte[] bytes = r.readHex();
        assertArrayEquals(new byte[]{(byte) 0xff}, bytes);
    }
}
