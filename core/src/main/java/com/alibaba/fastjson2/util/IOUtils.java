package com.alibaba.fastjson2.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;

import static com.alibaba.fastjson2.util.JDKUtils.*;

public class IOUtils {
    public static final int NULL_32 = BIG_ENDIAN ? 0x6e756c6c : 0x6c6c756e;
    public static final long NULL_64 = BIG_ENDIAN ? 0x6e0075006c006cL : 0x6c006c0075006eL;

    public static final int TRUE = BIG_ENDIAN ? 0x74727565 : 0x65757274;
    public static final long TRUE_64 = BIG_ENDIAN ? 0x74007200750065L : 0x65007500720074L;

    public static final int ALSE = BIG_ENDIAN ? 0x616c7365 : 0x65736c61;
    public static final long ALSE_64 = BIG_ENDIAN ? 0x61006c00730065L : 0x650073006c0061L;

    static final short[] PACKED_DIGITS;
    static final int[] PACKED_DIGITS_UTF16;

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
                0x3039, 0x3139, 0x3239, 0x3339, 0x3439, 0x3539, 0x3639, 0x3739, 0x3839, 0x3939
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
                0x300039, 0x310039, 0x320039, 0x330039, 0x340039, 0x350039, 0x360039, 0x370039, 0x380039, 0x390039
        };

        if (BIG_ENDIAN) {
            for (int i = 0; i < shorts.length; i++) {
                shorts[i] = Short.reverseBytes(shorts[i]);
            }
            for (int i = 0; i < digits.length; i++) {
                digits[i] = Integer.reverseBytes(digits[i] << 8);
            }
        }
        PACKED_DIGITS = shorts;
        PACKED_DIGITS_UTF16 = digits;
    }

    static final short[] HEX256;

    static final long SLASH_X03 = BIG_ENDIAN ? 0x2d00_0000_0000_2d00L : 0x2d_0000_0000_002dL;
    static final long DOT_X0 = BIG_ENDIAN ? 0x2e00L : 0x2eL;
    static final long COLON_X1 = BIG_ENDIAN ? 0x3a00_0000L : 0x3a_0000L;
    static final long COLON_X2 = BIG_ENDIAN ? 0x3a00_0000_0000L : 0x3a_0000_0000L;

    static {
        short[] digits = new short[16 * 16];

        for (int i = 0; i < 16; i++) {
            short hi = (short) (i < 10 ? i + '0' : i - 10 + 'a');

            for (int j = 0; j < 16; j++) {
                short lo = (short) (j < 10 ? j + '0' : j - 10 + 'a');
                digits[(i << 4) + j] = BIG_ENDIAN ? (short) ((hi << 8) | lo) : (short) (hi | (lo << 8));
            }
        }

        HEX256 = digits;
    }

    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    public static final int[] DIGITS_K = new int[1000];
    public static final long[] DIGITS_K_64 = new long[1000];

    static {
        for (int i = 0; i < 1000; i++) {
            int c0 = i < 10 ? 1 : i < 100 ? 2 : 3;
            int c1 = (i / 100) + '0';
            int c2 = ((i / 10) % 10) + '0';
            int c3 = i % 10 + '0';
            DIGITS_K[i] = c0 + (c1 << 8) + (c2 << 16) + (c3 << 24);
            long v = (c1 << 16) + (((long) c2) << 32) + (((long) c3) << 48);
            if (BIG_ENDIAN) {
                v <<= 8;
            }
            DIGITS_K_64[i] = c0 + v;
        }
    }

    /**
     * Return a big-endian packed integer for the 4 ASCII bytes for an input unsigned 2-byte integer.
     * {@code b0} is the most significant byte and {@code b1} is the least significant byte.
     * The integer is passed byte-wise to allow reordering of execution.
     */
    public static int packDigits(int b0, int b1) {
        int v = HEX256[b0 & 0xff] | (HEX256[b1 & 0xff] << 16);
        return BIG_ENDIAN ? Integer.reverseBytes(v) : v;
    }

    /**
     * Return a big-endian packed long for the 8 ASCII bytes for an input unsigned 4-byte integer.
     * {@code b0} is the most significant byte and {@code b3} is the least significant byte.
     * The integer is passed byte-wise to allow reordering of execution.
     */
    public static long packDigits(int b0, int b1, int b2, int b3) {
        short[] digits = HEX256;
        long v = (digits[b0 & 0xff]
                | (((long) digits[b1 & 0xff]) << 16)
                | (((long) digits[b2 & 0xff]) << 32))
                | (((long) digits[b3 & 0xff]) << 48);
        return BIG_ENDIAN ? Long.reverseBytes(v) : v;
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
            UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + charPos, PACKED_DIGITS[r]);
        }

        // We know there are at most two digits left at this point.
        if (i < -9) {
            charPos -= 2;
            UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + charPos, PACKED_DIGITS[-i]);
        } else {
            buf[--charPos] = (byte) ('0' - i);
        }

        if (negative) {
            buf[charPos - 1] = (byte) '-';
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
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[r]);
        }

        // We know there are at most two digits left at this point.
        if (i < -9) {
            charPos -= 2;
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[-i]);
        } else {
            buf[--charPos] = (char) ('0' - i);
        }

        if (negative) {
            buf[charPos - 1] = '-';
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
            assert charPos >= 0 && charPos < buf.length : "Trusted caller missed bounds check";
            UNSAFE.putShort(
                    buf,
                    ARRAY_BYTE_BASE_OFFSET + charPos,
                    PACKED_DIGITS[(int) ((q * 100) - i)]);
            i = q;
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 <= -100) {
            q2 = i2 / 100;
            charPos -= 2;
            assert charPos >= 0 && charPos < buf.length : "Trusted caller missed bounds check";
            UNSAFE.putShort(
                    buf,
                    ARRAY_BYTE_BASE_OFFSET + charPos,
                    PACKED_DIGITS[(q2 * 100) - i2]);
            i2 = q2;
        }

        // We know there are at most two digits left at this point.
        if (i2 < -9) {
            charPos -= 2;
            assert charPos >= 0 && charPos < buf.length : "Trusted caller missed bounds check";
            UNSAFE.putShort(
                    buf,
                    ARRAY_BYTE_BASE_OFFSET + charPos,
                    PACKED_DIGITS[-i2]);
        } else {
            buf[--charPos] = (byte) ('0' - i2);
        }

        if (negative) {
            buf[charPos - 1] = (byte) '-';
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
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[(int) ((q * 100) - i)]);
            i = q;
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 <= -100) {
            q2 = i2 / 100;
            charPos -= 2;
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[(q2 * 100) - i2]);
            i2 = q2;
        }

        // We know there are at most two digits left at this point.
        if (i2 < -9) {
            charPos -= 2;
            assert charPos >= 0 && charPos < buf.length : "Trusted caller missed bounds check";
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[-i2]);
        } else {
            buf[--charPos] = (char) ('0' - i2);
        }

        if (negative) {
            buf[--charPos] = '-';
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
            putLong(bytes, off, y0 + (DIGITS_K[yyy] & 0xffffff00) + ((DIGITS_K[month] & 0xffff0000L) << 24) + 0x2d00002d_0000_0000L);
            off += 8;
        } else {
            if (year > 9999) {
                bytes[off++] = '+';
            }
            off = IOUtils.writeInt32(bytes, off, year);
            putInt(bytes, off, ((DIGITS_K[month] & 0xffff0000) >> 8) + 0x2d00002d);
            off += 4;
        }

        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off, PACKED_DIGITS[dayOfMonth]);
        return off + 2;
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
            putLong(
                    chars,
                    off,
                    y0 + (DIGITS_K_64[yyy] & 0xffffffffffff0000L)
            );
            off += 4;
        } else {
            if (year > 9999) {
                chars[off++] = '+';
            }
            off = IOUtils.writeInt32(chars, off, year);
        }

        putLong(
                chars,
                off,
                SLASH_X03 + ((DIGITS_K_64[month] & 0xffff_ffff_0000_0000L) >> 16)
        );

        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 4) << 1), PACKED_DIGITS_UTF16[dayOfMonth]);
        off += 6;

        return off;
    }

    public static int writeLocalTime(byte[] bytes, int off, LocalTime time) {
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off, PACKED_DIGITS[time.getHour()]);
        bytes[off + 2] = ':';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 3, PACKED_DIGITS[time.getMinute()]);
        bytes[off + 5] = ':';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 6, PACKED_DIGITS[time.getSecond()]);

        off += 8;

        int nano = time.getNano();
        if (nano != 0) {
            final int div = nano / 1000;
            final int div2 = div / 1000;
            final int rem1 = nano - div * 1000;

            putInt(bytes, off, '.' + (DIGITS_K[div2] & 0xffffff00));
            off += 4;

            int v;
            if (rem1 == 0) {
                final int rem2 = div - div2 * 1000;
                if (rem2 == 0) {
                    return off;
                }

                v = DIGITS_K[rem2];
            } else {
                v = DIGITS_K[div - div2 * 1000];
            }

            putInt(bytes, off, v, 8);
            off += 3;
            if (rem1 == 0) {
                return off;
            }

            putInt(bytes, off, DIGITS_K[rem1], 8);
            off += 3;
        }

        return off;
    }

    public static int writeLocalTime(char[] chars, int off, LocalTime time) {
        UNSAFE.putInt(chars, ARRAY_BYTE_BASE_OFFSET + (off << 1), PACKED_DIGITS_UTF16[time.getHour()]);
        chars[off + 2] = ':';
        UNSAFE.putInt(chars, ARRAY_BYTE_BASE_OFFSET + ((off + 3) << 1), PACKED_DIGITS_UTF16[time.getMinute()]);
        chars[off + 5] = ':';
        UNSAFE.putInt(chars, ARRAY_BYTE_BASE_OFFSET + ((off + 6) << 1), PACKED_DIGITS_UTF16[time.getSecond()]);
        off += 8;

        int nano = time.getNano();
        if (nano != 0) {
            final int div = nano / 1000;
            final int div2 = div / 1000;
            final int rem1 = nano - div * 1000;

            putLong(chars, off, DOT_X0 + (DIGITS_K_64[div2] & 0xffffffffffff0000L));

            off += 4;

            int v;
            if (rem1 == 0) {
                final int rem2 = div - div2 * 1000;
                if (rem2 == 0) {
                    return off;
                }

                v = rem2;
            } else {
                v = div - div2 * 1000;
            }

            putLong(chars, off, DIGITS_K_64[v], 16);

            off += 3;
            if (rem1 == 0) {
                return off;
            }

            putLong(chars, off, DIGITS_K_64[rem1], 16);

            off += 3;
        }

        return off;
    }

    public static int writeInt64(final byte[] buf, int pos, long i) {
        if (i < 0) {
            buf[pos++] = '-';
        } else {
            i = -i;
        }

        if (i > -1000) {
            int v = DIGITS_K[(int) -i];
            int s = (byte) v;
            putInt(buf, pos, v, (4 - s) << 3);
            return pos + s;
        }

        final long q1 = i / 1000;
        final int r1 = (int) (i - q1 * 1000);
        final int v1 = DIGITS_K[-r1];
        if (i > -1000000) {
            final int v2 = DIGITS_K[(int) -q1];
            int s = (byte) v2;
            putInt(buf, pos, v2, (4 - s) << 3);
            putInt(buf, pos + s, v1, 8);
            return pos + s + 3;
        }

        final long q2 = q1 / 1000;
        final int r2 = (int) (q1 - q2 * 1000);
        final long q3 = q2 / 1000;
        final int v2 = DIGITS_K[-r2];
        if (q3 == 0) {
            final int v3 = DIGITS_K[(int) -q2];
            int s = (byte) v3;
            putInt(buf, pos, v3, (4 - s) << 3);
            pos += s;
            putInt(buf, pos, v2, 8);
            putInt(buf, pos + 3, v1, 8);
            return pos + 6;
        }
        final int r3 = (int) (q2 - q3 * 1000);
        final int q4 = (int) (q3 / 1000);
        final int v3 = DIGITS_K[-r3];
        if (q4 == 0) {
            final int v4 = DIGITS_K[(int) -q3];
            int s = (byte) v4;
            putInt(buf, pos, v4, (4 - s) << 3);
            pos += s;
            putInt(buf, pos, v3, 8);
            putInt(buf, pos + 3, v2, 8);
            putInt(buf, pos + 6, v1, 8);
            return pos + 9;
        }
        final int r4 = (int) (q3 - q4 * 1000);
        final int q5 = q4 / 1000;

        final int v4 = DIGITS_K[-r4];
        if (q5 == 0) {
            final int v5 = DIGITS_K[-q4];
            int s = (byte) v5;
            putInt(buf, pos, v5, (4 - s) << 3);
            pos += s;
            putInt(buf, pos, v4, 8);
            putInt(buf, pos + 3, v3, 8);
            putInt(buf, pos + 6, v2, 8);
            putInt(buf, pos + 9, v1, 8);
            return pos + 12;
        }
        final int r5 = q4 - q5 * 1000;
        final int q6 = q5 / 1000;
        final int v5 = DIGITS_K[-r5];
        if (q6 == 0) {
            final int v6 = DIGITS_K[-q5];
            int s = (byte) v6;
            putInt(buf, pos, v6, (4 - s) << 3);
            pos += s;
            putInt(buf, pos, v5, 8);
            putInt(buf, pos + 3, v4, 8);
            putInt(buf, pos + 6, v3, 8);
            putInt(buf, pos + 9, v2, 8);
            putInt(buf, pos + 12, v1, 8);
            return pos + 15;
        }

        final int r6 = q5 - q6 * 1000;
        final int v6 = DIGITS_K[-r6];
        putInt(buf, pos, '0' - q6 + (v6 & 0xffffff00));
        putInt(buf, pos + 4, v5, 8);
        putInt(buf, pos + 7, v4, 8);
        putInt(buf, pos + 10, v3, 8);
        putInt(buf, pos + 13, v2, 8);
        putInt(buf, pos + 16, v1, 8);
        return pos + 19;
    }

    public static int writeInt64(final char[] buf, int pos, long i) {
        if (i < 0) {
            buf[pos++] = '-';
        } else {
            i = -i;
        }

        if (i > -1000) {
            long v = DIGITS_K_64[(int) -i];
            int s = (byte) v;
            putLong(buf, pos, v, (4 - s) << 4);
            return pos + s;
        }

        final long q1 = i / 1000;
        final int r1 = (int) (i - q1 * 1000);
        final long v1 = DIGITS_K_64[-r1];
        if (i > -1000000) {
            final long v2 = DIGITS_K_64[(int) -q1];
            int s = (byte) v2;
            putLong(buf, pos, v2 >> ((4 - s) << 4));
            pos += s;
            putLong(buf, pos, v1, 16);
            return pos + 3;
        }

        final long q2 = q1 / 1000;
        final int r2 = (int) (q1 - q2 * 1000);
        final long q3 = q2 / 1000;
        final long v2 = DIGITS_K_64[-r2];
        if (q3 == 0) {
            final long v3 = DIGITS_K_64[(int) -q2];
            int s = (byte) v3;
            putLong(buf, pos, v3, (4 - s) << 4);
            pos += s;
            putLong(buf, pos, v2, 16);
            putLong(buf, pos + 3, v1, 16);
            return pos + 6;
        }
        final int r3 = (int) (q2 - q3 * 1000);
        final int q4 = (int) (q3 / 1000);
        final long v3 = DIGITS_K_64[-r3];
        if (q4 == 0) {
            final long v4 = DIGITS_K_64[(int) -q3];
            int s = (byte) v4;
            putLong(buf, pos, v4, (4 - s) << 4);
            pos += s;
            putLong(buf, pos, v3, 16);
            putLong(buf, pos + 3, v2, 16);
            putLong(buf, pos + 6, v1, 16);
            return pos + 9;
        }
        final int r4 = (int) (q3 - q4 * 1000);
        final int q5 = q4 / 1000;
        final long v4 = DIGITS_K_64[-r4];
        if (q5 == 0) {
            final long v5 = DIGITS_K_64[-q4];
            int s = (byte) v5;
            putLong(buf, pos, v5, (4 - s) << 4);
            pos += s;
            putLong(buf, pos, v4, 16);
            putLong(buf, pos + 3, v3, 16);
            putLong(buf, pos + 6, v2, 16);
            putLong(buf, pos + 9, v1, 16);
            return pos + 12;
        }
        final int r5 = q4 - q5 * 1000;
        final int q6 = q5 / 1000;
        final long v5 = DIGITS_K_64[-r5];
        if (q6 == 0) {
            final long v6 = DIGITS_K_64[-q5];
            int s = (byte) v6;
            putLong(buf, pos, v6, (4 - s) << 4);
            pos += s;
            putLong(buf, pos, v5, 16);
            putLong(buf, pos + 3, v4, 16);
            putLong(buf, pos + 6, v3, 16);
            putLong(buf, pos + 9, v2, 16);
            putLong(buf, pos + 12, v1, 16);
            return pos + 15;
        }

        putLong(buf,
                pos,
                '0' - q6 + (DIGITS_K_64[-q5 + q6 * 1000] & 0xffffffff_ffff0000L)
        );
        putLong(buf, pos + 4, v5, 16);
        putLong(buf, pos + 7, v4, 16);
        putLong(buf, pos + 10, v3, 16);
        putLong(buf, pos + 13, v2, 16);
        putLong(buf, pos + 16, v1, 16);
        return pos + 19;
    }

    public static int writeInt32(final byte[] buf, int pos, int i) {
        if (i < 0) {
            buf[pos++] = '-';
        } else {
            i = -i;
        }

        if (i > -1000) {
            int v = DIGITS_K[-i];
            int s = (byte) v;
            putInt(buf, pos, v, (4 - s) << 3);
            return pos + s;
        }

        final int q1 = i / 1000;
        final int r1 = i - q1 * 1000;
        final int v1 = DIGITS_K[-r1];
        if (i > -1000000) {
            final int v2 = DIGITS_K[-q1];
            int s = (byte) v2;
            putInt(buf, pos, v2, (4 - s) << 3);
            putInt(buf, pos + s, v1, 8);
            return pos + s + 3;
        }
        final int q2 = q1 / 1000;
        final int r2 = q1 - q2 * 1000;
        final int q3 = q2 / 1000;
        final int v2 = DIGITS_K[-r2];
        if (q3 == 0) {
            final int v3 = DIGITS_K[-q2];
            int s = (byte) v3;
            putInt(buf, pos, v3 >> ((4 - s) << 3));
            pos += s;
            putInt(buf, pos, v2 >> 8);
            putInt(buf, pos + 3, v1 >> 8);
            return pos + 6;
        }

        putInt(buf, pos, '0' - q3 + (DIGITS_K[-q2 + q3 * 1000] & 0xffffff00));
        putInt(buf, pos + 4, v2, 8);
        putInt(buf, pos + 7, v1, 8);
        return pos + 10;
    }

    public static int writeInt32(final char[] buf, int pos, int i) {
        if (i < 0) {
            buf[pos++] = '-';
        } else {
            i = -i;
        }

        if (i > -1000) {
            long v = DIGITS_K_64[-i];
            int s = (byte) v;
            putLong(buf, pos, v, (4 - s) << 4);
            return pos + s;
        }
        final int q1 = i / 1000;
        final int r1 = i - q1 * 1000;
        final long v1 = DIGITS_K_64[-r1];
        if (i > -1000000) {
            final long v2 = DIGITS_K_64[-q1];
            int s = (byte) v2;
            putLong(buf, pos, v2, (4 - s) << 4);
            putLong(buf, pos + s, v1, 16);
            return pos + s + 3;
        }
        final int q2 = q1 / 1000;
        final int r2 = q1 - q2 * 1000;
        final int q3 = q2 / 1000;
        final long v2 = DIGITS_K_64[-r2];
        if (q3 == 0) {
            final long v3 = DIGITS_K_64[-q2];
            int s = (byte) v3;
            putLong(buf, pos, v3, (4 - s) << 4);
            pos += s;
            putLong(buf, pos, v2, 16);
            putLong(buf, pos + 3, v1, 16);
            return pos + 6;
        }

        putLong(buf, pos, '0' - q3 + (DIGITS_K_64[-q2 + q3 * 1000] & 0xffffffff_ffff0000L));
        putLong(buf, pos + 4, v2, 16);
        putLong(buf, pos + 7, v1, 16);
        return pos + 10;
    }

    public static void putInt(byte[] buf, int pos, int v) {
        UNSAFE.putInt(
                buf,
                ARRAY_BYTE_BASE_OFFSET + pos,
                BIG_ENDIAN ? Integer.reverseBytes(v) : v
        );
    }

    public static void putInt(byte[] buf, int pos, int v, int shift) {
        UNSAFE.putInt(
                buf,
                ARRAY_BYTE_BASE_OFFSET + pos,
                BIG_ENDIAN
                        ? (Integer.reverseBytes(v) << shift)
                        : v >> shift
        );
    }

    public static void putLong(byte[] buf, int pos, long v) {
        UNSAFE.putLong(
                buf,
                ARRAY_BYTE_BASE_OFFSET + pos,
                BIG_ENDIAN ? Long.reverseBytes(v) : v
        );
    }

    public static void putLong(char[] buf, int pos, long v) {
        UNSAFE.putLong(
                buf,
                ARRAY_BYTE_BASE_OFFSET + (pos << 1),
                BIG_ENDIAN ? Long.reverseBytes(v) : v
        );
    }

    public static void putLong(char[] buf, int pos, long v, int shift) {
        UNSAFE.putLong(
                buf,
                ARRAY_BYTE_BASE_OFFSET + (pos << 1),
                BIG_ENDIAN
                        ? Long.reverseBytes(v) << shift
                        : v >> shift
        );
    }
}
