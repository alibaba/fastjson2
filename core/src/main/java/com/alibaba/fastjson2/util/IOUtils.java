package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;

import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.NumberUtils.MULTIPLY_HIGH;

public class IOUtils {
    static final short DOT_ZERO_16 = BIG_ENDIAN ? (short) ('.' << 8 | '0') : (short) ('0' << 8 | '.');
    static final int DOT_ZERO_32 = BIG_ENDIAN ? '.' << 16 | '0' : '0' << 16 | '.';
    static final short ZERO_DOT_16 = BIG_ENDIAN ? (short) ('0' << 8 | '.') : (short) ('.' << 8 | '0');
    static final int ZERO_DOT_32 = BIG_ENDIAN ? '0' << 16 | '.' : '.' << 16 | '0';
    static final short ZERO_ZERO_16 = '0' << 8 | '0';
    static final int ZERO_ZERO_32 = '0' << 16 | '0';
    static final int NULL_32 = BIG_ENDIAN ? 0x6e756c6c : 0x6c6c756e;
    static final long NULL_64 = BIG_ENDIAN ? 0x6e0075006c006cL : 0x6c006c0075006eL;

    static final int TRUE = BIG_ENDIAN ? 0x74727565 : 0x65757274;
    static final long TRUE_64 = BIG_ENDIAN ? 0x74007200750065L : 0x65007500720074L;

    static final int ALSE = BIG_ENDIAN ? 0x616c7365 : 0x65736c61;
    static final long ALSE_64 = BIG_ENDIAN ? 0x61006c00730065L : 0x650073006c0061L;
    public static final long DOT_X0 = BIG_ENDIAN ? 0x2e00L : 0x2eL;

    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    public static final int[] DIGITS_K_32 = new int[1024];
    public static final long[] DIGITS_K_64 = new long[1024];

    private static final byte[] MIN_LONG_BYTES = "-9223372036854775808".getBytes();
    private static final char[] MIN_LONG_CHARS = "-9223372036854775808".toCharArray();

    public static final short[] PACKED_DIGITS;
    public static final int[] PACKED_DIGITS_UTF16;

    static final long[] POWER_TEN = {
            10,
            100,
            1000,
            10000,
            100000,
            1000000,
            10000000,
            100000000,
            1000000000,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,
            10000000000000000L,
            100000000000000000L,
            1000000000000000000L,
    };

    private static final short ZERO_DOT_LATIN1;

    static {
        short[] shorts = new short[]{
                0x3030, 0x3130, 0x3230, 0x3330, 0x3430, 0x3530, 0x3630, 0x3730, 0x3830, 0x3930,
                0x3031, 0x3131, 0x3231, 0x3331, 0x3431, 0x3531, 0x3631, 0x3731, 0x3831, 0x3931,
                0x3032, 0x3132, 0x3232, 0x3332, 0x3432, 0x3532, 0x3632, 0x3732, 0x3832, 0x3932,
                0x3033, 0x3133, 0x3233, 0x3333, 0x3433, 0x3533, 0x3633, 0x3733, 0x3833, 0x3933,
                0x3034, 0x3134, 0x3234, 0x3334, 0x3434, 0x3534, 0x3634, 0x3734, 0x3834, 0x3934,
                0x3035, 0x3135, 0x3235, 0x3335, 0x3435, 0x3535, 0x3635, 0x3735, 0x3835, 0x3935,
                0x3036, 0x3136, 0x3236, 0x3336, 0x3436, 0x3536, 0x3636, 0x3736, 0x3836, 0x3936,
                0x3037, 0x3137, 0x3237, 0x3337, 0x3437, 0x3537, 0x3637, 0x3737, 0x3837, 0x3937,
                0x3038, 0x3138, 0x3238, 0x3338, 0x3438, 0x3538, 0x3638, 0x3738, 0x3838, 0x3938,
                0x3039, 0x3139, 0x3239, 0x3339, 0x3439, 0x3539, 0x3639, 0x3739, 0x3839, 0x3939,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1
        };
        int[] digits = new int[]{
                0x300030, 0x310030, 0x320030, 0x330030, 0x340030, 0x350030, 0x360030, 0x370030, 0x380030, 0x390030,
                0x300031, 0x310031, 0x320031, 0x330031, 0x340031, 0x350031, 0x360031, 0x370031, 0x380031, 0x390031,
                0x300032, 0x310032, 0x320032, 0x330032, 0x340032, 0x350032, 0x360032, 0x370032, 0x380032, 0x390032,
                0x300033, 0x310033, 0x320033, 0x330033, 0x340033, 0x350033, 0x360033, 0x370033, 0x380033, 0x390033,
                0x300034, 0x310034, 0x320034, 0x330034, 0x340034, 0x350034, 0x360034, 0x370034, 0x380034, 0x390034,
                0x300035, 0x310035, 0x320035, 0x330035, 0x340035, 0x350035, 0x360035, 0x370035, 0x380035, 0x390035,
                0x300036, 0x310036, 0x320036, 0x330036, 0x340036, 0x350036, 0x360036, 0x370036, 0x380036, 0x390036,
                0x300037, 0x310037, 0x320037, 0x330037, 0x340037, 0x350037, 0x360037, 0x370037, 0x380037, 0x390037,
                0x300038, 0x310038, 0x320038, 0x330038, 0x340038, 0x350038, 0x360038, 0x370038, 0x380038, 0x390038,
                0x300039, 0x310039, 0x320039, 0x330039, 0x340039, 0x350039, 0x360039, 0x370039, 0x380039, 0x390039,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1
        };
        PACKED_DIGITS = shorts;
        PACKED_DIGITS_UTF16 = digits;

        for (int i = 0; i < 1000; i++) {
            int c0 = i < 10 ? 2 : i < 100 ? 1 : 0;
            int c1 = (i / 100) + '0';
            int c2 = ((i / 10) % 10) + '0';
            int c3 = i % 10 + '0';
            DIGITS_K_32[i] = c0 + (c1 << 8) + (c2 << 16) + (c3 << 24);
            long v = (c1 << 16) + (((long) c2) << 32) + (((long) c3) << 48);
            DIGITS_K_64[i] = c0 + v;
        }
        ZERO_DOT_LATIN1 = UNSAFE.getShort(new byte[] {'0', '.'}, ARRAY_BYTE_BASE_OFFSET);
    }

    private static short digitPair(int value) {
        return PACKED_DIGITS[value & 0x7f];
    }

    public static void writeDigitPair(byte[] buf, int charPos, int value) {
        putShortLE(
                buf,
                charPos,
                PACKED_DIGITS[value & 0x7f]);
    }

    public static void writeDigitPair(char[] buf, int charPos, int value) {
        putIntLE(
                buf,
                charPos,
                PACKED_DIGITS_UTF16[value & 0x7f]);
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
        int q, r;
        int charPos = index;

        boolean negative = i < 0;
        if (!negative) {
            i = -i;
        }

        // Generate two digits per iteration
        while (i <= -100) {
            q = i / 100;
            r = (q * 100) - i;
            i = q;
            charPos -= 2;
            writeDigitPair(buf, charPos, r);
        }

        // We know there are at most two digits left at this point.
        if (i < -9) {
            charPos -= 2;
            writeDigitPair(buf, charPos, -i);
        } else {
            putByte(buf, --charPos, (byte) ('0' - i));
        }

        if (negative) {
            putByte(buf, charPos - 1, (byte) '-');
        }
    }

    public static void getChars(int i, int index, char[] buf) {
        int q, r;
        int charPos = index;

        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }

        // Get 2 digits/iteration using ints
        while (i <= -100) {
            q = i / 100;
            r = (q * 100) - i;
            i = q;

            charPos -= 2;
            writeDigitPair(buf, charPos, r);
        }

        // We know there are at most two digits left at this point.
        if (i < -9) {
            charPos -= 2;
            writeDigitPair(buf, charPos, -i);
        } else {
            putChar(buf, --charPos, (char) ('0' - i));
        }

        if (negative) {
            putChar(buf, charPos - 1, '-');
        }
    }

    public static void getChars(long i, int index, byte[] buf) {
        long q;
        int charPos = index;

        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i <= Integer.MIN_VALUE) {
            q = i / 100;
            charPos -= 2;
            writeDigitPair(buf, charPos, (int) ((q * 100) - i));
            i = q;
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 <= -100) {
            q2 = i2 / 100;
            charPos -= 2;

            writeDigitPair(buf, charPos, (q2 * 100) - i2);
            i2 = q2;
        }

        // We know there are at most two digits left at this point.
        if (i2 < -9) {
            charPos -= 2;
            writeDigitPair(buf, charPos, -i2);
        } else {
            putByte(buf, --charPos, (byte) ('0' - i2));
        }

        if (negative) {
            putByte(buf, charPos - 1, (byte) '-');
        }
    }

    public static void getChars(long i, int index, char[] buf) {
        long q;
        int charPos = index;

        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i <= Integer.MIN_VALUE) {
            q = i / 100;
            charPos -= 2;
            writeDigitPair(buf, charPos, (int) ((q * 100) - i));
            i = q;
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 <= -100) {
            q2 = i2 / 100;
            charPos -= 2;
            writeDigitPair(buf, charPos, (q2 * 100) - i2);
            i2 = q2;
        }

        // We know there are at most two digits left at this point.
        if (i2 < -9) {
            charPos -= 2;
            writeDigitPair(buf, charPos, -i2);
        } else {
            putChar(buf, --charPos, (char) ('0' - i2));
        }

        if (negative) {
            putChar(buf, charPos - 1, '-');
        }
    }

    /**
     * Writes a decimal number to a byte array buffer
     *
     * @param buf byte array buffer
     * @param off buffer starting offset
     * @param unscaledVal unscaled value (precision part of BigDecimal)
     * @param scale number of digits after the decimal point, caller must ensure scale >= 0
     * @return offset after writing
     *
     * Note: This method trusts that the caller has ensured scale >= 0
     */
    public static int writeDecimal(byte[] buf, int off, long unscaledVal, int scale) {
        if (unscaledVal < 0) {
            putByte(buf, off++, (byte) '-');
            unscaledVal = -unscaledVal;
        }

        if (scale != 0) {
            int unscaleValSize = IOUtils.stringSize(unscaledVal);
            int insertionPoint = unscaleValSize - scale;
            if (insertionPoint == 0) {
                putShortUnaligned(buf, off, ZERO_DOT_LATIN1);
                off += 2;
            } else if (insertionPoint < 0) {
                putShortUnaligned(buf, off, ZERO_DOT_LATIN1);
                off += 2;

                for (int i = 0; i < -insertionPoint; i++) {
                    putByte(buf, off++, (byte) '0');
                }
            } else {
                long power = POWER_TEN[scale - 1];
                long div = unscaledVal / power;
                long rem = unscaledVal - div * power;
                off = IOUtils.writeInt64(buf, off, div);
                putByte(buf, off, (byte) '.');

                if (scale == 1) {
                    putByte(buf, off + 1, (byte) (rem + '0'));
                    return off + 2;
                } else if (scale == 2) {
                    writeDigitPair(buf, off + 1, (int) rem);
                    return off + 3;
                }

                for (int i = 0, end = unscaleValSize - stringSize(rem) - insertionPoint; i < end; ++i) {
                    putByte(buf, ++off, (byte) '0');
                }
                return IOUtils.writeInt64(buf, off + 1, rem);
            }
        }

        return IOUtils.writeInt64(buf, off, unscaledVal);
    }

    /**
     * Writes a decimal number to a character array buffer
     *
     * @param buf character array buffer
     * @param off buffer starting offset
     * @param unscaledVal unscaled value (precision part of BigDecimal)
     * @param scale number of digits after the decimal point, caller must ensure scale >= 0
     * @return offset after writing
     *
     * Note: This method trusts that the caller has ensured scale >= 0
     */
    public static int writeDecimal(char[] buf, int off, long unscaledVal, int scale) {
        if (unscaledVal < 0) {
            putChar(buf, off++, '-');
            unscaledVal = -unscaledVal;
        }

        if (scale != 0) {
            int unscaleValSize = stringSize(unscaledVal);
            int insertionPoint = unscaleValSize - scale;
            if (insertionPoint == 0) {
                buf[off++] = '0';
                buf[off++] = '.';
            } else if (insertionPoint < 0) {
                buf[off++] = '0';
                buf[off++] = '.';

                for (int i = 0; i < -insertionPoint; i++) {
                    putChar(buf, off++, '0');
                }
            } else {
                long power = POWER_TEN[scale - 1];
                long div = unscaledVal / power;
                long rem = unscaledVal - div * power;
                off = IOUtils.writeInt64(buf, off, div);
                putChar(buf, off, '.');

                if (scale == 1) {
                    putChar(buf, off + 1, (char) (rem + '0'));
                    return off + 2;
                } else if (scale == 2) {
                    writeDigitPair(buf, off + 1, (int) rem);
                    return off + 3;
                }

                for (int i = 0, end = unscaleValSize - stringSize(rem) - insertionPoint; i < end; ++i) {
                    putChar(buf, ++off, '0');
                }
                return IOUtils.writeInt64(buf, off + 1, rem);
            }
        }

        return IOUtils.writeInt64(buf, off, unscaledVal);
    }

    public static int encodeUTF8(byte[] src, int offset, int len, byte[] dst, int dp) {
        int sl = offset + len;
        while (offset < sl) {
            char c = UNSAFE.getChar(src, ARRAY_BYTE_BASE_OFFSET + offset);
            offset += 2;

            if (c < 0x80) {
                dst[dp++] = (byte) c;
            } else {
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    dst[dp] = (byte) (0xc0 | (c >> 6));
                    dst[dp + 1] = (byte) (0x80 | (c & 0x3f));
                    dp += 2;
                } else if (c >= '\uD800' && c <= '\uDFFF') {
                    utf8_char2(src, offset, sl, c, dst, dp);
                    offset += 2;
                    dp += 4;
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
            } else if (c >= '\uD800' && c <= '\uDFFF') {
                utf8_char2(src, offset, sl, c, dst, dp);
                offset++;
                dp += 4;
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

    private static void utf8_char2(byte[] src, int offset, int sl, char c, byte[] dst, int dp) {
        char d;
        if (c > '\uDBFF'
                || sl - offset < 1
                || (d = UNSAFE.getChar(src, ARRAY_BYTE_BASE_OFFSET + offset)) < '\uDC00'
                || d > '\uDFFF'
        ) {
            throw new JSONException("malformed input off : " + offset);
        }

        int uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00');
        dst[dp] = (byte) (0xf0 | ((uc >> 18)));
        dst[dp + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
        dst[dp + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
        dst[dp + 3] = (byte) (0x80 | (uc & 0x3f));
    }

    private static void utf8_char2(char[] src, int offset, int sl, char c, byte[] dst, int dp) {
        char d;
        if (c > '\uDBFF' || sl - offset < 1 || (d = src[offset]) < '\uDC00' || d > '\uDFFF') {
            throw new JSONException("malformed input off : " + offset);
        }

        int uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00');
        dst[dp] = (byte) (0xf0 | ((uc >> 18)));
        dst[dp + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
        dst[dp + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
        dst[dp + 3] = (byte) (0x80 | (uc & 0x3f));
    }

    public static boolean isNumber(String str) {
        int len = str.length();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch == '+' || ch == '-') {
                if (i != 0 || len == 1) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(char[] buf, int off, int len) {
        if (len <= 0) {
            return false;
        }
        for (int i = off, end = off + len; i < end; ++i) {
            char ch = buf[i];
            if (ch == '+' || ch == '-') {
                if (i != off || len == 1) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(byte[] buf, int off, int len) {
        if (len <= 0) {
            return false;
        }
        for (int i = off, end = off + len; i < end; ++i) {
            char ch = (char) buf[i];
            if (ch == '+' || ch == '-') {
                if (i != off || len == 1) {
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
            int i = 0;
            long address = ARRAY_BYTE_BASE_OFFSET;
            int upperBound = (len & ~7);
            while (i < upperBound && notContains(UNSAFE.getLong(buf, address), 0x0A0A0A0A0A0A0A0AL)) {
                i += 8;
                address += 8;
            }
            for (; i < len; i++) {
                byte b = buf[i];
                if (b == '\n') {
                    lines++;
                }
            }
        }
        return lines;
    }

    public static int writeLocalDate(byte[] buf, int off, int year, int month, int dayOfMonth) {
        if (year < 0) {
            buf[off++] = '-';
            year = -year;
        } else if (year > 9999) {
            buf[off++] = '+';
        }
        int y01 = (int) (year * 1374389535L >> 37); //year / 100;
        int y23 = year - y01 * 100;

        if (year >= 0 && year < 10000) {
            writeDigitPair(buf, off, y01);
            off += 2;
        } else {
            off = IOUtils.writeInt32(buf, off, y01);
        }

        putLongLE(buf, off,
                0x2d00002d0000L
                        | digitPair(y23)
                        | ((long) digitPair(month) << 24)
                        | ((long) digitPair(dayOfMonth) << 48));
        return off + 8;
    }

    public static int writeLocalDate(char[] buf, int off, int year, int month, int dayOfMonth) {
        if (year < 0) {
            buf[off++] = '-';
            year = -year;
        } else if (year > 9999) {
            buf[off++] = '+';
        }
        int y01 = (int) (year * 1374389535L >> 37); // year / 100;
        int y23 = year - y01 * 100;

        if (year >= 0 && year < 10000) {
            writeDigitPair(buf, off, y01);
            off += 2;
        } else {
            off = IOUtils.writeInt32(buf, off, y01);
        }

        int p1 = PACKED_DIGITS_UTF16[month & 0x7f];
        putLongLE(buf, off,
                ((long) (p1 & 0xFFFF) << 48) | ((long) '-' << 32) | PACKED_DIGITS_UTF16[y23 & 0x7f]);
        putLongLE(buf, off + 4,
                ((long) (p1 & 0xFFFF0000) >> 16) | ((long) '-' << 16) | ((long) PACKED_DIGITS_UTF16[dayOfMonth & 0x7f] << 32));
        return off + 8;
    }

    public static void writeLocalTime(byte[] buf, int off, int hour, int minute, int second) {
        putLongLE(
                buf,
                off,
                0x3a00003a0000L
                        | digitPair(hour)
                        | ((long) digitPair(minute) << 24)
                        | ((long) digitPair(second) << 48));
    }

    public static int writeLocalTime(byte[] buf, int off, LocalTime time) {
        writeLocalTime(buf, off, time.getHour(), time.getMinute(), time.getSecond());
        off += 8;
        int nano = time.getNano();
        return nano != 0 ? writeNano(buf, off, nano) : off;
    }

    public static int writeNano(byte[] buf, int off, int nano) {
        final int div = (int) (nano * 274877907L >> 38); //nano / 1000;
        final int div2 = (int) (div * 274877907L >> 38); // div / 1000;
        final int rem1 = nano - div * 1000;

        putIntLE(buf, off, DIGITS_K_32[div2 & 0x3ff] & 0xffffff00 | '.');
        off += 4;

        int v;
        if (rem1 == 0) {
            final int rem2 = div - div2 * 1000;
            if (rem2 == 0) {
                return off;
            }

            v = DIGITS_K_32[rem2 & 0x3ff];
        } else {
            v = DIGITS_K_32[(div - div2 * 1000) & 0x3ff];
        }

        putShortLE(buf, off, (short) (v >> 8));
        off += 2;
        if (rem1 == 0) {
            putByte(buf, off, (byte) (v >> 24));
            return off + 1;
        }

        putIntLE(buf, off, DIGITS_K_32[rem1] & 0xffffff00 | (v >> 24));
        return off + 4;
    }

    public static int writeNano(char[] buf, int off, int nano) {
        final int div = (int) (nano * 274877907L >> 38); //nano / 1000;
        final int div2 = (int) (div * 274877907L >> 38); // div / 1000;
        final int rem1 = nano - div * 1000;

        putLongLE(buf, off, DIGITS_K_64[div2 & 0x3ff] & 0xffffffffffff0000L | DOT_X0);
        off += 4;

        long v;
        if (rem1 == 0) {
            final int rem2 = div - div2 * 1000;
            if (rem2 == 0) {
                return off;
            }

            v = DIGITS_K_64[rem2 & 0x3ff];
        } else {
            v = DIGITS_K_64[(div - div2 * 1000) & 0x3ff];
        }

        putIntLE(buf, off, (int) (v >> 16));
        off += 2;
        if (rem1 == 0) {
            putChar(buf, off, (char) (v >> 48));
            return off + 1;
        }

        putLongLE(buf, off, DIGITS_K_64[rem1 & 0x3ff] & 0xffffffffffff0000L | (v >> 48));
        return off + 4;
    }

    public static void writeLocalTime(char[] buf, int off, int hour, int minute, int second) {
        writeDigitPair(buf, off, hour);
        putChar(buf, off + 2, ':');
        writeDigitPair(buf, off + 3, minute);
        putChar(buf, off + 5, ':');
        writeDigitPair(buf, off + 6, second);
    }

    public static int writeLocalTime(char[] buf, int off, LocalTime time) {
        writeLocalTime(buf, off, time.getHour(), time.getMinute(), time.getSecond());
        off += 8;

        int nano = time.getNano();
        return nano != 0 ? writeNano(buf, off, nano) : off;
    }

    private static int writeInt4(byte[] buf, int off, int v) {
        int v1 = (int) (v * 1374389535L >> 37); // v / 100;
        int v0 = v - v1 * 100;
        int v2 = PACKED_DIGITS[v1 & 0x7f] | (PACKED_DIGITS[v0 & 0x7f] << 16);
        if (BIG_ENDIAN) {
            v2 = Integer.reverseBytes(v2);
        }
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + off, v2);
        return off + 4;
    }

    private static int writeInt4(char[] buf, int off, int v) {
        int v1 = (int) (v * 1374389535L >> 37); // v / 100;
        putLongUnaligned(buf, off, mergeInt64(v - v1 * 100, v1));
        return off + 4;
    }

    private static long mergeInt64(int v1, int v2) {
        long v = PACKED_DIGITS_UTF16[v2 & 0x7f] | ((long) PACKED_DIGITS_UTF16[v1 & 0x7f] << 32);
        if (BIG_ENDIAN) {
            v = Long.reverseBytes(v);
        }
        return v;
    }

    private static int writeInt3(byte[] buf, int off, int val) {
        int v = DIGITS_K_32[val & 0x3ff];
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + off, v >> ((((byte) v) + 1) << 3));
        return off + 3 - (byte) v;
    }

    private static int writeInt3(char[] buf, int off, int val) {
        long v = DIGITS_K_64[val & 0x3ff];
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1), v >> ((((short) v) + 1) << 4));
        return off + 3 - (byte) v;
    }

    private static int writeInt8(byte[] buf, int off, int v1, int v2) {
        int r1 = (int) (v1 * 1374389535L >> 37); // v1 / 100;
        int r2 = (int) (v2 * 1374389535L >> 37); // v2 / 100;
        long v = (PACKED_DIGITS[r1 & 0x7f])
                | (PACKED_DIGITS[(v1 - r1 * 100) & 0x7f] << 16)
                | ((long) PACKED_DIGITS[r2 & 0x7f] << 32)
                | ((long) PACKED_DIGITS[(v2 - r2 * 100) & 0x7f] << 48);
        if (BIG_ENDIAN) {
            v = Long.reverseBytes(v);
        }
        UNSAFE.putLong(buf, ARRAY_BYTE_BASE_OFFSET + off, v);
        return off + 8;
    }

    private static int writeInt8(char[] buf, int off, int v1, int v2) {
        int r1 = (int) (v1 * 1374389535L >> 37); // v1 / 100;
        long x1 = (PACKED_DIGITS_UTF16[r1 & 0x7f])
                | ((long) PACKED_DIGITS_UTF16[(v1 - r1 * 100) & 0x7f] << 32);
        int r2 = (int) (v2 * 1374389535L >> 37); // v2 / 100;
        long x2 = (PACKED_DIGITS_UTF16[r2 & 0x7f])
                | ((long) PACKED_DIGITS_UTF16[(v2 - r2 * 100) & 0x7f] << 32);
        if (BIG_ENDIAN) {
            x1 = Long.reverseBytes(x1);
            x2 = Long.reverseBytes(x2);
        }
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1), x1);
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1) + 8, x2);
        return off + 8;
    }

    public static int writeInt64(byte[] buf, int off, long val) {
        if (val < 0) {
            if (val == Long.MIN_VALUE) {
                System.arraycopy(MIN_LONG_BYTES, 0, buf, off, MIN_LONG_BYTES.length);
                return off + MIN_LONG_BYTES.length;
            }
            val = -val;
            putByte(buf, off++, (byte) ('-'));
        }

        if (val <= Integer.MAX_VALUE) {
            return writeInt32(buf, off, (int) val);
        }

        int v, v1, v2, v3;
        long numValue = val;
        val = MULTIPLY_HIGH.multiplyHigh(numValue, 0x68db8bac710cb296L) >> 12; // val = numValue / 10000;
        v1 = (int) (numValue - val * 10000);
        numValue = val;
        val = MULTIPLY_HIGH.multiplyHigh(numValue, 0x68db8bac710ccL); // val = numValue / 10000;
        v2 = (int) (numValue - val * 10000);
        if (val < 10000) {
            v = (int) val;
            if (v < 1000) {
                off = writeInt3(buf, off, v);
            } else {
                off = writeInt4(buf, off, v);
            }
            return writeInt8(buf, off, v2, v1);
        }

        numValue = val;
        val = MULTIPLY_HIGH.multiplyHigh(numValue, 0x68db8bac710ccL); // val = numValue / 10000;
        v3 = (int) (numValue - val * 10000);
        if (val < 10000) {
            v = (int) val;
            if (v < 1000) {
                off = writeInt3(buf, off, v);
                off = writeInt4(buf, off, v3);
            } else {
                writeInt8(buf, off, v, v3);
                off += 8;
            }
            return writeInt8(buf, off, v2, v1);
        }

        numValue = val;
        val = MULTIPLY_HIGH.multiplyHigh(numValue, 0x68db8bac710ccL); // val = numValue / 10000;
        int v4 = (int) (numValue - val * 10000);
        off = writeInt3(buf, off, (int) val);
        off = writeInt8(buf, off, v4, v3);
        return writeInt8(buf, off, v2, v1);
    }

    public static int writeInt64(char[] buf, int off, long val) {
        if (val < 0) {
            if (val == Long.MIN_VALUE) {
                System.arraycopy(MIN_LONG_CHARS, 0, buf, off, MIN_LONG_CHARS.length);
                return off + MIN_LONG_CHARS.length;
            }
            val = -val;
            putChar(buf, off++, '-');
        }

        if (val <= Integer.MAX_VALUE) {
            return writeInt32(buf, off, (int) val);
        }

        int v, v1, v2, v3;
        long numValue = val;
        val = MULTIPLY_HIGH.multiplyHigh(numValue, 0x68db8bac710cb296L) >> 12; // val = numValue / 10000;
        v1 = (int) (numValue - val * 10000);
        numValue = val;
        val = MULTIPLY_HIGH.multiplyHigh(numValue, 0x68db8bac710ccL); // val = numValue / 10000;
        v2 = (int) (numValue - val * 10000);
        if (val < 10000) {
            v = (int) val;
            if (v < 1000) {
                off = writeInt3(buf, off, v);
            } else {
                off = writeInt4(buf, off, v);
            }
            return writeInt8(buf, off, v2, v1);
        }

        numValue = val;
        val = MULTIPLY_HIGH.multiplyHigh(numValue, 0x68db8bac710ccL); // val = numValue / 10000;
        v3 = (int) (numValue - val * 10000);
        if (val < 10000) {
            v = (int) val;
            if (v < 1000) {
                off = writeInt3(buf, off, v);
                off = writeInt4(buf, off, v3);
            } else {
                writeInt8(buf, off, v, v3);
                off += 8;
            }
            return writeInt8(buf, off, v2, v1);
        }

        numValue = val;
        val = MULTIPLY_HIGH.multiplyHigh(numValue, 0x68db8bac710ccL); // val = numValue / 10000;
        int v4 = (int) (numValue - val * 10000);
        off = writeInt3(buf, off, (int) val);
        off = writeInt8(buf, off, v4, v3);
        return writeInt8(buf, off, v2, v1);
    }

    public static int writeInt8(final byte[] buf, int pos, final byte value) {
        int i;
        if (value < 0) {
            i = -value;
            putByte(buf, pos++, (byte) '-');
        } else {
            i = value;
        }

        int v = DIGITS_K_32[i & 0x3ff];
        final int start = (byte) v;
        if (start == 0) {
            putShortLE(buf, pos, (short) (v >> 8));
            pos += 2;
        } else if (start == 1) {
            putByte(buf, pos++, (byte) (v >> 16));
        }
        putByte(buf, pos, (byte) (v >> 24));
        return pos + 1;
    }

    public static int writeInt8(char[] buf, int pos, final byte value) {
        int i;
        if (value < 0) {
            i = -value;
            putChar(buf, pos++, '-');
        } else {
            i = value;
        }

        long v = DIGITS_K_64[i & 0x3ff];
        final int start = (byte) v;
        if (start == 0) {
            putIntLE(buf, pos, (int) (v >> 16));
            pos += 2;
        } else if (start == 1) {
            putChar(buf, pos++, (char) (v >> 32));
        }
        putChar(buf, pos, (char) (v >> 48));
        return pos + 1;
    }

    public static int writeInt16(byte[] buf, int pos, final short value) {
        int i;
        if (value < 0) {
            i = -value;
            putByte(buf, pos++, (byte) '-');
        } else {
            i = value;
        }

        if (i < 1000) {
            int v = DIGITS_K_32[i & 0x3ff];
            final int start = (byte) v;
            if (start == 0) {
                putShortLE(buf, pos, (short) (v >> 8));
                pos += 2;
            } else if (start == 1) {
                putByte(buf, pos++, (byte) (v >> 16));
            }
            putByte(buf, pos, (byte) (v >> 24));
            return pos + 1;
        }

        final int q1 = (int) (i * 274877907L >> 38); // i / 1000;
        final int v2 = DIGITS_K_32[q1 & 0x3ff];
        if ((byte) v2 == 1) {
            putByte(buf, pos++, (byte) (v2 >> 16));
        }
        putIntLE(buf, pos, (DIGITS_K_32[(i - q1 * 1000) & 0x3ff]) & 0xffffff00 | (v2 >> 24));
        return pos + 4;
    }

    public static int writeInt16(char[] buf, int pos, final short value) {
        int i;
        if (value < 0) {
            i = -value;
            putChar(buf, pos++, '-');
        } else {
            i = value;
        }

        if (i < 1000) {
            long v = DIGITS_K_64[i & 0x3ff];
            final int start = (byte) v;
            if (start == 0) {
                putIntLE(buf, pos, (int) (v >> 16));
                pos += 2;
            } else if (start == 1) {
                putChar(buf, pos++, (char) (v >> 32));
            }
            putChar(buf, pos, (char) (v >> 48));
            return pos + 1;
        }

        final int q1 = (int) (i * 274877907L >> 38); // i / 1000;
        final long v2 = DIGITS_K_64[q1 & 0x3ff];
        if ((byte) v2 == 1) {
            putChar(buf, pos++, (char) (v2 >> 32));
        }
        putLongLE(buf, pos, DIGITS_K_64[(i - q1 * 1000) & 0x3ff] & 0xffffffffffff0000L | (v2 >> 48));
        return pos + 4;
    }

    public static int writeInt32(final byte[] buf, int off, long val) {
        if (val < 0) {
            val = -val;
            putByte(buf, off++, (byte) ('-'));
        }
        int v, v1;
        if (val < 10000) {
            v = (int) val;
            if (v < 1000) {
                off = writeInt3(buf, off, v);
            } else {
                off = writeInt4(buf, off, v);
            }
            return off;
        }
        long numValue = val;
        val = (int) (numValue * 1759218605L >> 44);  // numValue / 10000;
        v1 = (int) (numValue - val * 10000);
        if (val < 10000) {
            v = (int) val;
            if (v < 1000) {
                off = writeInt3(buf, off, v);
                off = writeInt4(buf, off, v1);
            } else {
                off = writeInt8(buf, off, v, v1);
            }
            return off;
        }

        numValue = val;
        val = (int) (numValue * 1759218605L >> 44);  // numValue / 10000;
        off = writeInt3(buf, off, (int) val);
        return writeInt8(buf, off, (int) (numValue - val * 10000), v1);
    }

    public static int writeInt32(final char[] buf, int off, long val) {
        if (val < 0) {
            val = -val;
            putChar(buf, off++, '-');
        }
        int v, v1;
        if (val < 10000) {
            v = (int) val;
            if (v < 1000) {
                off = writeInt3(buf, off, v);
            } else {
                off = writeInt4(buf, off, v);
            }
            return off;
        }
        long numValue = val;
        val = (int) (numValue * 1759218605L >> 44);  // numValue / 10000;
        v1 = (int) (numValue - val * 10000);
        if (val < 10000) {
            v = (int) val;
            if (v < 1000) {
                off = writeInt3(buf, off, v);
                off = writeInt4(buf, off, v1);
            } else {
                off = writeInt8(buf, off, v, v1);
            }
            return off;
        }

        numValue = val;
        val = (int) (numValue * 1759218605L >> 44);  // numValue / 10000;
        off = writeInt3(buf, off, (int) val);
        return writeInt8(buf, off, (int) (numValue - val * 10000), v1);
    }

    public static byte getByte(byte[] buf, int pos) {
        return UNSAFE.getByte(buf, ARRAY_BYTE_BASE_OFFSET + pos);
    }

    public static char getChar(char[] buf, int pos) {
        return UNSAFE.getChar(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1));
    }

    public static char getChar(byte[] buf, int pos) {
        return UNSAFE.getChar(buf, ARRAY_BYTE_BASE_OFFSET + ((long) pos << 1));
    }

    private static void putByte(byte[] buf, int pos, byte v) {
        UNSAFE.putByte(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
    }

    private static void putChar(char[] buf, int pos, char v) {
        UNSAFE.putChar(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
    }

    public static void putShortBE(byte[] buf, int pos, short v) {
        UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(true, v));
    }

    public static void putShortLE(byte[] buf, int pos, short v) {
        UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(false, v));
    }

    public static void putIntBE(byte[] buf, int pos, int v) {
        if (!BIG_ENDIAN) {
            v = Integer.reverseBytes(v);
        }
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
    }

    public static void putIntLE(byte[] buf, int pos, int v) {
        if (BIG_ENDIAN) {
            v = Integer.reverseBytes(v);
        }
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
    }

    public static void putIntLE(char[] buf, int pos, int v) {
        if (BIG_ENDIAN) {
            v = Integer.reverseBytes(v);
        }
        UNSAFE.putInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
    }

    public static void putShortUnaligned(byte[] buf, int pos, short v) {
        UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
    }

    public static void putIntUnaligned(char[] buf, int pos, int v) {
        UNSAFE.putInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
    }

    public static void putIntUnaligned(byte[] buf, int pos, int v) {
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
    }

    public static void putLongLE(char[] buf, int pos, long v) {
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), convEndian(false, v));
    }

    public static void putLongUnaligned(char[] buf, int pos, long v) {
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
    }

    public static void putLongUnaligned(byte[] buf, int pos, long v) {
        UNSAFE.putLong(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
    }

    public static void putLongBE(byte[] buf, int pos, long v) {
        UNSAFE.putLong(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(true, v));
    }

    public static void putLongLE(byte[] buf, int pos, long v) {
        UNSAFE.putLong(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(false, v));
    }

    public static int putBoolean(byte[] buf, int off, boolean v) {
        long address = ARRAY_BYTE_BASE_OFFSET + off;
        if (v) {
            UNSAFE.putInt(buf, address, TRUE);
            return off + 4;
        } else {
            UNSAFE.putByte(buf, address, (byte) 'f');
            UNSAFE.putInt(buf, address + 1, ALSE);
            return off + 5;
        }
    }

    public static int putBoolean(char[] buf, int off, boolean v) {
        long address = ARRAY_CHAR_BASE_OFFSET + ((long) off << 1);
        if (v) {
            UNSAFE.putLong(buf, address, TRUE_64);
            return off + 4;
        } else {
            UNSAFE.putChar(buf, address, 'f');
            UNSAFE.putLong(buf, address + 2, ALSE_64);
            return off + 5;
        }
    }

    public static boolean isALSE(byte[] buf, int pos) {
        return UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + pos) == ALSE;
    }

    public static boolean notALSE(byte[] buf, int pos) {
        return UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + pos) != ALSE;
    }

    public static boolean isALSE(char[] buf, int pos) {
        return getLongUnaligned(buf, pos) == ALSE_64;
    }

    public static boolean notALSE(char[] buf, int pos) {
        return getLongUnaligned(buf, pos) != ALSE_64;
    }

    public static boolean isNULL(byte[] buf, int pos) {
        return UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + pos) == NULL_32;
    }

    public static boolean notNULL(byte[] buf, int pos) {
        return UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + pos) != NULL_32;
    }

    public static boolean notTRUE(byte[] buf, int pos) {
        return UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + pos) != TRUE;
    }

    public static boolean notTRUE(char[] buf, int pos) {
        return UNSAFE.getLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1)) != TRUE_64;
    }

    public static boolean isNULL(char[] buf, int pos) {
        return getLongUnaligned(buf, pos) == NULL_64;
    }

    public static boolean notNULL(char[] buf, int pos) {
        return getLongUnaligned(buf, pos) != NULL_64;
    }

    public static void putNULL(byte[] buf, int pos) {
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + pos, NULL_32);
    }

    public static void putNULL(char[] buf, int pos) {
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), NULL_64);
    }

    public static int digit4(char[] buf, int off) {
        long x = getLongLE(buf, off);
        long d;
        if ((((x & 0xFFF0FFF0FFF0FFF0L) - 0x30003000300030L) | (((d = x & 0x0F000F000F000FL) + 0x06000600060006L) & 0xF000F000F000F0L)) != 0) {
            return -1;
        }
        return (int) ((
                ((d & 0xF) * 10 +
                ((d >> 16) & 0xF)) * 10 +
                ((d >> 32) & 0xF)) * 10 +
                (d >> 48));
    }

    public static int digit4(byte[] buf, int off) {
        return digit4(
                getIntLE(buf, off)
        );
    }

    private static int digit4(int x) {
        /*
            Here we are doing a 4-Byte Vector operation on the Int type.

            x & 0xF0 != 0xC0
            ---------------
            0 0b0011_0000 & 0b1111_0000 = 0b0011_0000
            1 0b0011_0001 & 0b1111_0000 = 0b0011_0000
            2 0b0011_0010 & 0b1111_0000 = 0b0011_0000
            3 0b0011_0011 & 0b1111_0000 = 0b0011_0000
            4 0b0011_0100 & 0b1111_0000 = 0b0011_0000
            5 0b0011_0101 & 0b1111_0000 = 0b0011_0000
            6 0b0011_0110 & 0b1111_0000 = 0b0011_0000
            7 0b0011_0111 & 0b1111_0000 = 0b0011_0000
            8 0b0011_1000 & 0b1111_0000 = 0b0011_0000
            9 0b0011_1001 & 0b1111_0000 = 0b0011_0000

            (((d = x & 0x0F) + 0x06) & 0xF0) != 0
            ---------------
            0 ((0b0011_0000) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            1 ((0b0011_0001) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            2 ((0b0011_0010) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            3 ((0b0011_0011) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            4 ((0b0011_0100) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            5 ((0b0011_0101) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            6 ((0b0011_0110) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            7 ((0b0011_0111) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            8 ((0b0011_1000) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            9 ((0b0011_1001) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
         */
        int d;
        if ((((x & 0xF0F0F0F0) - 0x30303030) | (((d = x & 0x0F0F0F0F) + 0x06060606) & 0xF0F0F0F0)) != 0) {
            return -1;
        }
        return (((d & 0xF) * 10 +
                ((d >> 8) & 0xF)) * 10 +
                ((d >> 16) & 0xF)) * 10 +
                (d >> 24);
    }

    public static int digit3(char[] buf, int off) {
        long x = getIntLE(buf, off) + (((long) getChar(buf, off + 2)) << 32);
        long d;
        if ((((x & 0xFFF0FFF0FFF0L) - 0x3000300030L) | (((d = x & 0x0F000F000FL) + 0x0600060006L) & 0xF000F000F0L)) != 0) {
            return -1;
        }
        return (int) (((d & 0xF) * 10 + ((d >> 16) & 0xF)) * 10 + (d >> 32));
    }

    public static int digit3(byte[] buf, int off) {
        return digit3(
                getShortLE(buf, off) | (getByte(buf, off + 2) << 16)
        );
    }

    private static int digit3(int x) {
        int d;
        if ((((x & 0xF0F0F0) - 0x303030) | (((d = x & 0x0F0F0F) + 0x060606) & 0xF0F0F0)) != 0) {
            return -1;
        }
        return ((d & 0xF) * 10 + ((d >> 8) & 0xF)) * 10 + (d >> 16);
    }

    public static int digit2(char[] buf, int off) {
        int x = UNSAFE.getInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1));
        if (BIG_ENDIAN) {
            x = Integer.reverseBytes(x);
        }
        int d;
        if ((((x & 0xFFF0FFF0) - 0x300030) | (((d = x & 0x0F000F) + 0x060006) & 0xF000F0)) != 0) {
            return -1;
        }
        return (d & 0xF) * 10 + (d >> 16);
    }

    public static int digit2(byte[] buf, int off) {
        short x = UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + off);
        if (BIG_ENDIAN) {
            x = Short.reverseBytes(x);
        }
        int d;
        if ((((x & 0xF0F0) - 0x3030) | (((d = x & 0x0F0F) + 0x0606) & 0xF0F0)) != 0) {
            return -1;
        }
        return (d & 0xF) * 10 + (d >> 8);
    }

    public static boolean isDigit2(byte[] buf, int off) {
        short x = UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + off);
        if (BIG_ENDIAN) {
            x = Short.reverseBytes(x);
        }
        return (((x & 0xF0F0) - 0x3030) | (((x & 0x0F0F) + 0x0606) & 0xF0F0)) == 0;
    }

    public static boolean isDigit2(char[] buf, int off) {
        int x = UNSAFE.getShort(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1));
        if (BIG_ENDIAN) {
            x = Integer.reverseBytes(x);
        }
        return ((((x & 0xFFF0FFF0) - 0x300030) | (((x & 0x0F000F) + 0x060006) & 0xF000F0)) == 0);
    }

    public static int digit(int d) {
        return d >= 0 && d <= 9 ? d : -1;
    }

    public static int digit1(char[] buf, int off) {
        int d = UNSAFE.getByte(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1)) - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public static int digit1(byte[] buf, int off) {
        int d = UNSAFE.getByte(buf, ARRAY_BYTE_BASE_OFFSET + off) - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public static int indexOfQuote(byte[] buf, int quote, int fromIndex, int max) {
        if (INDEX_OF_CHAR_LATIN1 == null) {
            return indexOfQuoteV(buf, quote, fromIndex, max);
        }
        try {
            return (int) INDEX_OF_CHAR_LATIN1.invokeExact(buf, quote, fromIndex, max);
        } catch (Throwable e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static int indexOfQuoteV(byte[] buf, int quote, int fromIndex, int max) {
        int i = fromIndex;
        long address = ARRAY_BYTE_BASE_OFFSET + fromIndex;
        int upperBound = fromIndex + ((max - fromIndex) & ~7);
        long vectorQuote = quote == '\'' ? 0x2727_2727_2727_2727L : 0x2222_2222_2222_2222L;
        while (i < upperBound && notContains(UNSAFE.getLong(buf, address), vectorQuote)) {
            i += 8;
            address += 8;
        }
        return indexOfChar(buf, quote, i, max);
    }

    public static int indexOfDoubleQuote(byte[] buf, int fromIndex, int max) {
        if (INDEX_OF_CHAR_LATIN1 == null) {
            return indexOfDoubleQuoteV(buf, fromIndex, max);
        }
        try {
            return (int) INDEX_OF_CHAR_LATIN1.invokeExact(buf, (int) '"', fromIndex, max);
        } catch (Throwable e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static int indexOfDoubleQuoteV(byte[] buf, int fromIndex, int max) {
        int i = fromIndex;
        long address = ARRAY_BYTE_BASE_OFFSET + fromIndex;
        int upperBound = fromIndex + ((max - fromIndex) & ~7);
        while (i < upperBound && notContains(UNSAFE.getLong(buf, address), 0x2222_2222_2222_2222L)) {
            i += 8;
            address += 8;
        }
        return indexOfChar(buf, '"', i, max);
    }

    public static int indexOfLineSeparator(byte[] buf, int fromIndex, int max) {
        if (INDEX_OF_CHAR_LATIN1 == null) {
            return indexOfLineSeparatorV(buf, fromIndex, max);
        }
        try {
            return (int) INDEX_OF_CHAR_LATIN1.invokeExact(buf, (int) '\n', fromIndex, max);
        } catch (Throwable e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static int indexOfLineSeparatorV(byte[] buf, int fromIndex, int max) {
        int i = fromIndex;
        long address = ARRAY_BYTE_BASE_OFFSET + fromIndex;
        int upperBound = fromIndex + ((max - fromIndex) & ~7);
        while (i < upperBound && notContains(UNSAFE.getLong(buf, address), 0x0A0A0A0A0A0A0A0AL)) {
            i += 8;
            address += 8;
        }
        return indexOfChar(buf, '\n', i, max);
    }

    public static int indexOfSlash(byte[] buf, int fromIndex, int max) {
        if (INDEX_OF_CHAR_LATIN1 == null) {
            return indexOfSlashV(buf, fromIndex, max);
        }
        try {
            return (int) INDEX_OF_CHAR_LATIN1.invokeExact(buf, (int) '\\', fromIndex, max);
        } catch (Throwable e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static int indexOfSlashV(byte[] buf, int fromIndex, int max) {
        int i = fromIndex;
        long address = ARRAY_BYTE_BASE_OFFSET + fromIndex;
        int upperBound = fromIndex + ((max - fromIndex) & ~7);
        while (i < upperBound && notContains(UNSAFE.getLong(buf, address), 0x5C5C5C5C5C5C5C5CL)) {
            i += 8;
            address += 8;
        }
        return indexOfChar(buf, '\\', i, max);
    }

    public static boolean regionMatches(byte[] bytes, int off, String prefix) {
        int len = prefix.length();
        if (off + len >= bytes.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (bytes[off + i] != prefix.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static int indexOfChar(byte[] buf, int ch, int fromIndex, int max) {
        for (int i = fromIndex; i < max; i++) {
            if (buf[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOfChar(char[] buf, int ch, int fromIndex, int max) {
        for (int i = fromIndex; i < max; i++) {
            if (buf[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    private static boolean notContains(long value, long vector) {
        /*
          for (int i = 0; i < 8; ++i) {
            byte c = (byte) v;
            if (c == quote) {
                return true;
            }
            v >>>= 8;
          }
          return false;
         */
        long x = value ^ vector;
        return (((x - 0x0101010101010101L) & ~x) & 0x8080808080808080L) == 0;
    }

    public static int hexDigit4(byte[] buf, int offset) {
        int v = getIntLE(buf, offset);
        v = (v & 0x0F0F0F0F) + ((((v & 0x40404040) >> 2) | ((v & 0x40404040) << 1)) >>> 4);
        return ((v & 0xF000000) >>> 24) + ((v & 0xF0000) >>> 12) + (v & 0xF00) + ((v & 0xF) << 12);
    }

    public static int hexDigit4(char[] buf, int offset) {
        long v = getLongLE(buf, offset);
        v = (v & 0x000F_000F_000F_000FL) + ((((v & 0x0004_0004_0004_00040L) >> 2) | ((v & 0x0004_0004_0004_00040L) << 1)) >>> 4);
        return (int) (((v & 0xF_0000_0000_0000L) >>> 48) + ((v & 0xF_0000_0000L) >>> 28) + ((v & 0xF_0000) >> 8) + ((v & 0xF) << 12));
    }

    public static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    public static short getShortUnaligned(byte[] buf, int offset) {
        return UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + offset);
    }

    public static short getShortBE(byte[] buf, int offset) {
        return convEndian(true,
                UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public static short getShortLE(byte[] buf, int offset) {
        return convEndian(false,
                UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public static boolean isUTF8BOM(byte[] buf, int off) {
        // EF BB BF
        return ((getIntLE(buf, off)) & 0xFFFFFF) == 0xBFBBEF;
    }

    public static int getIntBE(byte[] buf, int offset) {
        int v = UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + offset);
        if (!BIG_ENDIAN) {
            v = Integer.reverseBytes(v);
        }
        return v;
    }

    public static int getIntLE(byte[] buf, int offset) {
        int v = UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + offset);
        if (BIG_ENDIAN) {
            v = Integer.reverseBytes(v);
        }
        return v;
    }

    public static int getIntLE(char[] buf, int offset) {
        int v = UNSAFE.getInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1));
        if (BIG_ENDIAN) {
            v = Integer.reverseBytes(v);
        }
        return v;
    }

    public static int getIntUnaligned(byte[] buf, int offset) {
        return UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + offset);
    }

    public static int getIntUnaligned(char[] buf, int offset) {
        return UNSAFE.getInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1));
    }

    public static long getLongBE(byte[] buf, int offset) {
        long v = UNSAFE.getLong(buf, ARRAY_BYTE_BASE_OFFSET + offset);
        if (!BIG_ENDIAN) {
            v = Long.reverseBytes(v);
        }
        return v;
    }

    public static long getLongUnaligned(byte[] buf, int offset) {
        return UNSAFE.getLong(buf, ARRAY_BYTE_BASE_OFFSET + offset);
    }

    public static long getLongUnaligned(char[] buf, int offset) {
        return UNSAFE.getLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1));
    }

    public static long getLongLE(byte[] buf, int offset) {
        return convEndian(false,
                UNSAFE.getLong(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public static long getLongLE(char[] buf, int offset) {
        long v = UNSAFE.getLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1));
        if (BIG_ENDIAN) {
            v = Long.reverseBytes(v);
        }
        return v;
    }

    public static short hex2(int i) {
        i = ((i & 0xF0) >> 4) | ((i & 0xF) << 8);
        int m = (i + 0x06060606) & 0x10101010;
        return (short) (((m << 1) + (m >> 1) - (m >> 4))
                + 0x30303030 + i);
    }

    public static short hex2U(int i) {
        i = ((i & 0xF0) >> 4) | ((i & 0xF) << 8);
        int m = (i + 0x06060606) & 0x10101010;
        return (short) (((m >> 1) - (m >> 4))
                + 0x30303030 + i);
    }

    public static int utf16Hex2(int i) {
        // 0x000F000F
        i = ((i & 0xF0) >> 4) | ((i & 0xF) << 16);
        int m = (i + 0x00060006) & 0x00100010;
        return ((m << 1) + (m >> 1) - (m >> 4))
                + 0x00300030 + i;
    }

    public static int hex4U(int i) {
        i = reverseBytesExpand(i);
        /*
            0  = 0b0000_0000 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 0
            1  = 0b0000_0001 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 1
            2  = 0b0000_0010 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 2
            3  = 0b0000_0011 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 3
            4  = 0b0000_0100 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 4
            5  = 0b0000_0101 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 5
            6  = 0b0000_0110 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 6
            7  = 0b0000_0111 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 7
            8  = 0b0000_1000 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 8
            9  = 0b0000_1001 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 9
            10 = 0b0000_1010 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => A
            11 = 0b0000_1011 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => B
            12 = 0b0000_1100 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => C
            13 = 0b0000_1101 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => D
            14 = 0b0000_1110 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => E
            15 = 0b0000_1111 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => F
         */
        int m = (i + 0x06060606) & 0x10101010;
        return ((m * 7) >> 4) + 0x30303030 + i;
    }

    public static long utf16Hex4U(long i) {
        i = utf16ReverseBytesExpand(i);
        /*
            0  = 0b0000_0000 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 0
            1  = 0b0000_0001 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 1
            2  = 0b0000_0010 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 2
            3  = 0b0000_0011 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 3
            4  = 0b0000_0100 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 4
            5  = 0b0000_0101 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 5
            6  = 0b0000_0110 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 6
            7  = 0b0000_0111 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 7
            8  = 0b0000_1000 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 8
            9  = 0b0000_1001 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 0 + 0x30 + (x & 0xF) => 9
            10 = 0b0000_1010 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => A
            11 = 0b0000_1011 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => B
            12 = 0b0000_1100 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => C
            13 = 0b0000_1101 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => D
            14 = 0b0000_1110 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => E
            15 = 0b0000_1111 => m = ((x + 6) & 0x10); (m >> 1) - (m >> 4) => 7 + 0x30 + (x & 0xF) => F
         */
        long m = (i + 0x00060006_00060006L) & 0x00100010_00100010L;
        return ((m >> 1) - (m >> 4))
                + 0x00300030_00300030L
                + i;
    }

    private static int reverseBytesExpand(int i) {
        // i = Integer.reverseBytes(Integer.expand(i, 0xF0F0F0F0));
        return ((i & 0xF000) >> 12) | (i & 0xF00) | ((i & 0xF0) << 12) | ((i & 0xF) << 24);
    }

    private static long utf16ReverseBytesExpand(long i) {
        // i = Long.reverseBytes(Long.expand(i, 0x00F000F0_00F000F0));
        return ((i & 0xF000L) >> 12) | ((i & 0xF00L) << 8) | ((i & 0xF0L) << 28) | ((i & 0xFL) << 48);
    }

    public static int convEndian(boolean big, int n) {
        return big == BIG_ENDIAN ? n : Integer.reverseBytes(n);
    }

    public static long convEndian(boolean big, long n) {
        return big == BIG_ENDIAN ? n : Long.reverseBytes(n);
    }

    static short convEndian(boolean big, short n) {
        return big == BIG_ENDIAN ? n : Short.reverseBytes(n);
    }

    public static boolean isLatin1(char[] buf, int off, int len) {
        int end = off + len;
        int upperBound = off + (len & ~7);
        long address = ARRAY_CHAR_BASE_OFFSET + ((long) off << 1);
        while (off < upperBound
                && (convEndian(false, UNSAFE.getLong(buf, address) | UNSAFE.getLong(buf, address + 8)) & 0xFF00FF00FF00FF00L) == 0
        ) {
            address += 16;
            off += 8;
        }
        while (off++ < end) {
            if ((convEndian(false, UNSAFE.getShort(buf, address)) & 0xFF00) != 0) {
                return false;
            }
            address += 2;
        }
        return true;
    }

    public static boolean isASCII(String str) {
        if (STRING_VALUE != null && STRING_CODER != null) {
            return STRING_CODER.applyAsInt(str) == 0 && isASCII(STRING_VALUE.apply(str));
        }
        for (int i = 0, len = str.length(); i < len; ++i) {
            if (str.charAt(i) > 0x7F) {
                return false;
            }
        }
        return true;
    }

    public static boolean isASCII(byte[] buf) {
        return isASCII(buf, 0, buf.length);
    }

    public static boolean isASCII(byte[] buf, int off, int len) {
        int end = off + len;
        int upperBound = off + (len & ~7);
        long address = ARRAY_BYTE_BASE_OFFSET + off;
        while (off < upperBound && (UNSAFE.getLong(buf, address) & 0x8080808080808080L) == 0) {
            address += 8;
            off += 8;
        }

        while (off++ < end) {
            if ((UNSAFE.getByte(buf, address++) & 0x80) != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNonSlashASCII(byte[] buf, int off, int len) {
        int end = off + len;
        int upperBound = off + (len & ~7);
        long addr = ARRAY_BYTE_BASE_OFFSET + off;
        long d, x;
        while (off < upperBound
                && (((d = UNSAFE.getLong(buf, addr)) | (((x = d ^ 0x5C5C5C5C5C5C5C5CL) - 0x0101010101010101L) & ~x)) & 0x8080808080808080L) == 0) {
            addr += 8;
            off += 8;
        }
        while (off++ < end) {
            byte b;
            if (((b = UNSAFE.getByte(buf, addr++)) & 0x80) != 0 || b == '\\') {
                return false;
            }
        }
        return true;
    }

    private static boolean isDigitLatin1(int c) {
        return c >= '0' && c <= '9';
    }

    public static int parseInt(byte[] buf, int off, int len) {
        int fc = buf[off];
        int result = isDigitLatin1(fc)
                ? '0' - fc
                : len != 1 && (fc == '-' || fc == '+')
                ? 0
                : 1;  // or any value > 0
        int end = off + len;
        off++;
        int d;
        while (off + 1 < end
                && (d = IOUtils.digit2(buf, off)) != -1
                && Integer.MIN_VALUE / 100 <= result & result <= 0) {
            result = result * 100 - d;  // overflow from d => result > 0
            off += 2;
        }
        if (off < end
                && isDigitLatin1(d = buf[off])
                && Integer.MIN_VALUE / 10 <= result & result <= 0) {
            result = result * 10 + '0' - d;  // overflow from '0' - d => result > 0
            off += 1;
        }
        if (off == end
                & result <= 0
                & (Integer.MIN_VALUE < result || fc == '-')) {
            return fc == '-' ? result : -result;
        }
        throw new NumberFormatException(new String(buf, off, len));
    }
}
