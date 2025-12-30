package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;

import static com.alibaba.fastjson2.internal.Conf.BYTES;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.NumberUtils.MULTIPLY_HIGH;

/**
 * IOUtils is a utility class that provides various I/O operations and helper methods
 * for working with byte arrays, character arrays, and other data structures used in
 * FASTJSON2 for serialization and deserialization.
 *
 * <p>This class contains optimized methods for:</p>
 * <ul>
 *   <li>Writing and reading primitive values to/from byte and character arrays</li>
 *   <li>Handling UTF-8 and UTF-16 encoding/decoding</li>
 *   <li>Working with date/time values</li>
 *   <li>Performing low-level memory operations</li>
 *   <li>Managing resource cleanup</li>
 * </ul>
 *
 * <p>All methods in this class are static and thread-safe.</p>
 *
 * @since 2.0.0
 */
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
        ZERO_DOT_LATIN1 = BYTES.getShortUnaligned(new byte[] {'0', '.'}, 0);
    }

    private static short digitPair(int value) {
        return PACKED_DIGITS[value & 0x7f];
    }

    /**
     * Writes a pair of digits to a byte array at the specified position.
     * This method is used for efficient digit pair writing during number formatting.
     *
     * @param buf the byte array buffer to write to
     * @param charPos the position in the buffer where to write the digit pair
     * @param value the value (0-99) to write as a digit pair
     */
    public static void writeDigitPair(byte[] buf, int charPos, int value) {
        BYTES.putShortLE(
                buf,
                charPos,
                PACKED_DIGITS[value & 0x7f]);
    }

    /**
     * Writes a pair of digits to a character array at the specified position.
     * This method is used for efficient digit pair writing during number formatting.
     *
     * @param buf the character array buffer to write to
     * @param charPos the position in the buffer where to write the digit pair
     * @param value the value (0-99) to write as a digit pair
     */
    public static void writeDigitPair(char[] buf, int charPos, int value) {
        BYTES.putIntLE(
                buf,
                charPos,
                PACKED_DIGITS_UTF16[value & 0x7f]);
    }

    /**
     * Calculates the string size (number of digits) needed to represent an integer value.
     * This method is used to determine the buffer size needed for number formatting.
     *
     * @param x the integer value to calculate the string size for
     * @return the number of digits needed to represent the integer value
     */
    public static int stringSize(int x) {
        for (int i = 0; ; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    /**
     * Calculates the string size (number of digits) needed to represent a long value.
     * This method is used to determine the buffer size needed for number formatting.
     *
     * @param x the long value to calculate the string size for
     * @return the number of digits needed to represent the long value
     */
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

    /**
     * Converts an integer to its character representation and writes it to a byte array.
     * This method handles negative numbers and optimizes digit conversion by processing
     * two digits at a time when possible.
     *
     * @param i the integer value to convert
     * @param index the starting index in the buffer where to write the characters
     * @param buf the byte array buffer to write the characters to
     */
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
            BYTES.putByte(buf, --charPos, (byte) ('0' - i));
        }

        if (negative) {
            BYTES.putByte(buf, charPos - 1, (byte) '-');
        }
    }

    /**
     * Converts an integer to its character representation and writes it to a character array.
     * This method handles negative numbers and optimizes digit conversion by processing
     * two digits at a time when possible.
     *
     * @param i the integer value to convert
     * @param index the starting index in the buffer where to write the characters
     * @param buf the character array buffer to write the characters to
     */
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
            BYTES.putChar(buf, --charPos, (char) ('0' - i));
        }

        if (negative) {
            BYTES.putChar(buf, charPos - 1, '-');
        }
    }

    /**
     * Converts a long integer to its character representation and writes it to a byte array.
     * This method handles negative numbers and optimizes digit conversion by processing
     * two digits at a time when possible, switching to int-based processing when the value
     * fits in an integer.
     *
     * @param i the long integer value to convert
     * @param index the starting index in the buffer where to write the characters
     * @param buf the byte array buffer to write the characters to
     */
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
            BYTES.putByte(buf, --charPos, (byte) ('0' - i2));
        }

        if (negative) {
            BYTES.putByte(buf, charPos - 1, (byte) '-');
        }
    }

    /**
     * Converts a long integer to its character representation and writes it to a character array.
     * This method handles negative numbers and optimizes digit conversion by processing
     * two digits at a time when possible, switching to int-based processing when the value
     * fits in an integer.
     *
     * @param i the long integer value to convert
     * @param index the starting index in the buffer where to write the characters
     * @param buf the character array buffer to write the characters to
     */
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
            BYTES.putChar(buf, --charPos, (char) ('0' - i2));
        }

        if (negative) {
            BYTES.putChar(buf, charPos - 1, '-');
        }
    }

    /**
     * Writes a decimal number to a byte array buffer
     *
     * @param buf byte array buffer
     * @param off buffer starting offset
     * @param unscaledVal unscaled value (precision part of BigDecimal)
     * @param scale number of digits after the decimal point, caller must ensure scale &gt;= 0
     * @return offset after writing
     *
     * Note: This method trusts that the caller has ensured scale &gt;= 0
     */
    public static int writeDecimal(byte[] buf, int off, long unscaledVal, int scale) {
        if (unscaledVal < 0) {
            BYTES.putByte(buf, off++, (byte) '-');
            unscaledVal = -unscaledVal;
        }

        if (scale != 0) {
            int unscaleValSize = IOUtils.stringSize(unscaledVal);
            int insertionPoint = unscaleValSize - scale;
            if (insertionPoint == 0) {
                BYTES.putShortUnaligned(buf, off, ZERO_DOT_LATIN1);
                off += 2;
            } else if (insertionPoint < 0) {
                BYTES.putShortUnaligned(buf, off, ZERO_DOT_LATIN1);
                off += 2;

                for (int i = 0; i < -insertionPoint; i++) {
                    BYTES.putByte(buf, off++, (byte) '0');
                }
            } else {
                long power = POWER_TEN[scale - 1];
                long div = unscaledVal / power;
                long rem = unscaledVal - div * power;
                off = IOUtils.writeInt64(buf, off, div);
                BYTES.putByte(buf, off, (byte) '.');

                if (scale == 1) {
                    BYTES.putByte(buf, off + 1, (byte) (rem + '0'));
                    return off + 2;
                } else if (scale == 2) {
                    writeDigitPair(buf, off + 1, (int) rem);
                    return off + 3;
                }

                for (int i = 0, end = unscaleValSize - stringSize(rem) - insertionPoint; i < end; ++i) {
                    BYTES.putByte(buf, ++off, (byte) '0');
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
     * @param scale number of digits after the decimal point, caller must ensure scale &gt;= 0
     * @return offset after writing
     *
     * Note: This method trusts that the caller has ensured scale &gt;= 0
     */
    public static int writeDecimal(char[] buf, int off, long unscaledVal, int scale) {
        if (unscaledVal < 0) {
            BYTES.putChar(buf, off++, '-');
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
                    BYTES.putChar(buf, off++, '0');
                }
            } else {
                long power = POWER_TEN[scale - 1];
                long div = unscaledVal / power;
                long rem = unscaledVal - div * power;
                off = IOUtils.writeInt64(buf, off, div);
                BYTES.putChar(buf, off, '.');

                if (scale == 1) {
                    BYTES.putChar(buf, off + 1, (char) (rem + '0'));
                    return off + 2;
                } else if (scale == 2) {
                    writeDigitPair(buf, off + 1, (int) rem);
                    return off + 3;
                }

                for (int i = 0, end = unscaleValSize - stringSize(rem) - insertionPoint; i < end; ++i) {
                    BYTES.putChar(buf, ++off, '0');
                }
                return IOUtils.writeInt64(buf, off + 1, rem);
            }
        }

        return IOUtils.writeInt64(buf, off, unscaledVal);
    }

    public static int encodeUTF8(byte[] str, int offset, int strlen, byte[] dst, int dp) {
        int sl = offset + strlen;
        while (offset < sl) {
            char c = BYTES.getChar(str, offset++);

            if (c < 0x80) {
                dst[dp++] = (byte) c;
            } else {
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    dst[dp] = (byte) (0xc0 | (c >> 6));
                    dst[dp + 1] = (byte) (0x80 | (c & 0x3f));
                    dp += 2;
                } else if (c >= '\uD800' && c <= '\uDFFF') {
                    utf8_char2(str, offset++, sl, c, dst, dp);
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

    /**
     * Encodes a UTF-16 character array to UTF-8 byte array.
     * This method converts characters from a source character array to a destination
     * byte array in UTF-8 encoding format, with optimized handling for ASCII characters.
     *
     * @param src the source character array
     * @param offset the starting offset in the source array
     * @param len the number of characters to encode from the source array
     * @param dst the destination byte array to write UTF-8 encoded data to
     * @param dp the starting position in the destination array
     * @return the updated position in the destination array after encoding
     */
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
                || (d = BYTES.getChar(src, offset)) < '\uDC00'
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

    /**
     * Checks if the given string represents a valid number.
     * A valid number may have an optional leading '+' or '-' sign,
     * followed by one or more digits.
     *
     * @param str the string to check
     * @return true if the string represents a valid number, false otherwise
     */
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

    /**
     * Checks if the character array segment represents a valid number.
     * A valid number may have an optional leading '+' or '-' sign,
     * followed by one or more digits.
     *
     * @param buf the character array to check
     * @param off the starting offset in the array
     * @param len the number of characters to check
     * @return true if the character array segment represents a valid number, false otherwise
     */
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

    /**
     * Checks if the byte array segment represents a valid number.
     * A valid number may have an optional leading '+' or '-' sign,
     * followed by one or more digits.
     *
     * @param buf the byte array to check
     * @param off the starting offset in the array
     * @param len the number of bytes to check
     * @return true if the byte array segment represents a valid number, false otherwise
     */
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

    /**
     * Safely closes a Closeable resource, ignoring any exceptions that may occur.
     * This method is a utility to close resources without having to handle
     * IOException or other exceptions.
     *
     * @param x the Closeable resource to close, can be null
     */
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

    /**
     * Decodes UTF-8 encoded byte array to a UTF-16 byte array.
     * This method converts UTF-8 encoded data from a source byte array to UTF-16 encoded
     * data in a destination byte array. Each UTF-16 character is stored as two consecutive bytes.
     *
     * @param src the source byte array containing UTF-8 encoded data
     * @param off the starting offset in the source array
     * @param len the number of bytes to decode from the source array
     * @param dst the destination byte array to write UTF-16 encoded data to
     * @return the number of bytes written to the destination array, or -1 if decoding fails
     */
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

    /**
     * Decodes UTF-8 encoded byte array to a character array.
     * This method converts UTF-8 encoded data from a source byte array to Unicode characters
     * in a destination character array, with optimized handling for ASCII characters.
     *
     * @param src the source byte array containing UTF-8 encoded data
     * @param off the starting offset in the source array
     * @param len the number of bytes to decode from the source array
     * @param dst the destination character array to write decoded characters to
     * @return the number of characters written to the destination array, or -1 if decoding fails
     */
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

    /**
     * Counts the number of lines in a file.
     * This method reads the specified file and counts the number of newline characters ('\
')
     * to determine the total number of lines in the file.
     *
     * @param file the File to count lines in
     * @return the number of lines in the file
     * @throws Exception if an I/O error occurs while reading the file
     */
    public static long lines(File file) throws Exception {
        try (FileInputStream in = new FileInputStream(file)) {
            return lines(in);
        }
    }

    /**
     * Counts the number of lines in an InputStream.
     * This method reads data from the specified InputStream and counts the number of
     * newline characters ('\
') to determine the total number of lines in the stream.
     *
     * @param in the InputStream to count lines in
     * @return the number of lines in the stream
     * @throws Exception if an I/O error occurs while reading the stream
     */
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

    /**
     * Writes a LocalDate value to a byte array in ISO8601 format (yyyy-MM-dd).
     * This method formats a date with year, month, and day components and writes it to
     * the specified byte array buffer at the given offset.
     *
     * @param buf the byte array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param year the year component of the date
     * @param month the month component of the date (1-12)
     * @param dayOfMonth the day component of the date (1-31)
     * @return the updated offset after writing the date
     */
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

        BYTES.putLongLE(buf, off,
                0x2d00002d0000L
                        | digitPair(y23)
                        | ((long) digitPair(month) << 24)
                        | ((long) digitPair(dayOfMonth) << 48));
        return off + 8;
    }

    /**
     * Writes a LocalDate value to a character array in ISO8601 format (yyyy-MM-dd).
     * This method formats a date with year, month, and day components and writes it to
     * the specified character array buffer at the given offset.
     *
     * @param buf the character array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param year the year component of the date
     * @param month the month component of the date (1-12)
     * @param dayOfMonth the day component of the date (1-31)
     * @return the updated offset after writing the date
     */
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
        BYTES.putLongLE(buf, off,
                ((long) (p1 & 0xFFFF) << 48) | ((long) '-' << 32) | PACKED_DIGITS_UTF16[y23 & 0x7f]);
        BYTES.putLongLE(buf, off + 4,
                ((long) (p1 & 0xFFFF0000) >> 16) | ((long) '-' << 16) | ((long) PACKED_DIGITS_UTF16[dayOfMonth & 0x7f] << 32));
        return off + 8;
    }

    /**
     * Writes a LocalTime value to a byte array in ISO8601 format (HH:mm:ss).
     * This method formats a time with hour, minute, and second components and writes it to
     * the specified byte array buffer at the given offset.
     *
     * @param buf the byte array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param hour the hour component of the time (0-23)
     * @param minute the minute component of the time (0-59)
     * @param second the second component of the time (0-59)
     */
    public static void writeLocalTime(byte[] buf, int off, int hour, int minute, int second) {
        BYTES.putLongLE(
                buf,
                off,
                0x3a00003a0000L
                        | digitPair(hour)
                        | ((long) digitPair(minute) << 24)
                        | ((long) digitPair(second) << 48));
    }

    /**
     * Writes a LocalTime value to a byte array in ISO8601 format (HH:mm:ss[.nnnnnnnnn]).
     * This method formats a time with hour, minute, second, and nanosecond components
     * and writes it to the specified byte array buffer at the given offset.
     *
     * @param buf the byte array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param time the LocalTime object to write
     * @return the updated offset after writing the time (including nanoseconds if present)
     */
    public static int writeLocalTime(byte[] buf, int off, LocalTime time) {
        writeLocalTime(buf, off, time.getHour(), time.getMinute(), time.getSecond());
        off += 8;
        int nano = time.getNano();
        return nano != 0 ? writeNano(buf, off, nano) : off;
    }

    /**
     * Writes nanosecond values to a byte array.
     * This method formats nanosecond values and writes them to the specified byte array
     * buffer at the given offset, typically used for writing fractional seconds in time values.
     *
     * @param buf the byte array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param nano the nanosecond value to write (0-999,999,999)
     * @return the updated offset after writing the nanoseconds
     */
    public static int writeNano(byte[] buf, int off, int nano) {
        final int div = (int) (nano * 274877907L >> 38); //nano / 1000;
        final int div2 = (int) (div * 274877907L >> 38); // div / 1000;
        final int rem1 = nano - div * 1000;

        BYTES.putIntLE(buf, off, DIGITS_K_32[div2 & 0x3ff] & 0xffffff00 | '.');
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

        BYTES.putShortLE(buf, off, (short) (v >> 8));
        off += 2;
        if (rem1 == 0) {
            BYTES.putByte(buf, off, (byte) (v >> 24));
            return off + 1;
        }

        BYTES.putIntLE(buf, off, DIGITS_K_32[rem1] & 0xffffff00 | (v >> 24));
        return off + 4;
    }

    /**
     * Writes nanosecond values to a character array.
     * This method formats nanosecond values and writes them to the specified character array
     * buffer at the given offset, typically used for writing fractional seconds in time values.
     *
     * @param buf the character array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param nano the nanosecond value to write (0-999,999,999)
     * @return the updated offset after writing the nanoseconds
     */
    public static int writeNano(char[] buf, int off, int nano) {
        final int div = (int) (nano * 274877907L >> 38); //nano / 1000;
        final int div2 = (int) (div * 274877907L >> 38); // div / 1000;
        final int rem1 = nano - div * 1000;

        BYTES.putLongLE(buf, off, DIGITS_K_64[div2 & 0x3ff] & 0xffffffffffff0000L | DOT_X0);
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

        BYTES.putIntLE(buf, off, (int) (v >> 16));
        off += 2;
        if (rem1 == 0) {
            BYTES.putChar(buf, off, (char) (v >> 48));
            return off + 1;
        }

        BYTES.putLongLE(buf, off, DIGITS_K_64[rem1 & 0x3ff] & 0xffffffffffff0000L | (v >> 48));
        return off + 4;
    }

    /**
     * Writes a LocalTime value to a character array in ISO8601 format (HH:mm:ss).
     * This method formats a time with hour, minute, and second components and writes it to
     * the specified character array buffer at the given offset.
     *
     * @param buf the character array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param hour the hour component of the time (0-23)
     * @param minute the minute component of the time (0-59)
     * @param second the second component of the time (0-59)
     */
    public static void writeLocalTime(char[] buf, int off, int hour, int minute, int second) {
        writeDigitPair(buf, off, hour);
        BYTES.putChar(buf, off + 2, ':');
        writeDigitPair(buf, off + 3, minute);
        BYTES.putChar(buf, off + 5, ':');
        writeDigitPair(buf, off + 6, second);
    }

    /**
     * Writes a LocalTime value to a character array in ISO8601 format (HH:mm:ss[.nnnnnnnnn]).
     * This method formats a time with hour, minute, second, and nanosecond components
     * and writes it to the specified character array buffer at the given offset.
     *
     * @param buf the character array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param time the LocalTime object to write
     * @return the updated offset after writing the time (including nanoseconds if present)
     */
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
        BYTES.putIntUnaligned(buf, off, v2);
        return off + 4;
    }

    private static int writeInt4(char[] buf, int off, int v) {
        int v1 = (int) (v * 1374389535L >> 37); // v / 100;
        BYTES.putLongUnaligned(buf, off, mergeInt64(v - v1 * 100, v1));
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
        BYTES.putIntUnaligned(buf, off, v >> ((((byte) v) + 1) << 3));
        return off + 3 - (byte) v;
    }

    private static int writeInt3(char[] buf, int off, int val) {
        long v = DIGITS_K_64[val & 0x3ff];
        BYTES.putLongUnaligned(buf, off, v >> ((((short) v) + 1) << 4));
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
        BYTES.putLongUnaligned(buf, off, v);
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
        BYTES.putLongUnaligned(buf, off, x1);
        BYTES.putLongUnaligned(buf, off + 4, x2);
        return off + 8;
    }

    /**
     * Writes a 64-bit long integer value to a byte array.
     * This method converts a long integer to its string representation and writes it
     * to the specified byte array buffer at the given offset, handling negative values
     * and optimizing for different value ranges.
     *
     * @param buf the byte array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param val the long integer value to write
     * @return the updated offset after writing the value
     */
    public static int writeInt64(byte[] buf, int off, long val) {
        if (val < 0) {
            if (val == Long.MIN_VALUE) {
                System.arraycopy(MIN_LONG_BYTES, 0, buf, off, MIN_LONG_BYTES.length);
                return off + MIN_LONG_BYTES.length;
            }
            val = -val;
            BYTES.putByte(buf, off++, (byte) ('-'));
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

    /**
     * Writes a 64-bit long integer value to a character array.
     * This method converts a long integer to its string representation and writes it
     * to the specified character array buffer at the given offset, handling negative values
     * and optimizing for different value ranges.
     *
     * @param buf the character array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param val the long integer value to write
     * @return the updated offset after writing the value
     */
    public static int writeInt64(char[] buf, int off, long val) {
        if (val < 0) {
            if (val == Long.MIN_VALUE) {
                System.arraycopy(MIN_LONG_CHARS, 0, buf, off, MIN_LONG_CHARS.length);
                return off + MIN_LONG_CHARS.length;
            }
            val = -val;
            BYTES.putChar(buf, off++, '-');
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

    /**
     * Writes an 8-bit byte value as an integer to a byte array.
     * This method converts a byte value to its string representation and writes it
     * to the specified byte array buffer at the given position, handling negative values
     * and optimizing for different value ranges.
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to start writing
     * @param value the byte value to write as an integer
     * @return the updated position after writing the value
     */
    public static int writeInt8(final byte[] buf, int pos, final byte value) {
        int i;
        if (value < 0) {
            i = -value;
            BYTES.putByte(buf, pos++, (byte) '-');
        } else {
            i = value;
        }

        int v = DIGITS_K_32[i & 0x3ff];
        final int start = (byte) v;
        if (start == 0) {
            BYTES.putShortLE(buf, pos, (short) (v >> 8));
            pos += 2;
        } else if (start == 1) {
            BYTES.putByte(buf, pos++, (byte) (v >> 16));
        }
        BYTES.putByte(buf, pos, (byte) (v >> 24));
        return pos + 1;
    }

    /**
     * Writes an 8-bit byte value as an integer to a character array.
     * This method converts a byte value to its string representation and writes it
     * to the specified character array buffer at the given position, handling negative values
     * and optimizing for different value ranges.
     *
     * @param buf the character array buffer to write to
     * @param pos the position in the buffer where to start writing
     * @param value the byte value to write as an integer
     * @return the updated position after writing the value
     */
    public static int writeInt8(char[] buf, int pos, final byte value) {
        int i;
        if (value < 0) {
            i = -value;
            BYTES.putChar(buf, pos++, '-');
        } else {
            i = value;
        }

        long v = DIGITS_K_64[i & 0x3ff];
        final int start = (byte) v;
        if (start == 0) {
            BYTES.putIntLE(buf, pos, (int) (v >> 16));
            pos += 2;
        } else if (start == 1) {
            BYTES.putChar(buf, pos++, (char) (v >> 32));
        }
        BYTES.putChar(buf, pos, (char) (v >> 48));
        return pos + 1;
    }

    /**
     * Writes a 16-bit short value as an integer to a byte array.
     * This method converts a short value to its string representation and writes it
     * to the specified byte array buffer at the given position, handling negative values
     * and optimizing for different value ranges.
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to start writing
     * @param value the short value to write as an integer
     * @return the updated position after writing the value
     */
    public static int writeInt16(byte[] buf, int pos, final short value) {
        int i;
        if (value < 0) {
            i = -value;
            BYTES.putByte(buf, pos++, (byte) '-');
        } else {
            i = value;
        }

        if (i < 1000) {
            int v = DIGITS_K_32[i & 0x3ff];
            final int start = (byte) v;
            if (start == 0) {
                BYTES.putShortLE(buf, pos, (short) (v >> 8));
                pos += 2;
            } else if (start == 1) {
                BYTES.putByte(buf, pos++, (byte) (v >> 16));
            }
            BYTES.putByte(buf, pos, (byte) (v >> 24));
            return pos + 1;
        }

        final int q1 = (int) (i * 274877907L >> 38); // i / 1000;
        final int v2 = DIGITS_K_32[q1 & 0x3ff];
        if ((byte) v2 == 1) {
            BYTES.putByte(buf, pos++, (byte) (v2 >> 16));
        }
        BYTES.putIntLE(buf, pos, (DIGITS_K_32[(i - q1 * 1000) & 0x3ff]) & 0xffffff00 | (v2 >> 24));
        return pos + 4;
    }

    /**
     * Writes a 16-bit short value as an integer to a character array.
     * This method converts a short value to its string representation and writes it
     * to the specified character array buffer at the given position, handling negative values
     * and optimizing for different value ranges.
     *
     * @param buf the character array buffer to write to
     * @param pos the position in the buffer where to start writing
     * @param value the short value to write as an integer
     * @return the updated position after writing the value
     */
    public static int writeInt16(char[] buf, int pos, final short value) {
        int i;
        if (value < 0) {
            i = -value;
            BYTES.putChar(buf, pos++, '-');
        } else {
            i = value;
        }

        if (i < 1000) {
            long v = DIGITS_K_64[i & 0x3ff];
            final int start = (byte) v;
            if (start == 0) {
                BYTES.putIntLE(buf, pos, (int) (v >> 16));
                pos += 2;
            } else if (start == 1) {
                BYTES.putChar(buf, pos++, (char) (v >> 32));
            }
            BYTES.putChar(buf, pos, (char) (v >> 48));
            return pos + 1;
        }

        final int q1 = (int) (i * 274877907L >> 38); // i / 1000;
        final long v2 = DIGITS_K_64[q1 & 0x3ff];
        if ((byte) v2 == 1) {
            BYTES.putChar(buf, pos++, (char) (v2 >> 32));
        }
        BYTES.putLongLE(buf, pos, DIGITS_K_64[(i - q1 * 1000) & 0x3ff] & 0xffffffffffff0000L | (v2 >> 48));
        return pos + 4;
    }

    /**
     * Writes a 32-bit integer value to a byte array.
     * This method converts a long value to its string representation and writes it
     * to the specified byte array buffer at the given offset, handling negative values
     * and optimizing for different value ranges.
     *
     * @param buf the byte array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param val the long value to write as an integer
     * @return the updated offset after writing the value
     */
    public static int writeInt32(final byte[] buf, int off, long val) {
        if (val < 0) {
            val = -val;
            BYTES.putByte(buf, off++, (byte) ('-'));
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

    /**
     * Writes a 32-bit integer value to a character array.
     * This method converts a long value to its string representation and writes it
     * to the specified character array buffer at the given offset, handling negative values
     * and optimizing for different value ranges.
     *
     * @param buf the character array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param val the long value to write as an integer
     * @return the updated offset after writing the value
     */
    public static int writeInt32(final char[] buf, int off, long val) {
        if (val < 0) {
            val = -val;
            BYTES.putChar(buf, off++, '-');
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

    /**
     * Writes a boolean value to a byte array as a string representation.
     * This method writes either "true" or "false" to the specified byte array at the given offset.
     * For true values, it writes "true" (4 bytes), and for false values, it writes "false" (5 bytes).
     *
     * @param buf the byte array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param v the boolean value to write
     * @return the updated offset after writing the boolean value
     */
    public static int putBoolean(byte[] buf, int off, boolean v) {
        if (v) {
            BYTES.putIntUnaligned(buf, off, TRUE);
            return off + 4;
        } else {
            BYTES.putByte(buf, off, (byte) 'f');
            BYTES.putIntUnaligned(buf, off + 1, ALSE);
            return off + 5;
        }
    }

    /**
     * Writes a boolean value to a character array as a string representation.
     * This method writes either "true" or "false" to the specified character array at the given offset.
     * For true values, it writes "true" (4 characters), and for false values, it writes "false" (5 characters).
     *
     * @param buf the character array buffer to write to
     * @param off the offset in the buffer where to start writing
     * @param v the boolean value to write
     * @return the updated offset after writing the boolean value
     */
    public static int putBoolean(char[] buf, int off, boolean v) {
        if (v) {
            BYTES.putLongUnaligned(buf, off, TRUE_64);
            return off + 4;
        } else {
            BYTES.putChar(buf, off, 'f');
            BYTES.putLongUnaligned(buf, off + 1, ALSE_64);
            return off + 5;
        }
    }

    /**
     * Checks if the specified position in a byte array contains the string "alse".
     * This method is used to verify if a byte sequence matches the "alse" portion of "false".
     *
     * @param buf the byte array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position contains "alse", false otherwise
     */
    public static boolean isALSE(byte[] buf, int pos) {
        return BYTES.getIntUnaligned(buf, pos) == ALSE;
    }

    /**
     * Checks if the specified position in a byte array does not contain the string "alse".
     * This method is used to verify if a byte sequence does not match the "alse" portion of "false".
     *
     * @param buf the byte array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position does not contain "alse", false otherwise
     */
    public static boolean notALSE(byte[] buf, int pos) {
        return BYTES.getIntUnaligned(buf, pos) != ALSE;
    }

    /**
     * Checks if the specified position in a character array contains the string "alse".
     * This method is used to verify if a character sequence matches the "alse" portion of "false".
     *
     * @param buf the character array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position contains "alse", false otherwise
     */
    public static boolean isALSE(char[] buf, int pos) {
        return getLongUnaligned(buf, pos) == ALSE_64;
    }

    /**
     * Checks if the specified position in a character array does not contain the string "alse".
     * This method is used to verify if a character sequence does not match the "alse" portion of "false".
     *
     * @param buf the character array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position does not contain "alse", false otherwise
     */
    public static boolean notALSE(char[] buf, int pos) {
        return getLongUnaligned(buf, pos) != ALSE_64;
    }

    /**
     * Checks if the specified position in a byte array contains the string "null".
     * This method is used to verify if a byte sequence matches the string "null".
     *
     * @param buf the byte array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position contains "null", false otherwise
     */
    public static boolean isNULL(byte[] buf, int pos) {
        return BYTES.getIntUnaligned(buf, pos) == NULL_32;
    }

    /**
     * Checks if the specified position in a byte array does not contain the string "null".
     * This method is used to verify if a byte sequence does not match the string "null".
     *
     * @param buf the byte array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position does not contain "null", false otherwise
     */
    public static boolean notNULL(byte[] buf, int pos) {
        return BYTES.getIntUnaligned(buf, pos) != NULL_32;
    }

    /**
     * Checks if the specified position in a byte array does not contain the string "true".
     * This method is used to verify if a byte sequence does not match the string "true".
     *
     * @param buf the byte array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position does not contain "true", false otherwise
     */
    public static boolean notTRUE(byte[] buf, int pos) {
        return BYTES.getIntUnaligned(buf, pos) != TRUE;
    }

    /**
     * Checks if the specified position in a character array does not contain the string "true".
     * This method is used to verify if a character sequence does not match the string "true".
     *
     * @param buf the character array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position does not contain "true", false otherwise
     */
    public static boolean notTRUE(char[] buf, int pos) {
        return BYTES.getLongUnaligned(buf, pos) != TRUE_64;
    }

    /**
     * Checks if the specified position in a character array contains the string "null".
     * This method is used to verify if a character sequence matches the string "null".
     *
     * @param buf the character array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position contains "null", false otherwise
     */
    public static boolean isNULL(char[] buf, int pos) {
        return BYTES.getLongUnaligned(buf, pos) == NULL_64;
    }

    /**
     * Checks if the specified position in a character array does not contain the string "null".
     * This method is used to verify if a character sequence does not match the string "null".
     *
     * @param buf the character array buffer to check
     * @param pos the position in the buffer to check
     * @return true if the position does not contain "null", false otherwise
     */
    public static boolean notNULL(char[] buf, int pos) {
        return BYTES.getLongUnaligned(buf, pos) != NULL_64;
    }

    /**
     * Writes the string "null" to a byte array at the specified position.
     * This method puts the byte representation of "null" into the specified byte array
     * at the given position.
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to write "null"
     */
    public static void putNULL(byte[] buf, int pos) {
        BYTES.putIntUnaligned(buf, pos, NULL_32);
    }

    /**
     * Writes the string "null" to a character array at the specified position.
     * This method puts the character representation of "null" into the specified character array
     * at the given position.
     *
     * @param buf the character array buffer to write to
     * @param pos the position in the buffer where to write "null"
     */
    public static void putNULL(char[] buf, int pos) {
        BYTES.putLongUnaligned(buf, pos, NULL_64);
    }

    /**
     * Validates if the specified integer value represents a single digit (0-9).
     * This method checks if the input value is within the valid digit range.
     *
     * @param d the integer value to check
     * @return the input value if it's a valid digit (0-9), -1 otherwise
     */
    public static int digit(int d) {
        return d >= 0 && d <= 9 ? d : -1;
    }

    /**
     * Finds the index of a quote character in a byte array.
     * This method searches for either a single quote (') or double quote (") character
     * within the specified range of the byte array.
     *
     * @param buf the byte array to search in
     * @param quote the quote character to search for (either '\'' or '"')
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the quote character, or -1 if not found
     */
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

    /**
     * Finds the index of a quote character in a byte array using vectorized operations.
     * This method searches for either a single quote (') or double quote (") character
     * within the specified range of the byte array using optimized vector operations.
     *
     * @param buf the byte array to search in
     * @param quote the quote character to search for (either '\'' or '"')
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the quote character, or -1 if not found
     */
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

    /**
     * Finds the index of a double quote character in a byte array.
     * This method searches for a double quote (") character within the specified range
     * of the byte array.
     *
     * @param buf the byte array to search in
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the double quote character, or -1 if not found
     */
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

    /**
     * Finds the index of a double quote character in a byte array using vectorized operations.
     * This method searches for a double quote (") character within the specified range
     * of the byte array using optimized vector operations.
     *
     * @param buf the byte array to search in
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the double quote character, or -1 if not found
     */
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

    /**
     * Finds the index of a line separator character in a byte array.
     * This method searches for a newline ('\n') character within the specified range
     * of the byte array.
     *
     * @param buf the byte array to search in
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the line separator character, or -1 if not found
     */
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

    /**
     * Finds the index of a line separator character in a byte array using vectorized operations.
     * This method searches for a newline ('\n') character within the specified range
     * of the byte array using optimized vector operations.
     *
     * @param buf the byte array to search in
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the line separator character, or -1 if not found
     */
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

    /**
     * Finds the index of a slash character in a byte array.
     * This method searches for a backslash ('\') character within the specified range
     * of the byte array.
     *
     * @param buf the byte array to search in
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the slash character, or -1 if not found
     */
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

    /**
     * Finds the index of a slash character in a byte array using vectorized operations.
     * This method searches for a backslash ('\') character within the specified range
     * of the byte array using optimized vector operations.
     *
     * @param buf the byte array to search in
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the slash character, or -1 if not found
     */
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

    /**
     * Checks if a byte array region matches a prefix string.
     * This method compares a segment of a byte array with a string prefix to see
     * if they match exactly.
     *
     * @param bytes the byte array to check
     * @param off the offset in the byte array where to start checking
     * @param prefix the string prefix to match against
     * @return true if the byte array region matches the prefix, false otherwise
     */
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

    /**
     * Finds the index of a character in a byte array.
     * This method searches for a specific character within the specified range
     * of the byte array.
     *
     * @param buf the byte array to search in
     * @param ch the character to search for
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the character, or -1 if not found
     */
    public static int indexOfChar(byte[] buf, int ch, int fromIndex, int max) {
        for (int i = fromIndex; i < max; i++) {
            if (buf[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the index of a character in a character array.
     * This method searches for a specific character within the specified range
     * of the character array.
     *
     * @param buf the character array to search in
     * @param ch the character to search for
     * @param fromIndex the index to start searching from
     * @param max the maximum index to search up to (exclusive)
     * @return the index of the character, or -1 if not found
     */
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

    public static int hexDigit4(byte[] buf, int offset, int end) {
        if (offset + 4 > Math.min(end, buf.length)) {
            throw outOfBoundsCheckFromToIndex(offset, end);
        }
        return hexDigit4(buf, offset);
    }

    static JSONException outOfBoundsCheckFromToIndex(int offset, int end) {
        return new JSONException("offset overflow, offset " + offset + ", end " + end);
    }

    /**
     * Extracts a 4-digit hexadecimal number from a byte array at the specified offset.
     * This method performs optimized hexadecimal digit extraction by processing 4 bytes at once
     * using vector operations for improved performance.
     *
     * @param buf the byte array to extract hexadecimal digits from
     * @param offset the offset in the array where to start extracting
     * @return the extracted 4-digit hexadecimal number
     */
    public static int hexDigit4(byte[] buf, int offset) {
        int v = BYTES.getIntLE(buf, offset);
        v = (v & 0x0F0F0F0F) + ((((v & 0x40404040) >> 2) | ((v & 0x40404040) << 1)) >>> 4);
        return ((v & 0xF000000) >>> 24) + ((v & 0xF0000) >>> 12) + (v & 0xF00) + ((v & 0xF) << 12);
    }

    public static int hexDigit4(char[] buf, int offset, int end) {
        if (offset + 4 > Math.min(end, buf.length)) {
            throw outOfBoundsCheckFromToIndex(offset, end);
        }
        return hexDigit4(buf, offset);
    }

    /**
     * Extracts a 4-digit hexadecimal number from a character array at the specified offset.
     * This method performs optimized hexadecimal digit extraction by processing 4 characters at once
     * using vector operations for improved performance.
     *
     * @param buf the character array to extract hexadecimal digits from
     * @param offset the offset in the array where to start extracting
     * @return the extracted 4-digit hexadecimal number
     */
    public static int hexDigit4(char[] buf, int offset) {
        long v = getLongLE(buf, offset);
        v = (v & 0x000F_000F_000F_000FL) + ((((v & 0x0004_0004_0004_00040L) >> 2) | ((v & 0x0004_0004_0004_00040L) << 1)) >>> 4);
        return (int) (((v & 0xF_0000_0000_0000L) >>> 48) + ((v & 0xF_0000_0000L) >>> 28) + ((v & 0xF_0000) >> 8) + ((v & 0xF) << 12));
    }

    /**
     * Checks if the specified character is a digit (0-9).
     * This method determines if a character represents a valid decimal digit.
     *
     * @param ch the character to check
     * @return true if the character is a digit (0-9), false otherwise
     */
    public static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Checks if the specified position in a byte array contains a UTF-8 Byte Order Mark (BOM).
     * This method verifies if the first three bytes at the specified offset match the UTF-8 BOM
     * sequence (0xEF, 0xBB, 0xBF).
     *
     * @param buf the byte array to check
     * @param off the offset in the array where to check for the BOM
     * @return true if the position contains a UTF-8 BOM, false otherwise
     */
    public static boolean isUTF8BOM(byte[] buf, int off) {
        // EF BB BF
        return ((BYTES.getIntLE(buf, off)) & 0xFFFFFF) == 0xBFBBEF;
    }

    /**
     * Gets a long value from a byte array at the specified offset in big-endian byte order.
     * This method retrieves a long value from the specified byte array at the given offset
     * using big-endian byte ordering (most significant byte first).
     *
     * @param buf the byte array to read from
     * @param offset the offset in the array where to read the long value
     * @return the long value at the specified offset in big-endian order
     */
    public static long getLongBE(byte[] buf, int offset) {
        return BYTES.getLongBE(buf, offset);
    }

    /**
     * Gets a long value from a byte array at the specified offset without alignment considerations.
     * This method retrieves a long value from the specified byte array at the given offset
     * without performing any byte order conversion or alignment adjustments.
     *
     * @param buf the byte array to read from
     * @param offset the offset in the array where to read the long value
     * @return the long value at the specified offset
     */
    public static long getLongUnaligned(byte[] buf, int offset) {
        return BYTES.getLongUnaligned(buf, offset);
    }

    /**
     * Gets a long value from a character array at the specified offset without alignment considerations.
     * This method retrieves a long value from the specified character array at the given offset
     * without performing any byte order conversion or alignment adjustments.
     *
     * @param buf the character array to read from
     * @param offset the offset in the array where to read the long value
     * @return the long value at the specified offset
     */
    public static long getLongUnaligned(char[] buf, int offset) {
        return BYTES.getLongUnaligned(buf, offset);
    }

    /**
     * Gets a long value from a byte array at the specified offset in little-endian byte order.
     * This method retrieves a long value from the specified byte array at the given offset
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the byte array to read from
     * @param offset the offset in the array where to read the long value
     * @return the long value at the specified offset in little-endian order
     */
    public static long getLongLE(byte[] buf, int offset) {
        return BYTES.getLongLE(buf, offset);
    }

    /**
     * Gets a long value from a character array at the specified offset in little-endian byte order.
     * This method retrieves a long value from the specified character array at the given offset
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the character array to read from
     * @param offset the offset in the array where to read the long value
     * @return the long value at the specified offset in little-endian order
     */
    public static long getLongLE(char[] buf, int offset) {
        return BYTES.getLongLE(buf, offset);
    }

    /**
     * Converts a 2-digit hexadecimal value to its character representation.
     * This method transforms a packed 2-digit hexadecimal value into its ASCII character
     * representation using optimized bit operations.
     *
     * @param i the packed 2-digit hexadecimal value to convert
     * @return the character representation of the hexadecimal value
     */
    public static short hex2(int i) {
        i = ((i & 0xF0) >> 4) | ((i & 0xF) << 8);
        int m = (i + 0x06060606) & 0x10101010;
        return (short) (((m << 1) + (m >> 1) - (m >> 4))
                + 0x30303030 + i);
    }

    /**
     * Converts a 2-digit hexadecimal value to its uppercase character representation.
     * This method transforms a packed 2-digit hexadecimal value into its uppercase ASCII
     * character representation using optimized bit operations.
     *
     * @param i the packed 2-digit hexadecimal value to convert
     * @return the uppercase character representation of the hexadecimal value
     */
    public static short hex2U(int i) {
        i = ((i & 0xF0) >> 4) | ((i & 0xF) << 8);
        int m = (i + 0x06060606) & 0x10101010;
        return (short) (((m >> 1) - (m >> 4))
                + 0x30303030 + i);
    }

    /**
     * Converts a 2-digit hexadecimal value to its UTF-16 character representation.
     * This method transforms a packed 2-digit hexadecimal value into its UTF-16 character
     * representation using optimized bit operations.
     *
     * @param i the packed 2-digit hexadecimal value to convert
     * @return the UTF-16 character representation of the hexadecimal value
     */
    public static int utf16Hex2(int i) {
        // 0x000F000F
        i = ((i & 0xF0) >> 4) | ((i & 0xF) << 16);
        int m = (i + 0x00060006) & 0x00100010;
        return ((m << 1) + (m >> 1) - (m >> 4))
                + 0x00300030 + i;
    }

    /**
     * Converts a 4-digit hexadecimal value to its uppercase character representation.
     * This method transforms a packed 4-digit hexadecimal value into its uppercase ASCII
     * character representation using optimized bit operations.
     *
     * @param i the packed 4-digit hexadecimal value to convert
     * @return the uppercase character representation of the hexadecimal value
     */
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

    /**
     * Converts a 4-digit hexadecimal value to its uppercase UTF-16 character representation.
     * This method transforms a packed 4-digit hexadecimal value into its uppercase UTF-16
     * character representation using optimized bit operations.
     *
     * @param i the packed 4-digit hexadecimal value to convert
     * @return the uppercase UTF-16 character representation of the hexadecimal value
     */
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

    /**
     * Converts an integer value between big-endian and little-endian byte orders.
     * This method conditionally reverses the byte order of an integer value based on
     * the specified endianness flag and the system's native endianness.
     *
     * @param big true to convert to big-endian, false to convert to little-endian
     * @param n the integer value to convert
     * @return the converted integer value in the specified byte order
     */
    public static int convEndian(boolean big, int n) {
        return big == BIG_ENDIAN ? n : Integer.reverseBytes(n);
    }

    /**
     * Converts a long value between big-endian and little-endian byte orders.
     * This method conditionally reverses the byte order of a long value based on
     * the specified endianness flag and the system's native endianness.
     *
     * @param big true to convert to big-endian, false to convert to little-endian
     * @param n the long value to convert
     * @return the converted long value in the specified byte order
     */
    public static long convEndian(boolean big, long n) {
        return big == BIG_ENDIAN ? n : Long.reverseBytes(n);
    }

    static short convEndian(boolean big, short n) {
        return big == BIG_ENDIAN ? n : Short.reverseBytes(n);
    }

    /**
     * Checks if a character array segment contains only Latin-1 characters.
     * This method verifies that all characters in the specified segment of the character
     * array are within the Latin-1 character set (Unicode code points 0-255).
     *
     * @param buf the character array to check
     * @param off the starting offset in the array
     * @param len the number of characters to check
     * @return true if all characters in the segment are Latin-1, false otherwise
     */
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

    /**
     * Checks if a string contains only ASCII characters.
     * This method verifies that all characters in the specified string are within
     * the ASCII character set (Unicode code points 0-127).
     *
     * @param str the string to check
     * @return true if all characters in the string are ASCII, false otherwise
     */
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

    /**
     * Checks if a byte array contains only ASCII characters.
     * This method verifies that all bytes in the specified byte array are within
     * the ASCII character set (values 0-127).
     *
     * @param buf the byte array to check
     * @return true if all bytes in the array are ASCII, false otherwise
     */
    public static boolean isASCII(byte[] buf) {
        return isASCII(buf, 0, buf.length);
    }

    /**
     * Checks if a byte array segment contains only ASCII characters.
     * This method verifies that all bytes in the specified segment of the byte array
     * are within the ASCII character set (values 0-127).
     *
     * @param buf the byte array to check
     * @param off the starting offset in the array
     * @param len the number of bytes to check
     * @return true if all bytes in the segment are ASCII, false otherwise
     */
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

    /**
     * Checks if a byte array segment contains only ASCII characters and no backslash characters.
     * This method verifies that all bytes in the specified segment of the byte array are within
     * the ASCII character set (values 0-127) and do not contain any backslash ('\\') characters.
     *
     * @param buf the byte array to check
     * @param off the starting offset in the array
     * @param len the number of bytes to check
     * @return true if all bytes in the segment are ASCII and not backslashes, false otherwise
     */
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

    /**
     * Parses an integer value from a byte array segment.
     * This method converts a sequence of ASCII digit characters in a byte array segment
     * to an integer value, handling optional leading '+' or '-' signs.
     *
     * @param buf the byte array containing the digit characters
     * @param off the starting offset in the array
     * @param len the number of bytes to parse
     * @return the parsed integer value
     * @throws NumberFormatException if the byte array segment does not contain a valid integer representation
     */
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
                && (d = BYTES.digit2(buf, off)) != -1
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
