package com.alibaba.fastjson2.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.Arrays;

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

    static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    static final int[] IA = new int[256];

    private static final int[] DIGITS_K = new int[1000];
    private static final byte MINUS = '-';
    private static final byte[] MIN_INT = "-2147483648".getBytes();
    private static final byte[] MIN_LONG = "-9223372036854775808".getBytes();

    static {
        Arrays.fill(IA, -1);
        for (int i = 0, iS = CA.length; i < iS; i++) {
            IA[CA[i]] = i;
        }
        IA['='] = 0;

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
        for (; ; ) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--p] = DIGITS[r];
            i = q;
            if (i == 0) {
                break;
            }
        }
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
        for (; ; ) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--p] = (char) DIGITS[r];
            i = q;
            if (i == 0) {
                break;
            }
        }
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
        for (; ; ) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buf[--charPos] = DIGITS[r];
            i2 = q2;
            if (i2 == 0) {
                break;
            }
        }
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
        for (; ; ) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buf[--charPos] = (char) DIGITS[r];
            i2 = q2;
            if (i2 == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    public static int getDecimalChars(long unscaledVal, int scale, byte[] buf, int off) {
        final int start = off;
        boolean negative = unscaledVal < 0;
        int size = IOUtils.stringSize(Math.abs(unscaledVal));

        if (scale == 0) {
            if (unscaledVal < 0) {
                size++;
            }
            getChars(unscaledVal, off + size, buf);
            return size;
        }

        int insertionPoint = size - scale;
        if (insertionPoint == 0) {
            if (negative) {
                buf[off++] = (byte) '-';
            }
            buf[off++] = (byte) '0';
            buf[off++] = (byte) '.';
        } else if (insertionPoint < 0) {
            if (negative) {
                buf[off++] = (byte) '-';
            }
            buf[off++] = (byte) '0';
            buf[off++] = (byte) '.';

            for (int i = 0; i < -insertionPoint; i++) {
                buf[off++] = (byte) '0';
            }
        } else {
            if (negative) {
                buf[off++] = (byte) '-';
            }
        }

        IOUtils.getChars(Math.abs(unscaledVal), off + size, buf);
        off += size;

        if (insertionPoint > 0) {
            int insertPointOff = off - scale;
            System.arraycopy(buf, insertPointOff, buf, insertPointOff + 1, scale);
            buf[insertPointOff] = (byte) '.';
            off++;
        }

        return off - start;
    }

    public static int getDecimalChars(long unscaledVal, int scale, char[] buf, int off) {
        final int start = off;
        boolean negative = unscaledVal < 0;
        int size = IOUtils.stringSize(Math.abs(unscaledVal));

        if (scale == 0) {
            if (unscaledVal < 0) {
                size++;
            }
            getChars(unscaledVal, off + size, buf);
            return size;
        }

        int insertionPoint = size - scale;
        if (insertionPoint == 0) {
            if (negative) {
                buf[off++] = '-';
            }
            buf[off++] = '0';
            buf[off++] = '.';
        } else if (insertionPoint < 0) {
            if (negative) {
                buf[off++] = '-';
            }
            buf[off++] = '0';
            buf[off++] = '.';

            for (int i = 0; i < -insertionPoint; i++) {
                buf[off++] = '0';
            }
        } else {
            if (negative) {
                buf[off++] = '-';
            }
        }

        IOUtils.getChars(Math.abs(unscaledVal), off + size, buf);
        off += size;

        if (insertionPoint > 0) {
            int insertPointOff = off - scale;
            System.arraycopy(buf, insertPointOff, buf, insertPointOff + 1, scale);
            buf[insertPointOff] = '.';
            off++;
        }

        return off - start;
    }

    public static int encodeUTF8(byte[] src, int offset, int len, byte[] dst, int dp) {
        int sl = offset + len;
        while (offset < sl) {
            byte b0 = src[offset++];
            byte b1 = src[offset++];

            if (b1 == 0 && b0 >= 0) {
                dst[dp++] = b0;
            } else {
                char c = (char) (((b0 & 0xff) << 0) | ((b1 & 0xff) << 8));
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    dst[dp++] = (byte) (0xc0 | (c >> 6));
                    dst[dp++] = (byte) (0x80 | (c & 0x3f));
                } else if (c >= '\uD800' && c < ('\uDFFF' + 1)) { //Character.isSurrogate(c) but 1.7
                    final int uc;
                    int ip = offset - 1;
                    if (c >= '\uD800' && c < ('\uDBFF' + 1)) { // Character.isHighSurrogate(c)
                        if (sl - ip < 2) {
                            uc = -1;
                        } else {
                            b0 = src[ip + 1];
                            b1 = src[ip + 2];
                            char d = (char) (((b0 & 0xff) << 0) | ((b1 & 0xff) << 8));
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
                        if (c >= '\uDC00' && c < ('\uDFFF' + 1)) { // Character.isLowSurrogate(c)
                            return -1;
                        } else {
                            uc = c;
                        }
                    }

                    if (uc < 0) {
                        dst[dp++] = (byte) '?';
                    } else {
                        dst[dp++] = (byte) (0xf0 | ((uc >> 18)));
                        dst[dp++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                        dst[dp++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                        dst[dp++] = (byte) (0x80 | (uc & 0x3f));
                    }
                } else {
                    // 3 bytes, 16 bits
                    dst[dp++] = (byte) (0xe0 | ((c >> 12)));
                    dst[dp++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    dst[dp++] = (byte) (0x80 | (c & 0x3f));
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
                dst[dp++] = (byte) (0xc0 | (c >> 6));
                dst[dp++] = (byte) (0x80 | (c & 0x3f));
            } else if (c >= '\uD800' && c < ('\uDFFF' + 1)) { //Character.isSurrogate(c) but 1.7
                final int uc;
                int ip = offset - 1;
                if (c >= '\uD800' && c < ('\uDBFF' + 1)) { // Character.isHighSurrogate(c)
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
                    if (c >= '\uDC00' && c < ('\uDFFF' + 1)) { // Character.isLowSurrogate(c)
                        dst[dp++] = (byte) '?';
                        continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                    } else {
                        uc = c;
                    }
                }

                if (uc < 0) {
                    dst[dp++] = (byte) '?';
                } else {
                    dst[dp++] = (byte) (0xf0 | ((uc >> 18)));
                    dst[dp++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                    dst[dp++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                    dst[dp++] = (byte) (0x80 | (uc & 0x3f));
                    offset++; // 2 chars
                }
            } else {
                // 3 bytes, 16 bits
                dst[dp++] = (byte) (0xe0 | ((c >> 12)));
                dst[dp++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                dst[dp++] = (byte) (0x80 | (c & 0x3f));
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
                dst[dp++] = (byte) b0;
                dst[dp++] = 0;
            } else if ((b0 >> 5) == -2 && (b0 & 0x1e) != 0) {
                // 2 bytes, 11 bits: 110xxxxx 10xxxxxx
                if (off < sl) {
                    int b1 = src[off++];
                    if ((b1 & 0xc0) != 0x80) { // isNotContinuation(b2)
                        return -1;
                    } else {
                        char c = (char) (((b0 << 6) ^ b1) ^
                                (((byte) 0xC0 << 6) ^
                                        ((byte) 0x80 << 0)));
                        dst[dp++] = (byte) c;
                        dst[dp++] = (byte) (c >> 8);
                    }
                    continue;
                }
                dst[dp++] = (byte) b0;
                dst[dp++] = 0;
                break;
            } else if ((b0 >> 4) == -2) {
                // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
                if (off + 1 < sl) {
                    int b1 = src[off++];
                    int b2 = src[off++];
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
                                                ((byte) 0x80 << 0)))
                                );
                        boolean isSurrogate = c >= '\uD800' && c < ('\uDFFF' + 1);
                        if (isSurrogate) {
                            return -1;
                        } else {
                            dst[dp++] = (byte) c;
                            dst[dp++] = (byte) (c >> 8);
                        }
                    }
                    continue;
                }
                return -1;
            } else if ((b0 >> 3) == -2) {
                // 4 bytes, 21 bits: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                if (off + 2 < sl) {
                    int b2 = src[off++];
                    int b3 = src[off++];
                    int b4 = src[off++];
                    int uc = ((b0 << 18) ^
                            (b2 << 12) ^
                            (b3 << 6) ^
                            (b4 ^
                                    (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80 << 0))));
                    if (((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 || (b4 & 0xc0) != 0x80) // isMalformed4
                            ||
                            // shortest form check
                            !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                    ) {
                        return -1;
                    } else {
                        char c = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10)));
                        dst[dp++] = (byte) c;
                        dst[dp++] = (byte) (c >> 8);

                        c = (char) ((uc & 0x3ff) + '\uDC00');
                        dst[dp++] = (byte) c;
                        dst[dp++] = (byte) (c >> 8);
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
                                        ((byte) 0x80 << 0)));
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 4) == -2) {
                // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
                if (off + 1 < sl) {
                    int b2 = src[off++];
                    int b3 = src[off++];
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
                                                ((byte) 0x80 << 0))));
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
                    int b2 = src[off++];
                    int b3 = src[off++];
                    int b4 = src[off++];
                    int uc = ((b1 << 18) ^
                            (b2 << 12) ^
                            (b3 << 6) ^
                            (b4 ^
                                    (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80 << 0))));
                    if (((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 || (b4 & 0xc0) != 0x80) // isMalformed4
                            ||
                            // shortest form check
                            !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                    ) {
                        return -1;
                    } else {
                        dst[dp++] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                        dst[dp++] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
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

    public static byte[] decodeBase64(String s) {
        // Check special case
        int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }

        int sIx = 0, eIx = sLen - 1; // Start and end index after trimming.

        // Trim illegal chars from start
        while (sIx < eIx && IA[s.charAt(sIx) & 0xff] < 0) {
            sIx++;
        }

        // Trim illegal chars from end
        while (eIx > 0 && IA[s.charAt(eIx) & 0xff] < 0) {
            eIx--;
        }

        // get the padding count (=) (0, 1 or 2)
        int pad = s.charAt(eIx) == '=' ? (s.charAt(eIx - 1) == '=' ? 2 : 1) : 0; // Count '=' at end.
        int cCnt = eIx - sIx + 1; // Content count including possible separators
        int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
        byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

        // Decode all but the last 0 - 2 bytes.
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6
                    | IA[s.charAt(sIx++)];

            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            dArr[d++] = (byte) (i >> 8);
            dArr[d++] = (byte) i;

            // If line separator, jump over it.
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            // Decode last 1-3 bytes (incl '=') into 1-3 bytes
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) {
                i |= IA[s.charAt(sIx++)] << (18 - j * 6);
            }

            for (int r = 16; d < len; r -= 8) {
                dArr[d++] = (byte) (i >> r);
            }
        }

        return dArr;
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

    public static void write4(final int value, final byte[] buf, final int pos) {
        if (value > 9999) {
            throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + value);
        }
        final int q = value / 1000;
        final int v = DIGITS_K[value - q * 1000];
        buf[pos] = (byte) (q + '0');
        buf[pos + 1] = (byte) (v >> 16);
        buf[pos + 2] = (byte) (v >> 8);
        buf[pos + 3] = (byte) v;
    }

    public static void write4(final int value, final char[] buf, final int pos) {
        if (value > 9999) {
            throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + value);
        }
        final int q = value / 1000;
        final int v = DIGITS_K[value - q * 1000];
        buf[pos] = (char) (byte) (q + '0');
        buf[pos + 1] = (char) (byte) (v >> 16);
        buf[pos + 2] = (char) (byte) (v >> 8);
        buf[pos + 3] = (char) (byte) v;
    }

    public static void write3(final int number, final byte[] buf, int pos) {
        final int v = DIGITS_K[number];
        buf[pos] = (byte) (v >> 16);
        buf[pos + 1] = (byte) (v >> 8);
        buf[pos + 2] = (byte) v;
    }

    public static void write3(final int number, final char[] buf, int pos) {
        final int v = DIGITS_K[number];
        buf[pos] = (char) (byte) (v >> 16);
        buf[pos + 1] = (char) (byte) (v >> 8);
        buf[pos + 2] = (char) (byte) v;
    }

    public static void write2(final int number, final byte[] buf, int pos) {
        final int v = DIGITS_K[number];
        buf[pos] = (byte) (v >> 8);
        buf[pos + 1] = (byte) v;
    }

    public static void write2(final int number, final char[] buf, int pos) {
        final int v = DIGITS_K[number];
        buf[pos] = (char) (byte) (v >> 8);
        buf[pos + 1] = (char) (byte) v;
    }

    public static int writeLocalDate(byte[] bytes, int off, int year, int month, int dayOfMonth) {
        if (year >= 1000 && year < 10000) {
            if (year < 3000) {
                int yyy;
                byte y0;
                if (year < 2000) {
                    yyy = year - 1000;
                    y0 = '1';
                } else {
                    yyy = year - 2000;
                    y0 = '2';
                }
                bytes[off] = y0;
                IOUtils.write3(yyy, bytes, off + 1);
            } else {
                IOUtils.write4(year, bytes, off);
            }
            off += 4;
        } else {
            off = IOUtils.writeInt32(bytes, off, year);
        }
        bytes[off] = '-';
        {
            int v = DIGITS_K[month];
            bytes[off + 1] = (byte) (v >> 8);
            bytes[off + 2] = (byte) (v);
        }
        bytes[off + 3] = '-';
        {
            int v = DIGITS_K[dayOfMonth];
            bytes[off + 4] = (byte) (v >> 8);
            bytes[off + 5] = (byte) (v);
        }
        off += 6;

        return off;
    }

    public static int writeLocalDate(char[] bytes, int off, int year, int month, int dayOfMonth) {
        if (year >= 1000 && year < 10000) {
            if (year < 3000) {
                int yyy;
                char y0;
                if (year < 2000) {
                    yyy = year - 1000;
                    y0 = '1';
                } else {
                    yyy = year - 2000;
                    y0 = '2';
                }
                bytes[off] = y0;
                IOUtils.write3(yyy, bytes, off + 1);
            } else {
                IOUtils.write4(year, bytes, off);
            }
            off += 4;
        } else {
            off = IOUtils.writeInt32(bytes, off, year);
        }
        bytes[off] = '-';
        {
            int v = DIGITS_K[month];
            bytes[off + 1] = (char) (byte) (v >> 8);
            bytes[off + 2] = (char) (byte) (v);
        }
        bytes[off + 3] = '-';
        {
            int v = DIGITS_K[dayOfMonth];
            bytes[off + 4] = (char) (byte) (v >> 8);
            bytes[off + 5] = (char) (byte) (v);
        }
        off += 6;

        return off;
    }

    public static int writeLocalTime(byte[] bytes, int off, LocalTime time) {
        int hour = time.getHour();
        IOUtils.write2(hour, bytes, off);
        off += 2;

        bytes[off++] = ':';

        int minute = time.getMinute();
        IOUtils.write2(minute, bytes, off);
        off += 2;

        bytes[off++] = ':';

        int second = time.getSecond();
        IOUtils.write2(second, bytes, off);
        off += 2;

        int nano = time.getNano();
        if (nano != 0) {
            final int div = nano / 1000;
            final int div2 = div / 1000;
            final int rem1 = nano - div * 1000;

            bytes[off++] = '.';
            IOUtils.write3(div2, bytes, off);
            if (rem1 != 0) {
                IOUtils.write3(div - div2 * 1000, bytes, off + 3);
                IOUtils.write3(rem1, bytes, off + 6);
                off += 9;
            } else {
                final int rem2 = div - div2 * 1000;
                if (rem2 != 0) {
                    IOUtils.write3(rem2, bytes, off + 3);
                    off += 6;
                } else {
                    off += 3;
                }
            }
        }

        return off;
    }

    public static int writeLocalTime(char[] bytes, int off, LocalTime time) {
        int hour = time.getHour();
        IOUtils.write2(hour, bytes, off);
        off += 2;

        bytes[off++] = ':';

        int minute = time.getMinute();
        IOUtils.write2(minute, bytes, off);
        off += 2;

        bytes[off++] = ':';

        int second = time.getSecond();
        IOUtils.write2(second, bytes, off);
        off += 2;

        int nano = time.getNano();
        if (nano != 0) {
            final int div = nano / 1000;
            final int div2 = div / 1000;
            final int rem1 = nano - div * 1000;

            bytes[off++] = '.';
            IOUtils.write3(div2, bytes, off);
            if (rem1 != 0) {
                IOUtils.write3(div - div2 * 1000, bytes, off + 3);
                IOUtils.write3(rem1, bytes, off + 6);
                off += 9;
            } else {
                final int rem2 = div - div2 * 1000;
                if (rem2 != 0) {
                    IOUtils.write3(rem2, bytes, off + 3);
                    off += 6;
                } else {
                    off += 3;
                }
            }
        }

        return off;
    }

    public static int writeInt64(final byte[] buf, int pos, final long value) {
        long i;
        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                for (int x = 0; x < MIN_LONG.length; x++) {
                    buf[pos + x] = MIN_LONG[x];
                }
                return pos + MIN_LONG.length;
            }
            i = -value;
            buf[pos++] = MINUS;
        } else {
            i = value;
        }
        final long q1 = i / 1000;
        if (q1 == 0) {
            pos += writeFirstBuf(buf, DIGITS_K[(int) i], pos);
            return pos;
        }
        final int r1 = (int) (i - q1 * 1000);
        final long q2 = q1 / 1000;
        if (q2 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[(int) q1];
            int off = writeFirstBuf(buf, v2, pos);
            writeBuf(buf, v1, pos + off);
            return pos + 3 + off;
        }
        final int r2 = (int) (q1 - q2 * 1000);
        final long q3 = q2 / 1000;
        if (q3 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[r2];
            final int v3 = DIGITS_K[(int) q2];
            pos += writeFirstBuf(buf, v3, pos);
            writeBuf(buf, v2, pos);
            writeBuf(buf, v1, pos + 3);
            return pos + 6;
        }
        final int r3 = (int) (q2 - q3 * 1000);
        final int q4 = (int) (q3 / 1000);
        if (q4 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[r2];
            final int v3 = DIGITS_K[r3];
            final int v4 = DIGITS_K[(int) q3];
            pos += writeFirstBuf(buf, v4, pos);
            writeBuf(buf, v3, pos);
            writeBuf(buf, v2, pos + 3);
            writeBuf(buf, v1, pos + 6);
            return pos + 9;
        }
        final int r4 = (int) (q3 - q4 * 1000);
        final int q5 = q4 / 1000;
        if (q5 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[r2];
            final int v3 = DIGITS_K[r3];
            final int v4 = DIGITS_K[r4];
            final int v5 = DIGITS_K[q4];
            pos += writeFirstBuf(buf, v5, pos);
            writeBuf(buf, v4, pos);
            writeBuf(buf, v3, pos + 3);
            writeBuf(buf, v2, pos + 6);
            writeBuf(buf, v1, pos + 9);
            return pos + 12;
        }
        final int r5 = q4 - q5 * 1000;
        final int q6 = q5 / 1000;
        final int v1 = DIGITS_K[r1];
        final int v2 = DIGITS_K[r2];
        final int v3 = DIGITS_K[r3];
        final int v4 = DIGITS_K[r4];
        final int v5 = DIGITS_K[r5];
        if (q6 == 0) {
            pos += writeFirstBuf(buf, DIGITS_K[q5], pos);
        } else {
            final int r6 = q5 - q6 * 1000;
            buf[pos++] = (byte) (q6 + '0');
            writeBuf(buf, DIGITS_K[r6], pos);
            pos += 3;
        }
        writeBuf(buf, v5, pos);
        writeBuf(buf, v4, pos + 3);
        writeBuf(buf, v3, pos + 6);
        writeBuf(buf, v2, pos + 9);
        writeBuf(buf, v1, pos + 12);
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
            buf[pos++] = MINUS;
        } else {
            i = value;
        }
        final long q1 = i / 1000;
        if (q1 == 0) {
            pos += writeFirstBuf(buf, DIGITS_K[(int) i], pos);
            return pos;
        }
        final int r1 = (int) (i - q1 * 1000);
        final long q2 = q1 / 1000;
        if (q2 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[(int) q1];
            int off = writeFirstBuf(buf, v2, pos);
            writeBuf(buf, v1, pos + off);
            return pos + 3 + off;
        }
        final int r2 = (int) (q1 - q2 * 1000);
        final long q3 = q2 / 1000;
        if (q3 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[r2];
            final int v3 = DIGITS_K[(int) q2];
            pos += writeFirstBuf(buf, v3, pos);
            writeBuf(buf, v2, pos);
            writeBuf(buf, v1, pos + 3);
            return pos + 6;
        }
        final int r3 = (int) (q2 - q3 * 1000);
        final int q4 = (int) (q3 / 1000);
        if (q4 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[r2];
            final int v3 = DIGITS_K[r3];
            final int v4 = DIGITS_K[(int) q3];
            pos += writeFirstBuf(buf, v4, pos);
            writeBuf(buf, v3, pos);
            writeBuf(buf, v2, pos + 3);
            writeBuf(buf, v1, pos + 6);
            return pos + 9;
        }
        final int r4 = (int) (q3 - q4 * 1000);
        final int q5 = q4 / 1000;
        if (q5 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[r2];
            final int v3 = DIGITS_K[r3];
            final int v4 = DIGITS_K[r4];
            final int v5 = DIGITS_K[q4];
            pos += writeFirstBuf(buf, v5, pos);
            writeBuf(buf, v4, pos);
            writeBuf(buf, v3, pos + 3);
            writeBuf(buf, v2, pos + 6);
            writeBuf(buf, v1, pos + 9);
            return pos + 12;
        }
        final int r5 = q4 - q5 * 1000;
        final int q6 = q5 / 1000;
        final int v1 = DIGITS_K[r1];
        final int v2 = DIGITS_K[r2];
        final int v3 = DIGITS_K[r3];
        final int v4 = DIGITS_K[r4];
        final int v5 = DIGITS_K[r5];
        if (q6 == 0) {
            pos += writeFirstBuf(buf, DIGITS_K[q5], pos);
        } else {
            final int r6 = q5 - q6 * 1000;
            buf[pos++] = (char) (byte) (q6 + '0');
            writeBuf(buf, DIGITS_K[r6], pos);
            pos += 3;
        }
        writeBuf(buf, v5, pos);
        writeBuf(buf, v4, pos + 3);
        writeBuf(buf, v3, pos + 6);
        writeBuf(buf, v2, pos + 9);
        writeBuf(buf, v1, pos + 12);
        return pos + 15;
    }

    public static int writeInt32(final byte[] buf, int pos, final int value) {
        int i;
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                for (int x = 0; x < MIN_INT.length; x++) {
                    buf[pos + x] = MIN_INT[x];
                }
                return pos + MIN_INT.length;
            }
            i = -value;
            buf[pos++] = MINUS;
        } else {
            i = value;
        }
        final int q1 = i / 1000;
        if (q1 == 0) {
            pos += writeFirstBuf(buf, DIGITS_K[i], pos);
            return pos;
        }
        final int r1 = i - q1 * 1000;
        final int q2 = q1 / 1000;
        if (q2 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[q1];
            int off = writeFirstBuf(buf, v2, pos);
            writeBuf(buf, v1, pos + off);
            return pos + 3 + off;
        }
        final int r2 = q1 - q2 * 1000;
        final int q3 = q2 / 1000;
        final int v1 = DIGITS_K[r1];
        final int v2 = DIGITS_K[r2];
        if (q3 == 0) {
            pos += writeFirstBuf(buf, DIGITS_K[q2], pos);
        } else {
            final int r3 = q2 - q3 * 1000;
            buf[pos++] = (byte) (q3 + '0');
            writeBuf(buf, DIGITS_K[r3], pos);
            pos += 3;
        }
        writeBuf(buf, v2, pos);
        writeBuf(buf, v1, pos + 3);
        return pos + 6;
    }

    public static int writeInt32(final char[] buf, int pos, final int value) {
        int i;
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                for (int x = 0; x < MIN_INT.length; x++) {
                    buf[pos + x] = (char) MIN_INT[x];
                }
                return pos + MIN_INT.length;
            }
            i = -value;
            buf[pos++] = MINUS;
        } else {
            i = value;
        }
        final int q1 = i / 1000;
        if (q1 == 0) {
            pos += writeFirstBuf(buf, DIGITS_K[i], pos);
            return pos;
        }
        final int r1 = i - q1 * 1000;
        final int q2 = q1 / 1000;
        if (q2 == 0) {
            final int v1 = DIGITS_K[r1];
            final int v2 = DIGITS_K[q1];
            int off = writeFirstBuf(buf, v2, pos);
            writeBuf(buf, v1, pos + off);
            return pos + 3 + off;
        }
        final int r2 = q1 - q2 * 1000;
        final int q3 = q2 / 1000;
        final int v1 = DIGITS_K[r1];
        final int v2 = DIGITS_K[r2];
        if (q3 == 0) {
            pos += writeFirstBuf(buf, DIGITS_K[q2], pos);
        } else {
            final int r3 = q2 - q3 * 1000;
            buf[pos++] = (char) (byte) (q3 + '0');
            writeBuf(buf, DIGITS_K[r3], pos);
            pos += 3;
        }
        writeBuf(buf, v2, pos);
        writeBuf(buf, v1, pos + 3);
        return pos + 6;
    }

    private static int writeFirstBuf(final byte[] buf, final int v, int pos) {
        final int start = v >> 24;
        if (start == 0) {
            buf[pos++] = (byte) (v >> 16);
            buf[pos++] = (byte) (v >> 8);
        } else if (start == 1) {
            buf[pos++] = (byte) (v >> 8);
        }
        buf[pos] = (byte) v;
        return 3 - start;
    }

    private static int writeFirstBuf(final char[] buf, final int v, int pos) {
        final int start = v >> 24;
        if (start == 0) {
            buf[pos++] = (char) (byte) (v >> 16);
            buf[pos++] = (char) (byte) (v >> 8);
        } else if (start == 1) {
            buf[pos++] = (char) (byte) (v >> 8);
        }
        buf[pos] = (char) (byte) v;
        return 3 - start;
    }

    private static void writeBuf(final byte[] buf, final int v, int pos) {
        buf[pos] = (byte) (v >> 16);
        buf[pos + 1] = (byte) (v >> 8);
        buf[pos + 2] = (byte) v;
    }

    private static void writeBuf(final char[] buf, final int v, int pos) {
        buf[pos] = (char) (byte) (v >> 16);
        buf[pos + 1] = (char) (byte) (v >> 8);
        buf[pos + 2] = (char) (byte) v;
    }
}
