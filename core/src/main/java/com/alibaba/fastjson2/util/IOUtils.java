package com.alibaba.fastjson2.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;

public class IOUtils {
    static final byte[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    static final byte[] DigitTens = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1',
            '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3',
            '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5',
            '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'};

    static final byte[] DigitOnes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    public static final int[] DIGITS_K = new int[1000];
    private static final byte[] MIN_INT_BYTES = "-2147483648".getBytes();
    private static final char[] MIN_INT_CHARS = "-2147483648".toCharArray();
    private static final byte[] MIN_LONG = "-9223372036854775808".getBytes();

    static {
        for (int i = 0; i < DIGITS_K.length; i++) {
            DIGITS_K[i] = (i < 10 ? (2 << 24) : i < 100 ? (1 << 24) : 0)
                    + (((i / 100) + '0') << 16)
                    + ((((i / 10) % 10) + '0') << 8)
                    + i % 10 + '0';
        }
    }

    public static int stringSize(int x) {
        for (int i = 0; ; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    public static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p = 10 * p;
        }
        return 19;
    }

    public static void getChars(int i, int index, byte[] buf) {
        int q, r, p = index;
        byte sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--p] = DigitOnes[r];
            buf[--p] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        do {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--p] = DIGITS[r];
            i = q;
        } while (i != 0);
        if (sign != 0) {
            buf[--p] = sign;
        }
    }

    public static void getChars(int i, int index, char[] buf) {
        int q, r, p = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--p] = (char) DigitOnes[r];
            buf[--p] = (char) DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        do {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--p] = (char) DIGITS[r];
            i = q;
        } while (i != 0);
        if (sign != 0) {
            buf[--p] = sign;
        }
    }

    public static void getChars(long i, int index, byte[] buf) {
        long q;
        int r;
        int charPos = index;
        byte sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        do {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buf[--charPos] = DIGITS[r];
            i2 = q2;
        } while (i2 != 0);
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    public static void getChars(long i, int index, char[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = (char) DigitOnes[r];
            buf[--charPos] = (char) DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = (char) DigitOnes[r];
            buf[--charPos] = (char) DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        do {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buf[--charPos] = (char) DIGITS[r];
            i2 = q2;
        } while (i2 != 0);
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    public static int writeDecimal(byte[] buf, int off, long unscaledVal, int scale) {
        if (unscaledVal < 0) {
            buf[off++] = (byte) '-';
            unscaledVal = -unscaledVal;
        }

        if (scale == 0) {
            return IOUtils.writeInt64(buf, off, unscaledVal);
        }

        int insertionPoint = IOUtils.stringSize(unscaledVal) - scale;
        if (insertionPoint == 0) {
            buf[off] = '0';
            buf[off + 1] = '.';
            off += 2;
        } else if (insertionPoint < 0) {
            buf[off] = '0';
            buf[off + 1] = '.';
            off += 2;

            for (int i = 0; i < -insertionPoint; i++) {
                buf[off++] = '0';
            }
        }

        off = IOUtils.writeInt64(buf, off, unscaledVal);

        if (insertionPoint > 0) {
            int insertPointOff = off - scale;
            System.arraycopy(buf, insertPointOff, buf, insertPointOff + 1, scale);
            buf[insertPointOff] = (byte) '.';
            off++;
        }

        return off;
    }

    public static int writeDecimal(char[] buf, int off, long unscaledVal, int scale) {
        if (unscaledVal < 0) {
            buf[off++] = (byte) '-';
            unscaledVal = -unscaledVal;
        }

        if (scale == 0) {
            return writeInt64(buf, off, unscaledVal);
        }

        int insertionPoint = stringSize(unscaledVal) - scale;
        if (insertionPoint == 0) {
            buf[off] = '0';
            buf[off + 1] = '.';
            off += 2;
        } else if (insertionPoint < 0) {
            buf[off] = '0';
            buf[off + 1] = '.';
            off += 2;

            for (int i = 0; i < -insertionPoint; i++) {
                buf[off++] = '0';
            }
        }

        off = IOUtils.writeInt64(buf, off, unscaledVal);

        if (insertionPoint > 0) {
            int insertPointOff = off - scale;
            System.arraycopy(buf, insertPointOff, buf, insertPointOff + 1, scale);
            buf[insertPointOff] = '.';
            off++;
        }

        return off;
    }

    public static int encodeUTF8(byte[] src, int offset, int len, byte[] dst, int dp) {
        int sl = offset + len;
        while (offset < sl) {
            byte b0 = src[offset];
            byte b1 = src[offset + 1];
            offset += 2;

            if (b1 == 0 && b0 >= 0) {
                dst[dp++] = b0;
            } else {
                char c = (char) (((b0 & 0xff)) | ((b1 & 0xff) << 8));
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    dst[dp] = (byte) (0xc0 | (c >> 6));
                    dst[dp + 1] = (byte) (0x80 | (c & 0x3f));
                    dp += 2;
                } else if (c >= '\uD800' && c < ('\uDFFF' + 1)) { //Character.isSurrogate(c) but 1.7
                    final int uc;
                    int ip = offset - 1;
                    if (c < '\uDBFF' + 1) { // Character.isHighSurrogate(c)
                        if (sl - ip < 2) {
                            uc = -1;
                        } else {
                            b0 = src[ip + 1];
                            b1 = src[ip + 2];
                            char d = (char) (((b0 & 0xff)) | ((b1 & 0xff) << 8));
                            // d >= '\uDC00' && d < ('\uDFFF' + 1)
                            if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                offset += 2;
                                uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                            } else {
                                return -1;
                            }
                        }
                    } else {
                        //
                        // Character.isLowSurrogate(c)
                        return -1;
                    }

                    if (uc < 0) {
                        dst[dp++] = (byte) '?';
                    } else {
                        dst[dp] = (byte) (0xf0 | ((uc >> 18)));
                        dst[dp + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                        dst[dp + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                        dst[dp + 3] = (byte) (0x80 | (uc & 0x3f));
                        dp += 4;
                    }
                } else {
                    // 3 bytes, 16 bits
                    dst[dp] = (byte) (0xe0 | ((c >> 12)));
                    dst[dp + 1] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    dst[dp + 2] = (byte) (0x80 | (c & 0x3f));
                    dp += 3;
                }
            }
        }
        return dp;
    }

    public static int encodeUTF8(char[] src, int offset, int len, byte[] dst, int dp) {
        int sl = offset + len;
        int dlASCII = dp + Math.min(len, dst.length);

        // ASCII only optimized loop
        while (dp < dlASCII && src[offset] < '\u0080') {
            dst[dp++] = (byte) src[offset++];
        }

        while (offset < sl) {
            char c = src[offset++];
            if (c < 0x80) {
                // Have at most seven bits
                dst[dp++] = (byte) c;
            } else if (c < 0x800) {
                // 2 bytes, 11 bits
                dst[dp] = (byte) (0xc0 | (c >> 6));
                dst[dp + 1] = (byte) (0x80 | (c & 0x3f));
                dp += 2;
            } else if (c >= '\uD800' && c < ('\uDFFF' + 1)) { //Character.isSurrogate(c) but 1.7
                final int uc;
                int ip = offset - 1;
                if (c < '\uDBFF' + 1) { // Character.isHighSurrogate(c)
                    if (sl - ip < 2) {
                        uc = -1;
                    } else {
                        char d = src[ip + 1];
                        // d >= '\uDC00' && d < ('\uDFFF' + 1)
                        if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                            uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                        } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                            dst[dp++] = (byte) '?';
                            continue;
                        }
                    }
                } else {
                    //
                    // Character.isLowSurrogate(c)
                    dst[dp++] = (byte) '?';
                    continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                }

                if (uc < 0) {
                    dst[dp++] = (byte) '?';
                } else {
                    dst[dp] = (byte) (0xf0 | ((uc >> 18)));
                    dst[dp + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                    dst[dp + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                    dst[dp + 3] = (byte) (0x80 | (uc & 0x3f));
                    dp += 4;
                    offset++; // 2 chars
                }
            } else {
                // 3 bytes, 16 bits
                dst[dp] = (byte) (0xe0 | ((c >> 12)));
                dst[dp + 1] = (byte) (0x80 | ((c >> 6) & 0x3f));
                dst[dp + 2] = (byte) (0x80 | (c & 0x3f));
                dp += 3;
            }
        }
        return dp;
    }

    public static boolean isNumber(String str) {
        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(char[] chars, int off, int len) {
        for (int i = off, end = off + len; i < end; ++i) {
            char ch = chars[i];
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(byte[] chars, int off, int len) {
        for (int i = off, end = off + len; i < end; ++i) {
            char ch = (char) chars[i];
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static void close(Closeable x) {
        if (x == null) {
            return;
        }

        try {
            x.close();
        } catch (Exception e) {
            // skip
        }
    }

    public static int decodeUTF8(byte[] src, int off, int len, byte[] dst) {
        final int sl = off + len;
        int dp = 0;

        while (off < sl) {
            int b0 = src[off++];
            if (b0 >= 0) {
                // 1 byte, 7 bits: 0xxxxxxx
                dst[dp] = (byte) b0;
                dst[dp + 1] = 0;
                dp += 2;
            } else if ((b0 >> 5) == -2 && (b0 & 0x1e) != 0) {
                // 2 bytes, 11 bits: 110xxxxx 10xxxxxx
                if (off < sl) {
                    int b1 = src[off++];
                    if ((b1 & 0xc0) != 0x80) { // isNotContinuation(b2)
                        return -1;
                    } else {
                        char c = (char) (((b0 << 6) ^ b1) ^
                                (((byte) 0xC0 << 6) ^
                                        ((byte) 0x80)));
                        dst[dp] = (byte) c;
                        dst[dp + 1] = (byte) (c >> 8);
                        dp += 2;
                    }
                    continue;
                }
                dst[dp] = (byte) b0;
                dst[dp + 1] = 0;
                dp += 2;
                break;
            } else if ((b0 >> 4) == -2) {
                // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
                if (off + 1 < sl) {
                    int b1 = src[off];
                    int b2 = src[off + 1];
                    off += 2;
                    if ((b0 == (byte) 0xe0 && (b1 & 0xe0) == 0x80) //
                            || (b1 & 0xc0) != 0x80 //
                            || (b2 & 0xc0) != 0x80) { // isMalformed3(b0, b1, b2)
                        return -1;
                    } else {
                        char c = (char)
                                ((b0 << 12) ^
                                        (b1 << 6) ^
                                        (b2 ^ (((byte) 0xE0 << 12) ^
                                                ((byte) 0x80 << 6) ^
                                                ((byte) 0x80)))
                                );
                        boolean isSurrogate = c >= '\uD800' && c < ('\uDFFF' + 1);
                        if (isSurrogate) {
                            return -1;
                        } else {
                            dst[dp] = (byte) c;
                            dst[dp + 1] = (byte) (c >> 8);
                            dp += 2;
                        }
                    }
                    continue;
                }
                return -1;
            } else if ((b0 >> 3) == -2) {
                // 4 bytes, 21 bits: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                if (off + 2 < sl) {
                    int b2 = src[off];
                    int b3 = src[off + 1];
                    int b4 = src[off + 2];
                    off += 3;
                    int uc = ((b0 << 18) ^
                            (b2 << 12) ^
                            (b3 << 6) ^
                            (b4 ^
                                    (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80))));
                    if (((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 || (b4 & 0xc0) != 0x80) // isMalformed4
                            ||
                            // shortest form check
                            !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                    ) {
                        return -1;
                    } else {
                        char c = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10)));
                        dst[dp] = (byte) c;
                        dst[dp + 1] = (byte) (c >> 8);
                        dp += 2;

                        c = (char) ((uc & 0x3ff) + '\uDC00');
                        dst[dp] = (byte) c;
                        dst[dp + 1] = (byte) (c >> 8);
                        dp += 2;
                    }
                    continue;
                }
                return -1;
            } else {
                return -1;
            }
        }
        return dp;
    }

    public static int decodeUTF8(byte[] src, int off, int len, char[] dst) {
        final int sl = off + len;
        int dp = 0;
        int dlASCII = Math.min(len, dst.length);

        // ASCII only optimized loop
        while (dp < dlASCII && src[off] >= 0) {
            dst[dp++] = (char) src[off++];
        }

        while (off < sl) {
            int b1 = src[off++];
            if (b1 >= 0) {
                // 1 byte, 7 bits: 0xxxxxxx
                dst[dp++] = (char) b1;
            } else if ((b1 >> 5) == -2 && (b1 & 0x1e) != 0) {
                // 2 bytes, 11 bits: 110xxxxx 10xxxxxx
                if (off < sl) {
                    int b2 = src[off++];
                    if ((b2 & 0xc0) != 0x80) { // isNotContinuation(b2)
                        return -1;
                    } else {
                        dst[dp++] = (char) (((b1 << 6) ^ b2) ^
                                (((byte) 0xC0 << 6) ^
                                        ((byte) 0x80)));
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 4) == -2) {
                // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
                if (off + 1 < sl) {
                    int b2 = src[off];
                    int b3 = src[off + 1];
                    off += 2;
                    if ((b1 == (byte) 0xe0 && (b2 & 0xe0) == 0x80) //
                            || (b2 & 0xc0) != 0x80 //
                            || (b3 & 0xc0) != 0x80) { // isMalformed3(b1, b2, b3)
                        return -1;
                    } else {
                        char c = (char) ((b1 << 12) ^
                                (b2 << 6) ^
                                (b3 ^
                                        (((byte) 0xE0 << 12) ^
                                                ((byte) 0x80 << 6) ^
                                                ((byte) 0x80))));
                        boolean isSurrogate = c >= '\uD800' && c < ('\uDFFF' + 1);
                        if (isSurrogate) {
                            return -1;
                        } else {
                            dst[dp++] = c;
                        }
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 3) == -2) {
                // 4 bytes, 21 bits: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                if (off + 2 < sl) {
                    int b2 = src[off];
                    int b3 = src[off + 1];
                    int b4 = src[off + 2];
                    off += 3;
                    int uc = ((b1 << 18) ^
                            (b2 << 12) ^
                            (b3 << 6) ^
                            (b4 ^
                                    (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80))));
                    if (((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 || (b4 & 0xc0) != 0x80) // isMalformed4
                            ||
                            // shortest form check
                            !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                    ) {
                        return -1;
                    } else {
                        dst[dp] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                        dst[dp + 1] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
                        dp += 2;
                    }
                    continue;
                }
                return -1;
            } else {
                return -1;
            }
        }
        return dp;
    }

    public static long lines(File file) throws Exception {
        try (FileInputStream in = new FileInputStream(file)) {
            return lines(in);
        }
    }

    public static long lines(InputStream in) throws Exception {
        long lines = 0;
        byte[] buf = new byte[1024 * 8];
        while (true) {
            int len = in.read(buf, 0, buf.length);
            if (len == -1) {
                break;
            }
            for (int i = 0; i < len; i++) {
                byte b = buf[i];
                if (b == '\n') {
                    lines++;
                }
            }
        }
        return lines;
    }

    public static int writeLocalDate(byte[] bytes, int off, int year, int month, int dayOfMonth) {
        if (year >= 1000 && year < 10000) {
            final byte y0;
            final int yyy;
            if (year < 3000) {
                if (year < 2000) {
                    yyy = year - 1000;
                    y0 = '1';
                } else {
                    yyy = year - 2000;
                    y0 = '2';
                }
            } else {
                final int q = year / 1000;
                yyy = year - q * 1000;
                y0 = (byte) (q + '0');
            }
            bytes[off] = y0;
            final int v = DIGITS_K[yyy];
            bytes[off + 1] = (byte) (v >> 16);
            bytes[off + 2] = (byte) (v >> 8);
            bytes[off + 3] = (byte) v;
            off += 4;
        } else {
            if (year > 9999) {
                bytes[off++] = '+';
            }
            off = IOUtils.writeInt32(bytes, off, year);
        }

        bytes[off] = '-';
        int v = DIGITS_K[month];
        bytes[off + 1] = (byte) (v >> 8);
        bytes[off + 2] = (byte) (v);
        bytes[off + 3] = '-';
        v = DIGITS_K[dayOfMonth];
        bytes[off + 4] = (byte) (v >> 8);
        bytes[off + 5] = (byte) (v);
        off += 6;

        return off;
    }

    public static int writeLocalDate(char[] chars, int off, int year, int month, int dayOfMonth) {
        if (year >= 1000 && year < 10000) {
            final char y0;
            final int yyy;
            if (year < 3000) {
                if (year < 2000) {
                    yyy = year - 1000;
                    y0 = '1';
                } else {
                    yyy = year - 2000;
                    y0 = '2';
                }
            } else {
                final int q = year / 1000;
                yyy = year - q * 1000;
                y0 = (char) (byte) (q + '0');
            }
            chars[off] = y0;
            final int v = DIGITS_K[yyy];
            chars[off + 1] = (char) (byte) (v >> 16);
            chars[off + 2] = (char) (byte) (v >> 8);
            chars[off + 3] = (char) (byte) v;
            off += 4;
        } else {
            if (year > 9999) {
                chars[off++] = '+';
            }
            off = IOUtils.writeInt32(chars, off, year);
        }

        chars[off] = '-';
        int v = DIGITS_K[month];
        chars[off + 1] = (char) (byte) (v >> 8);
        chars[off + 2] = (char) (byte) (v);
        chars[off + 3] = '-';
        v = DIGITS_K[dayOfMonth];
        chars[off + 4] = (char) (byte) (v >> 8);
        chars[off + 5] = (char) (byte) (v);
        off += 6;

        return off;
    }

    public static int writeLocalTime(byte[] bytes, int off, LocalTime time) {
        int v = DIGITS_K[time.getHour()];
        bytes[off] = (byte) (v >> 8);
        bytes[off + 1] = (byte) (v);
        bytes[off + 2] = ':';
        v = DIGITS_K[time.getMinute()];
        bytes[off + 3] = (byte) (v >> 8);
        bytes[off + 4] = (byte) (v);
        bytes[off + 5] = ':';
        v = DIGITS_K[time.getSecond()];
        bytes[off + 6] = (byte) (v >> 8);
        bytes[off + 7] = (byte) (v);
        off += 8;

        int nano = time.getNano();
        if (nano != 0) {
            final int div = nano / 1000;
            final int div2 = div / 1000;
            final int rem1 = nano - div * 1000;

            bytes[off] = '.';
            v = DIGITS_K[div2];
            bytes[off + 1] = (byte) (v >> 16);
            bytes[off + 2] = (byte) (v >> 8);
            bytes[off + 3] = (byte) v;
            off += 4;

            if (rem1 == 0) {
                final int rem2 = div - div2 * 1000;
                if (rem2 == 0) {
                    return off;
                }

                v = DIGITS_K[rem2];
            } else {
                v = DIGITS_K[div - div2 * 1000];
            }

            bytes[off] = (byte) (v >> 16);
            bytes[off + 1] = (byte) (v >> 8);
            bytes[off + 2] = (byte) v;
            off += 3;
            if (rem1 == 0) {
                return off;
            }

            v = DIGITS_K[rem1];
            bytes[off] = (byte) (v >> 16);
            bytes[off + 1] = (byte) (v >> 8);
            bytes[off + 2] = (byte) v;
            off += 3;
        }

        return off;
    }

    public static int writeLocalTime(char[] bytes, int off, LocalTime time) {
        int v = DIGITS_K[time.getHour()];
        bytes[off] = (char) (byte) (v >> 8);
        bytes[off + 1] = (char) (byte) (v);
        bytes[off + 2] = ':';
        v = DIGITS_K[time.getMinute()];
        bytes[off + 3] = (char) (byte) (v >> 8);
        bytes[off + 4] = (char) (byte) (v);
        bytes[off + 5] = ':';
        v = DIGITS_K[time.getSecond()];
        bytes[off + 6] = (char) (byte) (v >> 8);
        bytes[off + 7] = (char) (byte) (v);
        off += 8;

        int nano = time.getNano();
        if (nano != 0) {
            final int div = nano / 1000;
            final int div2 = div / 1000;
            final int rem1 = nano - div * 1000;

            bytes[off] = '.';
            v = DIGITS_K[div2];
            bytes[off + 1] = (char) (byte) (v >> 16);
            bytes[off + 2] = (char) (byte) (v >> 8);
            bytes[off + 3] = (char) (byte) v;
            off += 4;

            if (rem1 == 0) {
                final int rem2 = div - div2 * 1000;
                if (rem2 == 0) {
                    return off;
                }

                v = DIGITS_K[rem2];
            } else {
                v = DIGITS_K[div - div2 * 1000];
            }

            bytes[off] = (char) (byte) (v >> 16);
            bytes[off + 1] = (char) (byte) (v >> 8);
            bytes[off + 2] = (char) (byte) v;
            off += 3;
            if (rem1 == 0) {
                return off;
            }

            v = DIGITS_K[rem1];
            bytes[off] = (char) (byte) (v >> 16);
            bytes[off + 1] = (char) (byte) (v >> 8);
            bytes[off + 2] = (char) (byte) v;
            off += 3;
        }

        return off;
    }

    public static int writeInt64(final byte[] buf, int pos, final long value) {
        long i;
        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                System.arraycopy(MIN_LONG, 0, buf, pos, MIN_LONG.length);
                return pos + MIN_LONG.length;
            }
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        if (i < 1000) {
            int v = DIGITS_K[(int) i];
            int start = v >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v >> 16);
                buf[pos + 1] = (byte) (v >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 8);
            }
            buf[pos++] = (byte) v;
            return pos;
        }

        final long q1 = i / 1000;
        final int r1 = (int) (i - q1 * 1000);
        final int v1 = DIGITS_K[r1];
        if (i < 1000000) {
            final int v2 = DIGITS_K[(int) q1];

            int start = v2 >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v2 >> 16);
                buf[pos + 1] = (byte) (v2 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v2 >> 8);
            }
            buf[pos] = (byte) v2;
            buf[pos + 1] = (byte) (v1 >> 16);
            buf[pos + 2] = (byte) (v1 >> 8);
            buf[pos + 3] = (byte) v1;
            return pos + 4;
        }

        final long q2 = q1 / 1000;
        final int r2 = (int) (q1 - q2 * 1000);
        final long q3 = q2 / 1000;
        final int v2 = DIGITS_K[r2];
        if (q3 == 0) {
            final int v3 = DIGITS_K[(int) q2];
            int start = v3 >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v3 >> 16);
                buf[pos + 1] = (byte) (v3 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v3 >> 8);
            }
            buf[pos] = (byte) v3;
            buf[pos + 1] = (byte) (v2 >> 16);
            buf[pos + 2] = (byte) (v2 >> 8);
            buf[pos + 3] = (byte) v2;
            buf[pos + 4] = (byte) (v1 >> 16);
            buf[pos + 5] = (byte) (v1 >> 8);
            buf[pos + 6] = (byte) v1;
            return pos + 7;
        }
        final int r3 = (int) (q2 - q3 * 1000);
        final int q4 = (int) (q3 / 1000);
        final int v3 = DIGITS_K[r3];
        if (q4 == 0) {
            final int v4 = DIGITS_K[(int) q3];
            final int start = v4 >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v4 >> 16);
                buf[pos + 1] = (byte) (v4 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v4 >> 8);
            }
            buf[pos] = (byte) v4;

            buf[pos + 1] = (byte) (v3 >> 16);
            buf[pos + 2] = (byte) (v3 >> 8);
            buf[pos + 3] = (byte) v3;
            buf[pos + 4] = (byte) (v2 >> 16);
            buf[pos + 5] = (byte) (v2 >> 8);
            buf[pos + 6] = (byte) v2;
            buf[pos + 7] = (byte) (v1 >> 16);
            buf[pos + 8] = (byte) (v1 >> 8);
            buf[pos + 9] = (byte) v1;
            return pos + 10;
        }
        final int r4 = (int) (q3 - q4 * 1000);
        final int q5 = q4 / 1000;

        final int v4 = DIGITS_K[r4];
        if (q5 == 0) {
            final int v5 = DIGITS_K[q4];
            int start = v5 >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v5 >> 16);
                buf[pos + 1] = (byte) (v5 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v5 >> 8);
            }
            buf[pos] = (byte) v5;
            buf[pos + 1] = (byte) (v4 >> 16);
            buf[pos + 2] = (byte) (v4 >> 8);
            buf[pos + 3] = (byte) v4;
            buf[pos + 4] = (byte) (v3 >> 16);
            buf[pos + 5] = (byte) (v3 >> 8);
            buf[pos + 6] = (byte) v3;
            buf[pos + 7] = (byte) (v2 >> 16);
            buf[pos + 8] = (byte) (v2 >> 8);
            buf[pos + 9] = (byte) v2;
            buf[pos + 10] = (byte) (v1 >> 16);
            buf[pos + 11] = (byte) (v1 >> 8);
            buf[pos + 12] = (byte) v1;
            return pos + 13;
        }
        final int r5 = q4 - q5 * 1000;
        final int q6 = q5 / 1000;
        final int v5 = DIGITS_K[r5];
        if (q6 == 0) {
            int v = DIGITS_K[q5];
            final int start = v >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v >> 16);
                buf[pos + 1] = (byte) (v >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 8);
            }
            buf[pos++] = (byte) v;
        } else {
            final int r6 = q5 - q6 * 1000;
            buf[pos] = (byte) (q6 + '0');
            int v = DIGITS_K[r6];
            buf[pos + 1] = (byte) (v >> 16);
            buf[pos + 2] = (byte) (v >> 8);
            buf[pos + 3] = (byte) v;
            pos += 4;
        }

        buf[pos] = (byte) (v5 >> 16);
        buf[pos + 1] = (byte) (v5 >> 8);
        buf[pos + 2] = (byte) v5;
        buf[pos + 3] = (byte) (v4 >> 16);
        buf[pos + 4] = (byte) (v4 >> 8);
        buf[pos + 5] = (byte) v4;
        buf[pos + 6] = (byte) (v3 >> 16);
        buf[pos + 7] = (byte) (v3 >> 8);
        buf[pos + 8] = (byte) v3;
        buf[pos + 9] = (byte) (v2 >> 16);
        buf[pos + 10] = (byte) (v2 >> 8);
        buf[pos + 11] = (byte) v2;
        buf[pos + 12] = (byte) (v1 >> 16);
        buf[pos + 13] = (byte) (v1 >> 8);
        buf[pos + 14] = (byte) v1;
        return pos + 15;
    }

    public static int writeInt64(final char[] buf, int pos, final long value) {
        long i;
        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                for (int x = 0; x < MIN_LONG.length; x++) {
                    buf[pos + x] = (char) MIN_LONG[x];
                }
                return pos + MIN_LONG.length;
            }
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        if (i < 1000) {
            int v = DIGITS_K[(int) i];
            int start = v >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v >> 16);
                buf[pos + 1] = (char) (byte) (v >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v >> 8);
            }
            buf[pos++] = (char) (byte) v;
            return pos;
        }

        final long q1 = i / 1000;
        final int r1 = (int) (i - q1 * 1000);
        final int v1 = DIGITS_K[r1];
        if (i < 1000000) {
            final int v2 = DIGITS_K[(int) q1];

            int start = v2 >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v2 >> 16);
                buf[pos + 1] = (char) (byte) (v2 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v2 >> 8);
            }
            buf[pos] = (char) (byte) v2;
            buf[pos + 1] = (char) (byte) (v1 >> 16);
            buf[pos + 2] = (char) (byte) (v1 >> 8);
            buf[pos + 3] = (char) (byte) v1;
            return pos + 4;
        }

        final long q2 = q1 / 1000;
        final int r2 = (int) (q1 - q2 * 1000);
        final long q3 = q2 / 1000;
        final int v2 = DIGITS_K[r2];
        if (q3 == 0) {
            final int v3 = DIGITS_K[(int) q2];
            int start = v3 >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v3 >> 16);
                buf[pos + 1] = (char) (byte) (v3 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v3 >> 8);
            }
            buf[pos] = (char) (byte) v3;
            buf[pos + 1] = (char) (byte) (v2 >> 16);
            buf[pos + 2] = (char) (byte) (v2 >> 8);
            buf[pos + 3] = (char) (byte) v2;
            buf[pos + 4] = (char) (byte) (v1 >> 16);
            buf[pos + 5] = (char) (byte) (v1 >> 8);
            buf[pos + 6] = (char) (byte) v1;
            return pos + 7;
        }
        final int r3 = (int) (q2 - q3 * 1000);
        final int q4 = (int) (q3 / 1000);
        final int v3 = DIGITS_K[r3];
        if (q4 == 0) {
            final int v4 = DIGITS_K[(int) q3];

            final int start = v4 >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v4 >> 16);
                buf[pos + 1] = (char) (byte) (v4 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v4 >> 8);
            }
            buf[pos] = (char) (byte) v4;
            buf[pos + 1] = (char) (byte) (v3 >> 16);
            buf[pos + 2] = (char) (byte) (v3 >> 8);
            buf[pos + 3] = (char) (byte) v3;
            buf[pos + 4] = (char) (byte) (v2 >> 16);
            buf[pos + 5] = (char) (byte) (v2 >> 8);
            buf[pos + 6] = (char) (byte) v2;
            buf[pos + 7] = (char) (byte) (v1 >> 16);
            buf[pos + 8] = (char) (byte) (v1 >> 8);
            buf[pos + 9] = (char) (byte) v1;
            return pos + 10;
        }
        final int r4 = (int) (q3 - q4 * 1000);
        final int q5 = q4 / 1000;
        final int v4 = DIGITS_K[r4];
        if (q5 == 0) {
            final int v5 = DIGITS_K[q4];
            int start = v5 >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v5 >> 16);
                buf[pos + 1] = (char) (byte) (v5 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v5 >> 8);
            }
            buf[pos] = (char) (byte) v5;
            buf[pos + 1] = (char) (byte) (v4 >> 16);
            buf[pos + 2] = (char) (byte) (v4 >> 8);
            buf[pos + 3] = (char) (byte) v4;
            buf[pos + 4] = (char) (byte) (v3 >> 16);
            buf[pos + 5] = (char) (byte) (v3 >> 8);
            buf[pos + 6] = (char) (byte) v3;
            buf[pos + 7] = (char) (byte) (v2 >> 16);
            buf[pos + 8] = (char) (byte) (v2 >> 8);
            buf[pos + 9] = (char) (byte) v2;
            buf[pos + 10] = (char) (byte) (v1 >> 16);
            buf[pos + 11] = (char) (byte) (v1 >> 8);
            buf[pos + 12] = (char) (byte) v1;
            return pos + 13;
        }
        final int r5 = q4 - q5 * 1000;
        final int q6 = q5 / 1000;
        final int v5 = DIGITS_K[r5];
        if (q6 == 0) {
            int v = DIGITS_K[q5];
            final int start = v >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v >> 16);
                buf[pos + 1] = (char) (byte) (v >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v >> 8);
            }
            buf[pos++] = (char) (byte) v;
        } else {
            final int r6 = q5 - q6 * 1000;
            buf[pos] = (char) (byte) (q6 + '0');
            int v = DIGITS_K[r6];
            buf[pos + 1] = (char) (byte) (v >> 16);
            buf[pos + 2] = (char) (byte) (v >> 8);
            buf[pos + 3] = (char) (byte) v;
            pos += 4;
        }

        buf[pos] = (char) (byte) (v5 >> 16);
        buf[pos + 1] = (char) (byte) (v5 >> 8);
        buf[pos + 2] = (char) (byte) v5;
        buf[pos + 3] = (char) (byte) (v4 >> 16);
        buf[pos + 4] = (char) (byte) (v4 >> 8);
        buf[pos + 5] = (char) (byte) v4;
        buf[pos + 6] = (char) (byte) (v3 >> 16);
        buf[pos + 7] = (char) (byte) (v3 >> 8);
        buf[pos + 8] = (char) (byte) v3;
        buf[pos + 9] = (char) (byte) (v2 >> 16);
        buf[pos + 10] = (char) (byte) (v2 >> 8);
        buf[pos + 11] = (char) (byte) v2;
        buf[pos + 12] = (char) (byte) (v1 >> 16);
        buf[pos + 13] = (char) (byte) (v1 >> 8);
        buf[pos + 14] = (char) (byte) v1;
        return pos + 15;
    }

    public static int writeInt32(final byte[] buf, int pos, final int value) {
        int i;
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                System.arraycopy(MIN_INT_BYTES, 0, buf, pos, MIN_INT_BYTES.length);
                return pos + MIN_INT_BYTES.length;
            }
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        if (i < 1000) {
            int v = DIGITS_K[i];
            final int start = v >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v >> 16);
                buf[pos + 1] = (byte) (v >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 8);
            }
            buf[pos] = (byte) v;
            return pos + 1;
        }

        final int q1 = i / 1000;
        final int r1 = i - q1 * 1000;
        final int v1 = DIGITS_K[r1];
        if (i < 1000000) {
            final int v2 = DIGITS_K[q1];
            int start = v2 >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v2 >> 16);
                buf[pos + 1] = (byte) (v2 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v2 >> 8);
            }
            buf[pos] = (byte) v2;
            buf[pos + 1] = (byte) (v1 >> 16);
            buf[pos + 2] = (byte) (v1 >> 8);
            buf[pos + 3] = (byte) v1;
            return pos + 4;
        }
        final int q2 = q1 / 1000;
        final int r2 = q1 - q2 * 1000;
        final int q3 = q2 / 1000;
        final int v2 = DIGITS_K[r2];
        if (q3 == 0) {
            int v = DIGITS_K[q2];
            final int start = v >> 24;
            if (start == 0) {
                buf[pos] = (byte) (v >> 16);
                buf[pos + 1] = (byte) (v >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 8);
            }
            buf[pos++] = (byte) v;
        } else {
            final int r3 = q2 - q3 * 1000;
            buf[pos] = (byte) (q3 + '0');
            int v = DIGITS_K[r3];
            buf[pos + 1] = (byte) (v >> 16);
            buf[pos + 2] = (byte) (v >> 8);
            buf[pos + 3] = (byte) v;
            pos += 4;
        }

        buf[pos] = (byte) (v2 >> 16);
        buf[pos + 1] = (byte) (v2 >> 8);
        buf[pos + 2] = (byte) v2;
        buf[pos + 3] = (byte) (v1 >> 16);
        buf[pos + 4] = (byte) (v1 >> 8);
        buf[pos + 5] = (byte) v1;
        return pos + 6;
    }

    public static int writeInt32(final char[] buf, int pos, final int value) {
        int i;
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                System.arraycopy(MIN_INT_CHARS, 0, buf, pos, MIN_INT_CHARS.length);
                return pos + MIN_INT_CHARS.length;
            }
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }
        if (i < 1000) {
            int v = DIGITS_K[i];
            final int start = v >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v >> 16);
                buf[pos + 1] = (char) (byte) (v >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v >> 8);
            }
            buf[pos] = (char) (byte) v;
            return pos + 1;
        }
        final int q1 = i / 1000;
        final int r1 = i - q1 * 1000;
        final int v1 = DIGITS_K[r1];
        if (i < 1000000) {
            final int v2 = DIGITS_K[q1];
            int start = v2 >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v2 >> 16);
                buf[pos + 1] = (char) (byte) (v2 >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v2 >> 8);
            }
            buf[pos] = (char) (byte) v2;
            buf[pos + 1] = (char) (byte) (v1 >> 16);
            buf[pos + 2] = (char) (byte) (v1 >> 8);
            buf[pos + 3] = (char) (byte) v1;
            return pos + 4;
        }
        final int q2 = q1 / 1000;
        final int r2 = q1 - q2 * 1000;
        final int q3 = q2 / 1000;
        final int v2 = DIGITS_K[r2];
        if (q3 == 0) {
            int v = DIGITS_K[q2];
            final int start = v >> 24;
            if (start == 0) {
                buf[pos] = (char) (byte) (v >> 16);
                buf[pos + 1] = (char) (byte) (v >> 8);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v >> 8);
            }
            buf[pos++] = (char) (byte) v;
        } else {
            final int r3 = q2 - q3 * 1000;
            buf[pos] = (char) (byte) (q3 + '0');
            int v = DIGITS_K[r3];
            buf[pos + 1] = (char) (byte) (v >> 16);
            buf[pos + 2] = (char) (byte) (v >> 8);
            buf[pos + 3] = (char) (byte) v;
            pos += 4;
        }

        buf[pos] = (char) (byte) (v2 >> 16);
        buf[pos + 1] = (char) (byte) (v2 >> 8);
        buf[pos + 2] = (char) (byte) v2;
        buf[pos + 3] = (char) (byte) (v1 >> 16);
        buf[pos + 4] = (char) (byte) (v1 >> 8);
        buf[pos + 5] = (char) (byte) v1;
        return pos + 6;
    }

    public static int getInt(byte[] bytes, int offset) {
        return ((bytes[offset + 3] & 0xFF)) +
                ((bytes[offset + 2] & 0xFF) << 8) +
                ((bytes[offset + 1] & 0xFF) << 16) +
                ((bytes[offset]) << 24);
    }

    public static long getLong(byte[] bytes, int offset) {
        return ((bytes[offset + 7] & 0xFFL)) +
                ((bytes[offset + 6] & 0xFFL) << 8) +
                ((bytes[offset + 5] & 0xFFL) << 16) +
                ((bytes[offset + 4] & 0xFFL) << 24) +
                ((bytes[offset + 3] & 0xFFL) << 32) +
                ((bytes[offset + 2] & 0xFFL) << 40) +
                ((bytes[offset + 1] & 0xFFL) << 48) +
                ((long) (bytes[offset]) << 56);
    }
}
