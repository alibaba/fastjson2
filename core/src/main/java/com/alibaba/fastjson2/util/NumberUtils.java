package com.alibaba.fastjson2.util;

import java.lang.invoke.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.ANDROID;

/**
 * Author: wangy
 */
public final class NumberUtils {
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
        if (JDKUtils.JVM_VERSION > 8 && !ANDROID) {
            try {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(NumberUtils.class);
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
        }
        if (function == null) {
            function = NumberUtils::multiplyHigh;
        }
        MULTIPLY_HIGH = function;
    }

    private NumberUtils() {
    }

    static final long INFI;
    static final long NITY;
    static final long INFINITY;

    static {
        String str = "Infinity";
        INFINITY = IOUtils.getLongUnaligned(str.getBytes(StandardCharsets.ISO_8859_1), 0);
        char[] chars = str.toCharArray();
        INFI = IOUtils.getLongUnaligned(chars, 0);
        NITY = IOUtils.getLongUnaligned(chars, 4);
    }

    static final double[] POSITIVE_DECIMAL_POWER = new double[325];
    static final double[] NEGATIVE_DECIMAL_POWER = new double[325];
    static final long[] POW10_LONG_VALUES = new long[]{
            10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L, 9223372036854775807L
    };

    static final long[] POW5_LONG_VALUES = new long[27];
    static final BigInteger[] POW5_BI_VALUES = new BigInteger[343];

    static {
        // e0 ~ e360(e306)
        for (int i = 0, len = POSITIVE_DECIMAL_POWER.length; i < len; ++i) {
            POSITIVE_DECIMAL_POWER[i] = Double.valueOf("1.0E" + i);
            NEGATIVE_DECIMAL_POWER[i] = Double.valueOf("1.0E-" + i);
        }
        // 4.9e-324
        NEGATIVE_DECIMAL_POWER[NEGATIVE_DECIMAL_POWER.length - 1] = Double.MIN_VALUE;

        long val = 1;
        for (int i = 0; i < POW5_LONG_VALUES.length; ++i) {
            POW5_LONG_VALUES[i] = val;
            val *= 5;
        }

        BigInteger five = BigInteger.valueOf(5);
        POW5_BI_VALUES[0] = BigInteger.ONE;
        for (int i = 1; i < POW5_BI_VALUES.length; ++i) {
            BigInteger pow5Value = five.pow(i);
            POW5_BI_VALUES[i] = pow5Value;
        }
    }

    static final int MOD_DOUBLE_EXP = (1 << 12) - 1;
    static final long MOD_DOUBLE_MANTISSA = (1L << 52) - 1;

    /**
     * multiplyOutput
     *
     * @param x > 0
     * @param y > 0
     * @param shift > 0
     * @return
     */
    static long multiplyHighAndShift(long x, long y, int shift) {
        long H = MULTIPLY_HIGH.multiplyHigh(x, y);
        if (shift >= 64) {
            int sr = shift - 64;
            return H >>> sr;
        }
        long L = x * y;
        return H << (64 - shift) | (L >>> shift);
    }

    /**
     * Unsigned algorithm:  (H * 2^64 * 2^n + L * 2^n + H1 * 2^64 + L1) / 2^(n+s) = (H * 2^64 + L + (H1 * 2^64 + L1) / 2^n) / 2^s
     * <p>
     * use BigInteger:  BigInteger.valueOf(pd.y).shiftLeft(n).add(BigInteger.valueOf(f & MASK_32_BITS)).multiply(BigInteger.valueOf(x)).shiftRight(s + n).longValue()
     *
     * @param x 63bits
     * @param y 63bits
     * @param y32 unsigned int32 f(32bits)
     * @param s s > 0
     * @return
     */
    static long multiplyHighAndShift(long x, long y, long y32, int s) {
        int sr = s - 64;
        long H = MULTIPLY_HIGH.multiplyHigh(x, y);
        long L = x * y;

        // cal x * f -> H1, L1
        long H1 = MULTIPLY_HIGH.multiplyHigh(x, y32);
        long L1 = x * y32;

        // carry = (H1 * 2^64 + L1) / 2^n
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

    /**
     * Conversion of ieee floating point numbers to Scientific notation
     *
     * <p> Using the difference estimation method </p>
     * <p> The output may not be the shortest, but the general result is correct </p>
     *
     * @param doubleValue &gt; 0
     */
    public static Scientific doubleToScientific(double doubleValue) {
        if (doubleValue == Double.MIN_VALUE) {
            // Double.MIN_VALUE The minimum double value converted by JDK is 4.9e-324. This method converts it to 5.0e-324.
            // Due to the special value, it is specially processed to be consistent with JDK conversion.
            return Scientific.DOUBLE_MIN;
        }
        long bits = Double.doubleToRawLongBits(doubleValue);
        int e2 = (int) (bits >> 52) & MOD_DOUBLE_EXP;
        long mantissa0 = bits & MOD_DOUBLE_MANTISSA;
        // boolean flagForUp = mantissa0 < MOD_DOUBLE_MANTISSA;
        boolean flagForDown = mantissa0 > 0;
        int e52;
        long output;
        long rawOutput, /*d2, */d3, d4;
        int e10, adl;
        if (e2 > 0) {
            if (e2 == 2047) {
                return Scientific.SCIENTIFIC_NULL;
            }
            mantissa0 = 1L << 52 | mantissa0;
            e52 = e2 - 1075;
        } else {
            int lz52 = Long.numberOfLeadingZeros(mantissa0) - 11;
            mantissa0 <<= lz52;
            e52 = -1074 - lz52;
        }
        boolean /*tflag = true,*/ accurate = false;
        if (e52 >= 0) {
            ED d = ED.E2_D_A[e52];
            e10 = d.e10;   // e10 > 15
            adl = d.adl;
            // d2 = d.d2;
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
            int o5 = d.o5;  // adl + 2 - e10
            int sb = e52 + o5;
            if (o5 < 0) {
                // mantissa0 * 2^(e52 + o5) * 5^o5 -> mantissa0 * 2^sb / 5^(-o5)
                ED5 d5 = ED5.ED5_A[-o5];
                int rb = sb - 10 - d5.ob;
                // rawOutput = BigInteger.valueOf(mantissa0).shiftLeft(sb).divide(POW5_BI_VALUES[-o5]).longValue();
                rawOutput = multiplyHighAndShift(mantissa0 << 10, d5.oy, d5.of, 32 - rb);
                accurate = o5 == -1 && sb < 11;
            } else {
                // o5 > 0 -> sb > 0
                // accurate
                rawOutput = (mantissa0 * POW5_LONG_VALUES[o5]) << sb;
                accurate = true;
            }
        } else {
            // e52 >= -1074 -> p5 <= 1074
            int e5 = -e52;
            ED d = ED.E5_D_A[e5];
            e10 = d.e10;
            adl = d.adl;
            // d2 = d.d2;
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

            int o5 = d.o5; // adl + 2 - e10; // o5 > 0
            int sb = o5 + e52;
            if (sb < 0) {
                if (o5 < POW5_LONG_VALUES.length) {
                    rawOutput = multiplyHighAndShift(mantissa0, POW5_LONG_VALUES[o5], -sb);
                } else if (o5 < POW5_LONG_VALUES.length + 4) {
                    rawOutput = multiplyHighAndShift(mantissa0 * POW5_LONG_VALUES[o5 - POW5_LONG_VALUES.length + 1], POW5_LONG_VALUES[POW5_LONG_VALUES.length - 1], -sb);
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
                rawOutput = (rawOutput + 5) / 10; // rawOutput = rawOutput / 10 + ((rawOutput % 10) >= 5 ? 1 : 0);
            }
            return new Scientific(rawOutput, adl + 2, e10);
        }

        // rem <= Actual Rem Value
        long div = rawOutput / 1000, rem = rawOutput - div * 1000;
        long remUp = (10001 - rem * 10) << 1;
        boolean up;
        if ((up = (remUp <= d4)) || ((rem + 1) << (flagForDown ? 1 : 2)) <= d3) {
            output = div + (up ? 1 : 0);
            --adl;
        } else {
            if (flagForDown) {
                output = (rawOutput + 50) / 100; // rawOutput / 100 + ((rawOutput % 100) >= 50 ? 1 : 0)
            } else {
                output = (rawOutput + 5) / 10; // rawOutput / 10 + ((rawOutput % 10) >= 5 ? 1 : 0)
                ++adl;
            }
        }
        return new Scientific(output, adl + 1, e10);
    }

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

    public static int writeDouble(byte[] buf, int off, double doubleValue, boolean json) {
        long bits;
        if (doubleValue == 0) {
            bits = Double.doubleToLongBits(doubleValue);
            if (bits == 0x8000000000000000L) {
                buf[off++] = '-';
            }
            buf[off] = '0';
            IOUtils.putShortUnaligned(buf, off + 1, DOT_ZERO_16);
            return off + 3;
        }
        boolean sign = doubleValue < 0;
        if (sign) {
            if (!json || doubleValue != Double.NEGATIVE_INFINITY) {
                buf[off++] = '-';
            }
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
        if (scientific == Scientific.SCIENTIFIC_NULL) {
            if (json) {
                IOUtils.putIntUnaligned(buf, off, IOUtils.NULL_32);
                return off + 4;
            } else {
                if (doubleValue == Double.POSITIVE_INFINITY) {
                    IOUtils.putLongUnaligned(buf, off, INFINITY);
                    return off + 8;
                } else {
                    buf[off] = 'N';
                    buf[off + 1] = 'a';
                    buf[off + 2] = 'N';
                    return off + 3;
                }
            }
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

    public static int writeDouble(char[] buf, int off, double doubleValue, boolean json) {
        long bits;
        if (doubleValue == 0) {
            bits = Double.doubleToLongBits(doubleValue);
            if (bits == 0x8000000000000000L) {
                buf[off++] = '-';
            }
            buf[off] = '0';
            IOUtils.putIntUnaligned(buf, off + 1, DOT_ZERO_32);
            return off + 3;
        }
        boolean sign = doubleValue < 0;
        if (sign) {
            if (!json || doubleValue != Double.NEGATIVE_INFINITY) {
                buf[off++] = '-';
            }
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
            return writeDecimal(scientific.output, scientific.count, e10, buf, off);
        }
        if (scientific == Scientific.SCIENTIFIC_NULL) {
            if (json) {
                IOUtils.putLongUnaligned(buf, off, NULL_64);
                return off + 4;
            } else {
                if (doubleValue == Double.POSITIVE_INFINITY) {
                    IOUtils.putLongUnaligned(buf, off, INFI);
                    IOUtils.putLongUnaligned(buf, off + 4, NITY);
                    return off + 8;
                } else {
                    buf[off] = 'N';
                    buf[off + 1] = 'a';
                    buf[off + 2] = 'N';
                    return off + 3;
                }
            }
        }
        if (e10 >= 0) {
            char[] chars = POSITIVE_DECIMAL_POWER_CHARS[e10];
            System.arraycopy(chars, 0, buf, off, chars.length);
            return off + chars.length;
        } else {
            char[] chars = NEGATIVE_DECIMAL_POWER_CHARS[-e10];
            System.arraycopy(chars, 0, buf, off, chars.length);
            return off + chars.length;
        }
    }

    public static int writeFloat(byte[] buf, int off, float floatValue, boolean json) {
        if (Float.isNaN(floatValue) || floatValue == Float.POSITIVE_INFINITY || floatValue == Float.NEGATIVE_INFINITY) {
            return writeSpecial(buf, off, floatValue, json);
        }
        int bits;
        if (floatValue == 0) {
            bits = Float.floatToIntBits(floatValue);
            if (bits == 0x80000000) {
                buf[off++] = '-';
            }
            buf[off] = '0';
            IOUtils.putShortUnaligned(buf, off + 1, DOT_ZERO_16);
            return off + 3;
        }
        boolean sign = floatValue < 0;
        if (sign) {
            buf[off++] = '-';
            floatValue = -floatValue;
        }

        Scientific scientific = floatToScientific(floatValue);
        return writeDecimal(scientific.output, scientific.count, scientific.e10, buf, off);
    }

    public static int writeFloat(char[] buf, int off, float floatValue, boolean json) {
        if (Float.isNaN(floatValue) || floatValue == Float.POSITIVE_INFINITY || floatValue == Float.NEGATIVE_INFINITY) {
            return writeSpecial(buf, off, floatValue, json);
        }
        int bits;
        if (floatValue == 0) {
            bits = Float.floatToIntBits(floatValue);
            if (bits == 0x80000000) {
                buf[off++] = '-';
            }
            buf[off] = '0';
            IOUtils.putIntUnaligned(buf, off + 1, DOT_ZERO_32); // .0
            return off + 3;
        }
        boolean sign = floatValue < 0;
        if (sign) {
            buf[off++] = '-';
            floatValue = -floatValue;
        }

        Scientific scientific = NumberUtils.floatToScientific(floatValue);
        return writeDecimal(scientific.output, scientific.count, scientific.e10, buf, off);
    }

    private static int writeSpecial(byte[] buf, int off, float floatValue, boolean json) {
        if (json) {
            IOUtils.putIntUnaligned(buf, off, NULL_32);
            return off + 4;
        }

        if (Float.isNaN(floatValue)) {
            buf[off] = 'N';
            buf[off + 1] = 'a';
            buf[off + 2] = 'N';
            return off + 3;
        }

        if (floatValue == Float.NEGATIVE_INFINITY) {
            buf[off++] = '-';
        }

        IOUtils.putLongUnaligned(buf, off, INFINITY);
        return off + 8;
    }

    private static int writeSpecial(char[] buf, int off, float floatValue, boolean json) {
        if (json) {
            IOUtils.putLongUnaligned(buf, off, NULL_64);
            return off + 4;
        }

        if (Float.isNaN(floatValue)) {
            buf[off] = 'N';
            buf[off + 1] = 'a';
            buf[off + 2] = 'N';
            return off + 3;
        }

        if (floatValue == Float.NEGATIVE_INFINITY) {
            buf[off++] = '-';
        }

        IOUtils.putLongUnaligned(buf, off, INFI);
        IOUtils.putLongUnaligned(buf, off + 4, NITY);
        return off + 8;
    }

    static final int MOD_FLOAT_EXP = (1 << 9) - 1;
    static final int MOD_FLOAT_MANTISSA = (1 << 23) - 1;

    public static Scientific floatToScientific(float floatValue) {
        final int bits = Float.floatToRawIntBits(floatValue);
        int e2 = (bits >> 23) & MOD_FLOAT_EXP;
        int mantissa0 = bits & MOD_FLOAT_MANTISSA;
        boolean nonZeroFlag = mantissa0 > 0;
        int e23;
        long output, rawOutput;
        long d4;
        int e10, adl;
        boolean accurate = false;
        if (e2 > 0) {
            mantissa0 = 1 << 23 | mantissa0;
            e23 = e2 - 150;   // 1023 - 52
        } else {
            // e2 == 0
            int l = Integer.numberOfLeadingZeros(mantissa0) - 8;
            mantissa0 <<= l;
            e23 = -149 - l;
        }
        if (e23 >= 0) {
            ED d = EF.E2_F_A[e23];
            e10 = d.e10;
            adl = d.adl;
            d4 = d.d4;
            if (d.b && mantissa0 > d.bv) {
                ++e10;
                ++adl;
            }
            int o5 = d.o5 + 6;  // Compared to double (adl + 2 - e10), adding 6 more numbers increases the probability of hitting
            int sb = e23 + o5;
            if (o5 < 0) {
                // mantissa0 * 2^(e23 + o5) * 5^o5 -> mantissa0 * 2^sb / 5^(-o5)
                // rawOutput = BigInteger.valueOf(mantissa0).shiftLeft(sb).divide(POW5_BI_VALUES[-o5]).longValue();
                if (sb < 40) {
                    rawOutput = ((long) mantissa0 << sb) / POW5_LONG_VALUES[-o5];
                } else {
                    ED5 d5 = ED5.ED5_A[-o5];
                    rawOutput = multiplyHighAndShift((long) mantissa0 << 39, d5.oy, d5.of, 71 + d5.ob - sb);
                }
            } else {
                // o5 > 0 -> sb > 0
                // accurate
                rawOutput = mantissa0 * POW5_LONG_VALUES[o5] << sb;
                accurate = true;
            }
        } else {
            // e52 >= -149 -> p5 <= 149
            int e5 = -e23;
            ED d = EF.E5_F_A[e5];
            e10 = d.e10;
            adl = d.adl;
            d4 = d.d4;
            if (d.b && mantissa0 > d.bv) {
                ++e10;
                ++adl;
            }

            int o5 = d.o5 + 6; // Compared to double (adl + 2 - e10), adding 6 more numbers increases the probability of hitting
            int sb = o5 + e23;
            if (sb < 0) {
                // todo To be optimized
                if (o5 < 17) {
                    rawOutput = mantissa0 * POW5_LONG_VALUES[o5] >> -sb;
                } else if (o5 < POW5_LONG_VALUES.length) {
                    rawOutput = multiplyHighAndShift(mantissa0, POW5_LONG_VALUES[o5], -sb);
                } else if (o5 < POW5_LONG_VALUES.length + 4) {
                    rawOutput = multiplyHighAndShift(mantissa0 * POW5_LONG_VALUES[o5 - POW5_LONG_VALUES.length + 1], POW5_LONG_VALUES[POW5_LONG_VALUES.length - 1], -sb);
                } else {
                    ED5 ed5 = ED5.ED5_A[o5];
                    rawOutput = multiplyHighAndShift((long) mantissa0 << 39, ed5.y, ed5.f, -(ed5.dfb + sb) + 39);   // 39 = 63 - 24
                }
            } else {
                rawOutput = POW5_LONG_VALUES[o5] * mantissa0 << sb;
                accurate = true;
            }
        }

        if (accurate) {
            // If we pursue performance, we can return it here, but it may return a non-shortest sequence of numbers (the result is correct)
            // rawOutput = EnvUtils.JDK_AGENT_INSTANCE.multiplyHighKaratsuba(rawOutput, 0x6b5fca6af2bd215fL) >> 22; // rawOutput / 10000000;
            // if (adl == 7) {
            //     --adl;
            //     rawOutput = (rawOutput + 5) / 10; // rawOutput = rawOutput / 10 + ((rawOutput % 10) >= 5 ? 1 : 0);
            // }
            // return new Scientific(rawOutput, adl + 2, e10);
        }

        if (rawOutput < 1000000000) {
            return new Scientific(rawOutput / 10000000, 2, e10);
        }
        long div = MULTIPLY_HIGH.multiplyHigh(rawOutput, 0x44b82fa09b5a52ccL) >> 28; // rawOutput / 1000000000;
        long rem = rawOutput - div * 1000000000;
        long remUp = (1000000001 - rem) << 1;
        boolean up = remUp <= d4;
        if (up || ((rem + 1) << (nonZeroFlag ? 1 : 2)) <= d4) {
            output = div + (up ? 1 : 0);
            --adl;
            if (up) {
                if (POW10_LONG_VALUES[adl] == output) {
                    ++e10;
                    output = 1;
                    adl = 0;
                }
            }
        } else {
            if (nonZeroFlag) {
                long div0 = MULTIPLY_HIGH.multiplyHigh(rawOutput, 0x55e63b88c230e77fL) >> 25; // rawOutput / 100000000
                output = div0 + (rem % 100000000 >= 50000000 ? 1 : 0);
            } else {
                long div0 = MULTIPLY_HIGH.multiplyHigh(rawOutput, 0x6b5fca6af2bd215fL) >> 22; // rawOutput / 10000000
                output = div0 + (rem % 10000000 >= 5000000 ? 1 : 0);
                ++adl;
            }
        }

        return new Scientific(output, adl + 1, e10);
    }

    static final int[] TWO_DIGITS_32_BITS = new int[100];
    static final short[] TWO_DIGITS_16_BITS = new short[100];

    static {
        for (long d1 = 0; d1 < 10; ++d1) {
            for (long d2 = 0; d2 < 10; ++d2) {
                long intVal64;
                int intVal32;
                if (JDKUtils.BIG_ENDIAN) {
                    intVal64 = (d1 + 48) << 16 | (d2 + 48);
                    intVal32 = ((int) d1 + 48) << 8 | ((int) d2 + 48);
                } else {
                    intVal64 = (d2 + 48) << 16 | (d1 + 48);
                    intVal32 = ((int) d2 + 48) << 8 | ((int) d1 + 48);
                }
                int k = (int) (d1 * 10 + d2);
                TWO_DIGITS_32_BITS[k] = (int) intVal64;
                TWO_DIGITS_16_BITS[k] = (short) intVal32;
            }
        }
    }

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
        boolean useScientific = e10 < -3 || e10 >= 7; // !((e10 >= -3) && (e10 < 7));
        if (useScientific) {
            if (digitCnt == 1) {
                buf[off] = (byte) (value + 48);
                IOUtils.putShortUnaligned(buf, off + 1, DOT_ZERO_16);
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
                off = IOUtils.writeInt64(buf, off, pointAfter);
            }
            buf[off++] = 'E';
            if (e10 < 0) {
                buf[off++] = '-';
                e10 = -e10;
            }
            if (e10 > 99) {
                int n = e10 / 100;
                buf[off] = (byte) (n + 48);
                e10 = e10 - n * 100;
                IOUtils.putShortUnaligned(buf, off + 1, TWO_DIGITS_16_BITS[e10]);
                off += 3;
            } else {
                if (e10 > 9) {
                    IOUtils.putShortUnaligned(buf, off, TWO_DIGITS_16_BITS[e10]);
                    off += 2;
                } else {
                    buf[off++] = (byte) (e10 + 48);
                }
            }
        } else {
            // for non-scientific notation such as 12345, write a decimal point when size = decimalExp.
            if (e10 < 0) {
                // -1/-2/-3
                IOUtils.putShortUnaligned(buf, off, ZERO_DOT_16); // 0.
                off += 2;
                if (e10 == -2) {
                    buf[off++] = '0';
                } else if (e10 == -3) {
                    IOUtils.putShortUnaligned(buf, off, ZERO_ZERO_16); // 00
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
                    IOUtils.putShortUnaligned(buf, off, DOT_ZERO_16); // .0
                    off += 2;
                }
            }
        }

        return off;
    }

    private static int writeDecimal(long value, int digitCnt, int e10, char[] buf, int off) {
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
        // whether to use scientific notation
        boolean useScientific = e10 < -3 || e10 >= 7; // !((e10 >= -3) && (e10 < 7));
        if (useScientific) {
            if (digitCnt == 1) {
                buf[off] = (char) (value + 48);
                IOUtils.putIntUnaligned(buf, off + 1, DOT_ZERO_32);
                off += 3;
            } else {
                int pos = digitCnt - 2;
                // get the first digit
                long tl = POW10_LONG_VALUES[pos];
                int fd = (int) (value / tl);
                buf[off] = (char) (fd + 48);
                buf[off + 1] = '.';
                off += 2;

                long pointAfter = value - fd * tl;
                // fill zeros
                while (--pos > -1 && pointAfter < POW10_LONG_VALUES[pos]) {
                    buf[off++] = '0';
                }
                off = IOUtils.writeInt64(buf, off, pointAfter);
            }
            buf[off++] = 'E';
            if (e10 < 0) {
                buf[off++] = '-';
                e10 = -e10;
            }
            if (e10 > 99) {
                int n = e10 / 100;
                buf[off] = (char) (n + 48);
                e10 = e10 - n * 100;
                IOUtils.putIntUnaligned(buf, off + 1, TWO_DIGITS_32_BITS[e10]);
                off += 3;
            } else {
                if (e10 > 9) {
                    IOUtils.putIntUnaligned(buf, off, TWO_DIGITS_32_BITS[e10]);
                    off += 2;
                } else {
                    buf[off++] = (char) (e10 + 48);
                }
            }
        } else {
            // for non-scientific notation such as 12345, write a decimal point when size = decimalExp.
            if (e10 < 0) {
                // -1/-2/-3
                IOUtils.putIntUnaligned(buf, off, ZERO_DOT_32); // 0.
                off += 2;
                if (e10 == -2) {
                    buf[off++] = '0';
                } else if (e10 == -3) {
                    IOUtils.putIntUnaligned(buf, off, ZERO_ZERO_32); // 00
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
                    off = writeInt64(buf, off, pointBefore);
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
                    IOUtils.putIntUnaligned(buf, off, DOT_ZERO_32); // .0
                    off += 2;
                }
            }
        }

        return off;
    }
}
