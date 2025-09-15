package com.alibaba.fastjson2.util;

import java.io.Closeable;
import java.time.LocalTime;
import java.util.Arrays;

import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_CHAR_BASE_OFFSET;

public class IOUtils {
    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    public static final int[] DIGITS_K = new int[1000];
    private static final byte[] MIN_INT_BYTES = "-2147483648".getBytes();
    private static final char[] MIN_INT_CHARS = "-2147483648".toCharArray();
    private static final byte[] MIN_LONG = "-9223372036854775808".getBytes();

    static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    static final int[] IA = new int[256];

    public static final short[] PACKED_DIGITS;
    public static final int[] PACKED_DIGITS_UTF16;

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
                char c = (char) ((b0 & 0xff) | ((b1 & 0xff) << 8));
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
                            char d = (char) ((b0 & 0xff) | ((b1 & 0xff) << 8));
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

    public static int writeLocalDate(byte[] bytes, int off, int year, int month, int dayOfMonth) {
        if (year < 0) {
            bytes[off++] = '-';
            year = -year;
        } else if (year > 9999) {
            bytes[off++] = '+';
        }

        if (year < 10000) {
            int y01 = year / 100;
            int y23 = year - y01 * 100;
            UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off, PACKED_DIGITS[y01]);
            UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 2, PACKED_DIGITS[y23]);
            off += 4;
        } else {
            off = IOUtils.writeInt32(bytes, off, year);
        }

        bytes[off] = '-';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 1, PACKED_DIGITS[month]);
        bytes[off + 3] = '-';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 4, PACKED_DIGITS[dayOfMonth]);
        return off + 6;
    }

    public static int writeLocalDate(char[] chars, int off, int year, int month, int dayOfMonth) {
        if (year < 0) {
            chars[off++] = '-';
            year = -year;
        } else if (year > 9999) {
            chars[off++] = '+';
        }

        if (year < 10000) {
            int y01 = year / 100;
            int y23 = year - y01 * 100;
            UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + (off << 1), PACKED_DIGITS_UTF16[y01]);
            UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 2) << 1), PACKED_DIGITS_UTF16[y23]);
            off += 4;
        } else {
            off = IOUtils.writeInt32(chars, off, year);
        }

        chars[off] = '-';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 1) << 1), PACKED_DIGITS_UTF16[month]);
        chars[off + 3] = '-';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 4) << 1), PACKED_DIGITS_UTF16[dayOfMonth]);
        return off + 6;
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

    public static void writeLocalTime(byte[] bytes, int off, int hour, int minute, int second) {
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off, PACKED_DIGITS[hour]);
        bytes[off + 2] = ':';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 3, PACKED_DIGITS[minute]);
        bytes[off + 5] = ':';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 6, PACKED_DIGITS[second]);
    }

    public static void writeLocalTime(char[] chars, int off, int hour, int minute, int second) {
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + (off << 1), PACKED_DIGITS_UTF16[hour]);
        chars[off + 2] = ':';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 3) << 1), PACKED_DIGITS_UTF16[minute]);
        chars[off + 5] = ':';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 6) << 1), PACKED_DIGITS_UTF16[second]);
    }

    public static int writeInt64(final byte[] buf, int pos, final long value) {
        long i;
        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                System.arraycopy(MIN_LONG, 0, buf, pos + 0, MIN_LONG.length);
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
        final int v2 = DIGITS_K[r2];
        if (i < 1000000000) {
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

        final long q3 = q2 / 1000;
        final int r3 = (int) (q2 - q3 * 1000);
        final int v3 = DIGITS_K[r3];
        if (i < 1000000000000L) {
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

        final int q4 = (int) (q3 / 1000);
        final int r4 = (int) (q3 - q4 * 1000);
        final int v4 = DIGITS_K[r4];
        if (i < 1000000000000000L) {
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

        final int q5 = q4 / 1000;
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
        final int v2 = DIGITS_K[r2];
        if (i < 1000000000) {
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

        final long q3 = q2 / 1000;
        final int r3 = (int) (q2 - q3 * 1000);
        final int v3 = DIGITS_K[r3];
        if (i < 1000000000000L) {
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

        final int q4 = (int) (q3 / 1000);
        final int r4 = (int) (q3 - q4 * 1000);
        final int v4 = DIGITS_K[r4];
        if (i < 1000000000000000L) {
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

        final int q5 = q4 / 1000;
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
            buf[pos++] = (byte) v;
            return pos;
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
            buf[pos++] = (char) (byte) v;
            return pos;
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
        int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
        return BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value);
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
            int i = IA[s.charAt(sIx)] << 18
                    | IA[s.charAt(sIx + 1)] << 12
                    | IA[s.charAt(sIx + 2)] << 6
                    | IA[s.charAt(sIx + 3)];
            sIx += 4;

            // Add the bytes
            dArr[d] = (byte) (i >> 16);
            dArr[d + 1] = (byte) (i >> 8);
            dArr[d + 2] = (byte) i;
            d += 3;

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
            UNSAFE.putShort(
                    buf,
                    ARRAY_BYTE_BASE_OFFSET + charPos,
                    PACKED_DIGITS[(q2 * 100) - i2]);
            i2 = q2;
        }

        // We know there are at most two digits left at this point.
        if (i2 < -9) {
            charPos -= 2;
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

    public static int digit2(byte[] bytes, int off) {
        int x = bytes[off] | (bytes[off + 1] << 8);
        int d;
        if ((((x & 0xF0F0) - 0x3030) | (((d = x & 0x0F0F) + 0x0606) & 0xF0F0)) != 0) {
            return -1;
        }
        return (d & 0xF) * 10 + (d >> 8);
    }

    public static int digit2(char[] chars, int off) {
        int x = chars[off] | (chars[off + 1] << 16);
        int d;
        if ((((x & 0xFFF0FFF0) - 0x300030) | (((d = x & 0x0F000F) + 0x060006) & 0xF000F0)) != 0) {
            return -1;
        }
        return (d & 0xF) * 10 + (d >> 16);
    }

    public static int indexOfChar(byte[] value, int ch, int fromIndex, int max) {
        for (int i = fromIndex; i < max; i++) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOfChar(char[] value, int ch, int fromIndex, int max) {
        for (int i = fromIndex; i < max; i++) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    public static int digit4(char[] chars, int off) {
        long x = UNSAFE.getLong(chars, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1));
        if (BIG_ENDIAN) {
            x = Long.reverseBytes(x);
        }
        long d;
        if ((((x & 0xFFF0FFF0FFF0FFF0L) - 0x30003000300030L) | (((d = x & 0x0F000F000F000FL) + 0x06000600060006L) & 0xF000F000F000F0L)) != 0) {
            return -1;
        }
        return (int) ((((d & 0xF) * 10 + ((d >> 16) & 0xF)) * 10 + ((d >> 32) & 0xF)) * 10 + (d >> 48));
    }
}
