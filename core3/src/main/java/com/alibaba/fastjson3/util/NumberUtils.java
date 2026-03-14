package com.alibaba.fastjson3.util;

import java.lang.invoke.*;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Number formatting utilities for fastjson3.
 * Ported from fastjson2's NumberUtils with IOUtils dependencies inlined.
 *
 * <p>Author: wangy</p>
 */
public final class NumberUtils {
    // ==================== Endian detection ====================
    static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    // ==================== Byte-level constants (previously in IOUtils) ====================
    static final short DOT_ZERO_16 = BIG_ENDIAN ? (short) ('.' << 8 | '0') : (short) ('0' << 8 | '.');
    static final short ZERO_DOT_16 = BIG_ENDIAN ? (short) ('0' << 8 | '.') : (short) ('.' << 8 | '0');
    static final short ZERO_ZERO_16 = (short) ('0' << 8 | '0');
    static final int NULL_32 = BIG_ENDIAN ? 0x6e756c6c : 0x6c6c756e;

    // ==================== PACKED_DIGITS (byte pairs for 00-99) ====================
    static final short[] PACKED_DIGITS;
    static final int[] DIGITS_K_32 = new int[1024];

    private static final byte[] MIN_LONG_BYTES = "-9223372036854775808".getBytes();

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
        PACKED_DIGITS = shorts;

        for (int i = 0; i < 1000; i++) {
            int c0 = i < 10 ? 2 : i < 100 ? 1 : 0;
            int c1 = (i / 100) + '0';
            int c2 = ((i / 10) % 10) + '0';
            int c3 = i % 10 + '0';
            DIGITS_K_32[i] = c0 + (c1 << 8) + (c2 << 16) + (c3 << 24);
        }
    }

    // ==================== TWO_DIGITS_16_BITS (for exponent writing) ====================
    static final short[] TWO_DIGITS_16_BITS = new short[100];

    static {
        for (int d1 = 0; d1 < 10; ++d1) {
            for (int d2 = 0; d2 < 10; ++d2) {
                int intVal32;
                if (BIG_ENDIAN) {
                    intVal32 = (d1 + 48) << 8 | (d2 + 48);
                } else {
                    intVal32 = (d2 + 48) << 8 | (d1 + 48);
                }
                int k = d1 * 10 + d2;
                TWO_DIGITS_16_BITS[k] = (short) intVal32;
            }
        }
    }

    // ==================== INFINITY constant for byte[] ====================
    static final long INFINITY;

    static {
        String str = "Infinity";
        INFINITY = JDKUtils.getLongDirect(str.getBytes(StandardCharsets.ISO_8859_1), 0);
    }

    // ==================== multiplyHigh ====================
    @FunctionalInterface
    interface LongBiFunction {
        long multiplyHigh(long x, long y);
    }

    static long multiplyHigh(long x, long y) {
        long x1 = x >> 32;
        long x2 = x & 0xFFFFFFFFL;
        long y1 = y >> 32;
        long y2 = y & 0xFFFFFFFFL;

        long z2 = x2 * y2;
        long t = x1 * y2 + (z2 >>> 32);
        long z1 = t & 0xFFFFFFFFL;
        long z0 = t >> 32;
        z1 += x2 * y1;

        return x1 * y1 + z0 + (z1 >> 32);
    }

    static final LongBiFunction MULTIPLY_HIGH;

    static {
        LongBiFunction function = null;
        // JDK 17+: Math.multiplyHigh is available
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType methodType = MethodType.methodType(long.class, long.class, long.class);
            MethodHandle methodHandle = lookup.findStatic(Math.class, "multiplyHigh", methodType);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "multiplyHigh",
                    MethodType.methodType(LongBiFunction.class),
                    methodType,
                    methodHandle,
                    methodType
            );
            function = (LongBiFunction) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            // ignored
        }
        if (function == null) {
            function = NumberUtils::multiplyHigh;
        }
        MULTIPLY_HIGH = function;
    }

    private NumberUtils() {
    }

    // ==================== Decimal power tables ====================
    static final double[] POSITIVE_DECIMAL_POWER = new double[325];
    static final double[] NEGATIVE_DECIMAL_POWER = new double[325];
    static final long[] POW10_LONG_VALUES = new long[]{
            10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000L,
            100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
            10000000000000000L, 100000000000000000L, 1000000000000000000L, 9223372036854775807L
    };

    static final long[] POW5_LONG_VALUES = new long[27];
    static final BigInteger[] POW5_BI_VALUES = new BigInteger[343];

    static {
        for (int i = 0, len = POSITIVE_DECIMAL_POWER.length; i < len; ++i) {
            POSITIVE_DECIMAL_POWER[i] = Double.valueOf("1.0E" + i);
            NEGATIVE_DECIMAL_POWER[i] = Double.valueOf("1.0E-" + i);
        }
        NEGATIVE_DECIMAL_POWER[NEGATIVE_DECIMAL_POWER.length - 1] = Double.MIN_VALUE;

        long val = 1;
        for (int i = 0; i < POW5_LONG_VALUES.length; ++i) {
            POW5_LONG_VALUES[i] = val;
            val *= 5;
        }

        BigInteger five = BigInteger.valueOf(5);
        POW5_BI_VALUES[0] = BigInteger.ONE;
        for (int i = 1; i < POW5_BI_VALUES.length; ++i) {
            POW5_BI_VALUES[i] = five.pow(i);
        }
    }

    static final int MOD_DOUBLE_EXP = (1 << 11) - 1;
    static final long MOD_DOUBLE_MANTISSA = (1L << 52) - 1;

    static final char[][] POSITIVE_DECIMAL_POWER_CHARS = new char[325][];
    static final char[][] NEGATIVE_DECIMAL_POWER_CHARS = new char[325][];

    static {
        for (int i = 0, len = POSITIVE_DECIMAL_POWER_CHARS.length; i < len; ++i) {
            String positive = "1.0E" + i;
            String negative = "1.0E-" + i;
            POSITIVE_DECIMAL_POWER_CHARS[i] = positive.toCharArray();
            NEGATIVE_DECIMAL_POWER_CHARS[i] = negative.toCharArray();
        }
        NEGATIVE_DECIMAL_POWER_CHARS[NEGATIVE_DECIMAL_POWER_CHARS.length - 1] = "4.9E-324".toCharArray();
    }

    // ==================== stringSize (inlined from IOUtils) ====================

    static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p = 10 * p;
        }
        return 19;
    }

    // ==================== multiplyHighAndShift ====================

    static long multiplyHighAndShift(long x, long y, int shift) {
        long H = MULTIPLY_HIGH.multiplyHigh(x, y);
        if (shift >= 64) {
            int sr = shift - 64;
            return H >>> sr;
        }
        long L = x * y;
        return H << (64 - shift) | (L >>> shift);
    }

    static long multiplyHighAndShift(long x, long y, long y32, int s) {
        int sr = s - 64;
        long H = MULTIPLY_HIGH.multiplyHigh(x, y);
        long L = x * y;

        long H1 = MULTIPLY_HIGH.multiplyHigh(x, y32);
        long L1 = x * y32;

        long carry = (H1 << 32) + (L1 >>> 32);
        long L2 = L + carry;
        if ((L | carry) < 0 && ((L & carry) < 0 || L2 >= 0)) {
            ++H;
        }
        L = L2;
        if (sr >= 0) {
            return H >>> sr;
        }
        return H << -sr | (L >>> s);
    }

    // ==================== doubleToScientific ====================

    public static Scientific doubleToScientific(double doubleValue) {
        if (doubleValue == Double.MIN_VALUE) {
            return Scientific.DOUBLE_MIN;
        }
        long bits = Double.doubleToRawLongBits(doubleValue);
        int e2 = (int) (bits >> 52) & MOD_DOUBLE_EXP;
        long mantissa0 = bits & MOD_DOUBLE_MANTISSA;
        boolean flagForDown = mantissa0 > 0;
        int e52;
        long output;
        long rawOutput, d3, d4;
        int e10, adl;
        if (e2 > 0) {
            if (e2 == 2047) {
                return Scientific.SCIENTIFIC_NULL;
            }
            mantissa0 = 1L << 52 | mantissa0;
            e52 = e2 - 1075;
        } else {
            if (mantissa0 == 0) {
                return bits == 0 ? Scientific.ZERO : Scientific.NEGATIVE_ZERO;
            }
            int lz52 = Long.numberOfLeadingZeros(mantissa0) - 11;
            mantissa0 <<= lz52;
            e52 = -1074 - lz52;
        }
        boolean accurate = false;
        if (e52 >= 0) {
            ED d = ED.E2_D_A[e52];
            e10 = d.e10;
            adl = d.adl;
            d3 = d.d3;
            d4 = d.d4;
            if (d.b && mantissa0 >= d.bv) {
                if (mantissa0 > d.bv) {
                    ++e10;
                    ++adl;
                } else {
                    if (doubleValue == POSITIVE_DECIMAL_POWER[e10 + 1]) {
                        return new Scientific(e10 + 1, true);
                    }
                }
            }
            int o5 = d.o5;
            int sb = e52 + o5;
            if (o5 < 0) {
                ED5 d5 = ED5.ED5_A[-o5];
                int rb = sb - 10 - d5.ob;
                rawOutput = multiplyHighAndShift(mantissa0 << 10, d5.oy, d5.of, 32 - rb);
                accurate = o5 == -1 && sb < 11;
            } else {
                rawOutput = (mantissa0 * POW5_LONG_VALUES[o5]) << sb;
                accurate = true;
            }
        } else {
            int e5 = -e52;
            ED d = ED.E5_D_A[e5];
            e10 = d.e10;
            adl = d.adl;
            d3 = d.d3;
            d4 = d.d4;
            if (d.b && mantissa0 >= d.bv) {
                if (mantissa0 > d.bv) {
                    ++e10;
                    ++adl;
                } else {
                    if (e10 >= -1 && doubleValue == POSITIVE_DECIMAL_POWER[e10 + 1]) {
                        return new Scientific(e10 + 1, true);
                    }
                    if (e10 < -1 && doubleValue == NEGATIVE_DECIMAL_POWER[-e10 - 1]) {
                        return new Scientific(e10 + 1, true);
                    }
                }
            }

            int o5 = d.o5;
            int sb = o5 + e52;
            if (sb < 0) {
                if (o5 < POW5_LONG_VALUES.length) {
                    rawOutput = multiplyHighAndShift(mantissa0, POW5_LONG_VALUES[o5], -sb);
                } else if (o5 < POW5_LONG_VALUES.length + 4) {
                    rawOutput = multiplyHighAndShift(
                            mantissa0 * POW5_LONG_VALUES[o5 - POW5_LONG_VALUES.length + 1],
                            POW5_LONG_VALUES[POW5_LONG_VALUES.length - 1], -sb);
                } else {
                    ED5 ed5 = ED5.ED5_A[o5];
                    rawOutput = multiplyHighAndShift(mantissa0 << 10, ed5.y, ed5.f, -(ed5.dfb + sb) + 10);
                }
            } else {
                rawOutput = POW5_LONG_VALUES[o5] * mantissa0 << sb;
            }
        }
        if (accurate) {
            rawOutput = rawOutput / 10;
            if (adl == 16) {
                --adl;
                rawOutput = (rawOutput + 5) / 10;
            }
            return new Scientific(rawOutput, adl + 2, e10);
        }

        long div = rawOutput / 1000, rem = rawOutput - div * 1000;
        long remUp = (10001 - rem * 10) << 1;
        boolean up;
        if ((up = (remUp <= d4)) || ((rem + 1) << (flagForDown ? 1 : 2)) <= d3) {
            output = div + (up ? 1 : 0);
            --adl;
        } else {
            if (flagForDown) {
                output = (rawOutput + 50) / 100;
            } else {
                output = (rawOutput + 5) / 10;
                ++adl;
            }
        }
        return new Scientific(output, adl + 1, e10);
    }

    // ==================== writeDouble (byte[] overload) ====================

    public static int writeDouble(byte[] buf, int off, double doubleValue, boolean json, boolean writeSpecialAsString) {
        if (Double.isNaN(doubleValue) || doubleValue == Double.POSITIVE_INFINITY || doubleValue == Double.NEGATIVE_INFINITY) {
            return writeSpecial(buf, off, (float) doubleValue, json, writeSpecialAsString);
        }

        long bits;
        if (doubleValue == 0) {
            bits = Double.doubleToLongBits(doubleValue);
            if (bits == 0x8000000000000000L) {
                buf[off++] = '-';
            }
            buf[off] = '0';
            JDKUtils.putShortDirect(buf, off + 1, DOT_ZERO_16);
            return off + 3;
        }
        boolean sign = doubleValue < 0;
        if (sign) {
            buf[off++] = '-';
            doubleValue = -doubleValue;
        }
        if (doubleValue == (long) doubleValue) {
            long output = (long) doubleValue;
            int numLength = stringSize(output);
            return writeDecimal(output, numLength, numLength - 1, buf, off);
        }
        Scientific scientific = NumberUtils.doubleToScientific(doubleValue);
        int e10 = scientific.e10;
        if (!scientific.b) {
            return writeDecimal(scientific.output, scientific.count, scientific.e10, buf, off);
        }
        if (e10 >= 0) {
            char[] chars = POSITIVE_DECIMAL_POWER_CHARS[e10];
            for (char c : chars) {
                buf[off++] = (byte) c;
            }
            return off;
        } else {
            char[] chars = NEGATIVE_DECIMAL_POWER_CHARS[-e10];
            for (char c : chars) {
                buf[off++] = (byte) c;
            }
            return off;
        }
    }

    // ==================== writeSpecial (byte[] overload) ====================

    private static int writeSpecial(byte[] buf, int off, float floatValue, boolean json, boolean writeSpecialAsString) {
        if (json && !writeSpecialAsString) {
            JDKUtils.putIntDirect(buf, off, NULL_32);
            return off + 4;
        }
        if (writeSpecialAsString) {
            buf[off++] = '"';
        }

        if (Float.isNaN(floatValue)) {
            buf[off] = 'N';
            buf[off + 1] = 'a';
            buf[off + 2] = 'N';
            off += 3;
        } else {
            if (floatValue < 0) {
                buf[off++] = '-';
            }
            JDKUtils.putLongDirect(buf, off, INFINITY);
            off += 8;
        }

        if (writeSpecialAsString) {
            buf[off++] = '"';
        }
        return off;
    }

    // ==================== writeDecimal (byte[] overload) ====================

    private static int writeDecimal(long value, int digitCnt, int e10, byte[] buf, int off) {
        if ((value & 1) == 0 && value % 5 == 0) {
            while (value % 100 == 0) {
                digitCnt -= 2;
                value /= 100;
                if (digitCnt == 1) {
                    break;
                }
            }
            if ((value & 1) == 0 && value % 5 == 0) {
                if (value > 0) {
                    --digitCnt;
                    value /= 10;
                }
            }
        }
        // Whether to use Scientific notation
        boolean useScientific = e10 < -3 || e10 >= 7;
        if (useScientific) {
            if (digitCnt == 1) {
                buf[off] = (byte) (value + 48);
                JDKUtils.putShortDirect(buf, off + 1, DOT_ZERO_16);
                off += 3; // .0
            } else {
                int pos = digitCnt - 2;
                // get the first digit
                long tl = POW10_LONG_VALUES[pos];
                int fd = (int) (value / tl);
                buf[off] = (byte) (fd + 48);
                buf[off + 1] = '.';
                off += 2;

                long pointAfter = value - fd * tl;
                // fill zeros
                while (--pos > -1 && pointAfter < POW10_LONG_VALUES[pos]) {
                    buf[off++] = '0';
                }
                off = writeInt64(buf, off, pointAfter);
            }
            buf[off++] = 'E';
            if (e10 < 0) {
                buf[off++] = '-';
                e10 = -e10;
            }
            if (e10 > 99) {
                int n = (int) (e10 * 1374389535L >> 37); //e10 / 100;
                buf[off] = (byte) (n + 48);
                e10 = e10 - n * 100;
                JDKUtils.putShortDirect(buf, off + 1, TWO_DIGITS_16_BITS[e10]);
                off += 3;
            } else {
                if (e10 > 9) {
                    JDKUtils.putShortDirect(buf, off, TWO_DIGITS_16_BITS[e10]);
                    off += 2;
                } else {
                    buf[off++] = (byte) (e10 + 48);
                }
            }
        } else {
            // for non-scientific notation such as 12345, write a decimal point when size = decimalExp.
            if (e10 < 0) {
                // -1/-2/-3
                JDKUtils.putShortDirect(buf, off, ZERO_DOT_16); // 0.
                off += 2;
                if (e10 == -2) {
                    buf[off++] = '0';
                } else if (e10 == -3) {
                    JDKUtils.putShortDirect(buf, off, ZERO_ZERO_16); // 00
                    off += 2;
                }
                off = writeInt64(buf, off, value);
            } else {
                // 0 - 6
                int decimalPointPos = (digitCnt - 1) - e10;
                if (decimalPointPos > 0) {
                    int pos = decimalPointPos - 1;
                    long tl = POW10_LONG_VALUES[pos];
                    int pointBefore = (int) (value / tl);
                    off = writeInt32(buf, off, pointBefore);
                    buf[off++] = '.';
                    long pointAfter = value - pointBefore * tl;
                    // fill zeros
                    while (--pos > -1 && pointAfter < POW10_LONG_VALUES[pos]) {
                        buf[off++] = '0';
                    }
                    off = writeInt64(buf, off, pointAfter);
                } else {
                    off = writeInt64(buf, off, value);
                    int zeroCnt = -decimalPointPos;
                    if (zeroCnt > 0) {
                        for (int i = 0; i < zeroCnt; ++i) {
                            buf[off++] = '0';
                        }
                    }
                    JDKUtils.putShortDirect(buf, off, DOT_ZERO_16); // .0
                    off += 2;
                }
            }
        }

        return off;
    }

    // ==================== Integer writing helpers (byte[] only) ====================

    private static int writeInt3(byte[] buf, int off, int val) {
        int v = DIGITS_K_32[val & 0x3ff];
        putIntDirect(buf, off, v >> ((((byte) v) + 1) << 3));
        return off + 3 - (byte) v;
    }

    private static int writeInt4(byte[] buf, int off, int v) {
        int v1 = (int) (v * 1374389535L >> 37); // v / 100;
        int v0 = v - v1 * 100;
        int v2 = PACKED_DIGITS[v1 & 0x7f] | (PACKED_DIGITS[v0 & 0x7f] << 16);
        if (BIG_ENDIAN) {
            v2 = Integer.reverseBytes(v2);
        }
        putIntDirect(buf, off, v2);
        return off + 4;
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
        JDKUtils.putLongDirect(buf, off, v);
        return off + 8;
    }

    static int writeInt64(byte[] buf, int off, long val) {
        if (val < 0) {
            if (val == Long.MIN_VALUE) {
                System.arraycopy(MIN_LONG_BYTES, 0, buf, off, MIN_LONG_BYTES.length);
                return off + MIN_LONG_BYTES.length;
            }
            val = -val;
            buf[off++] = (byte) '-';
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

    static int writeInt32(byte[] buf, int off, long val) {
        if (val < 0) {
            val = -val;
            buf[off++] = (byte) '-';
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

    // ==================== putIntDirect (local helper using UNSAFE) ====================

    private static void putIntDirect(byte[] buf, int off, int v) {
        JDKUtils.putIntDirect(buf, off, v);
    }
}
