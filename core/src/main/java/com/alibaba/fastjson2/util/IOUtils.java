package com.alibaba.fastjson2.util;

import java.io.Closeable;
import java.time.ZoneId;
import java.time.zone.ZoneRules;

public class IOUtils {
    public static final byte[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static final byte[] DigitTens = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1',
            '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3',
            '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5',
            '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'};

    public static final byte[] DigitOnes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

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
            buf[--p] = digits[r];
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
            buf[--p] = (char) digits[r];
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
            buf[--charPos] = digits[r];
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
            buf[--charPos] = (char) digits[r];
            i2 = q2;
            if (i2 == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
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

    public static final int OFFSET_0800_TOTAL_SECONDS = 28800;
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    public static final String SHANGHAI_ZONE_ID_NAME = "Asia/Shanghai";
    public static final ZoneId SHANGHAI_ZONE_ID = SHANGHAI_ZONE_ID_NAME.equals(DEFAULT_ZONE_ID.getId()) ? DEFAULT_ZONE_ID : ZoneId.of(SHANGHAI_ZONE_ID_NAME);
    public static final ZoneRules SHANGHAI_ZONE_RULES = SHANGHAI_ZONE_ID.getRules();

    public static int getShanghaiZoneOffsetTotalSeconds(long seconds) {
        long SECONDS_1991_09_15_02 = 684900000; // utcMillis(1991, 9, 15, 2, 0, 0);
        long SECONDS_1991_04_14_03 = 671598000; // utcMillis(1991, 4, 14, 3, 0, 0);
        long SECONDS_1990_09_16_02 = 653450400; // utcMillis(1990, 9, 16, 2, 0, 0);
        long SECONDS_1990_04_15_03 = 640148400; // utcMillis(1990, 4, 15, 3, 0, 0);
        long SECONDS_1989_09_17_02 = 622000800; // utcMillis(1989, 9, 17, 2, 0, 0);
        long SECONDS_1989_04_16_03 = 608698800; // utcMillis(1989, 4, 16, 3, 0, 0);
        long SECONDS_1988_09_11_02 = 589946400; // utcMillis(1988, 9, 11, 2, 0, 0);
        long SECONDS_1988_04_17_03 = 577249200; // utcMillis(1988, 4, 17, 3, 0, 0);
        long SECONDS_1987_09_13_02 = 558496800; // utcMillis(1987, 9, 13, 2, 0, 0);
        long SECONDS_1987_04_12_03 = 545194800; // utcMillis(1987, 4, 12, 3, 0, 0);
        long SECONDS_1986_09_14_02 = 527047200; // utcMillis(1986, 9, 14, 2, 0, 0);
        long SECONDS_1986_05_04_03 = 515559600; // utcMillis(1986, 5, 4, 3, 0, 0);
        long SECONDS_1949_05_28_00 = -649987200; // utcMillis(1949, 5, 28, 0, 0, 0);
        long SECONDS_1949_05_01_01 = -652316400; // utcMillis(1949, 5, 1, 1, 0, 0);
        long SECONDS_1948_10_01_00 = -670636800; // utcMillis(1948, 10, 1, 0, 0, 0);
        long SECONDS_1948_05_01_01 = -683852400; // utcMillis(1948, 5, 1, 1, 0, 0);
        long SECONDS_1947_11_01_00 = -699580800; // utcMillis(1947, 11, 1, 0, 0, 0);
        long SECONDS_1947_04_15_01 = -716857200; // utcMillis(1947, 4, 15, 1, 0, 0);
        long SECONDS_1946_10_01_00 = -733795200; // utcMillis(1946, 10, 1, 0, 0, 0);
        long SECONDS_1946_05_15_01 = -745801200; // utcMillis(1946, 5, 15, 1, 0, 0);
        long SECONDS_1945_09_02_00 = -767836800; // utcMillis(1945, 9, 2, 0, 0, 0);
        long SECONDS_1942_01_31_01 = -881017200; // utcMillis(1942, 1, 31, 1, 0, 0);
        long SECONDS_1941_11_02_00 = -888796800; // utcMillis(1941, 11, 2, 0, 0, 0);
        long SECONDS_1941_03_15_01 = -908838000; // utcMillis(1941, 3, 15, 1, 0, 0);
        long SECONDS_1940_10_13_00 = -922060800; // utcMillis(1940, 10, 13, 0, 0, 0);
        long SECONDS_1940_06_01_01 = -933634800L; //utcMillis(1940, 6, 1, 1, 0, 0);
        long SECONDS_1919_10_01_00 = -1585872000L; // utcMillis(1919, 10, 1, 0, 0, 0);
        long SECONDS_1919_04_13_01 = -1600642800L; // utcMillis(1919, 4, 13, 1, 0, 0);
        long SECONDS_1901_01_01_00 = -2177452800L; // utcMillis(1901, 1, 1, 0, 0, 0);

        final int OFFSET_0900_TOTAL_SECONDS = 32400;
        final int OFFSET_0800_TOTAL_SECONDS = 28800;
        final int OFFSET_0543_TOTAL_SECONDS = 29143;

        int zoneOffsetTotalSeconds;
        if (seconds >= SECONDS_1991_09_15_02) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1991_04_14_03) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1990_09_16_02) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1990_04_15_03) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1989_09_17_02) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1989_04_16_03) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1988_09_11_02) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1988_04_17_03) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1987_09_13_02) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1987_04_12_03) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1986_09_14_02) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1986_05_04_03) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1949_05_28_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1949_05_01_01) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1948_10_01_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1948_05_01_01) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1947_11_01_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1947_04_15_01) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1946_10_01_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1946_05_15_01) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1945_09_02_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1942_01_31_01) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1941_11_02_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1941_03_15_01) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1940_10_13_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1940_06_01_01) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1919_10_01_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1919_04_13_01) {
            zoneOffsetTotalSeconds = OFFSET_0900_TOTAL_SECONDS;
        } else if (seconds >= SECONDS_1901_01_01_00) {
            zoneOffsetTotalSeconds = OFFSET_0800_TOTAL_SECONDS;
        } else {
            zoneOffsetTotalSeconds = OFFSET_0543_TOTAL_SECONDS;
        }

        return zoneOffsetTotalSeconds;
    }
}
