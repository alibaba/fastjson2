package com.alibaba.fastjson2.benchmark.fastcode;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DecimalUtils {
    public static String toString(long unscaledVal, int scale) {
        if (scale == 0) {
            return Long.toString(unscaledVal);
        }

        boolean negative = false;
        if (unscaledVal < 0) {
            unscaledVal = -unscaledVal;
            negative = true;
        }

        int size = stringSize(unscaledVal);

        byte[] buf;
        int off = 0;
        if (scale < 0) {
            buf = new byte[size - scale + (negative ? 1 : 0)];
            if (negative) {
                buf[0] = '-';
                off = 1;
            }
            getChars(unscaledVal, off + size, buf);
            Arrays.fill(buf, off + size, buf.length, (byte) '0');
        } else {
            int insertionPoint = size - scale;
            if (insertionPoint <= 0) {
                buf = new byte[size + 2 - insertionPoint + (negative ? 1 : 0)];
                if (negative) {
                    buf[0] = '-';
                    off = 1;
                }
                buf[off] = '0';
                buf[off + 1] = '.';

                for (int i = 0; i < -insertionPoint; i++) {
                    buf[off + i + 2] = '0';
                }
                getChars(unscaledVal, buf.length, buf);
            } else {
                long power = POWER_TEN[scale - 1];
                long div = unscaledVal / power;
                long rem = unscaledVal - div * power;
                int divSize = size - scale;
                buf = new byte[size + 1 + (negative ? 1 : 0)];
                if (negative) {
                    buf[0] = '-';
                    off = 1;
                }
                getChars(div, off + divSize, buf);
                buf[divSize + off] = '.';
                getChars(rem, buf.length, buf);
            }
        }

        return new String(buf);
    }

    @SuppressWarnings("deprecated")
    public static String toString(BigInteger unscaledVal, int scale) {
        if (scale == 0) {
            return unscaledVal.toString(10);
        }

        boolean negative = false;
        if (unscaledVal.signum() < 0) {
            negative = true;
            unscaledVal = unscaledVal.negate();
        }

        String unscaledValString = unscaledVal.toString(10);

        int size = unscaledValString.length();

        byte[] buf;
        int off = 0;
        if (scale < 0) {
            buf = new byte[size - scale + (negative ? 1 : 0)];
            if (negative) {
                buf[0] = '-';
                off = 1;
            }
            unscaledValString.getBytes(0, size, buf, off);
            Arrays.fill(buf, off + size, buf.length, (byte) '0');
        } else {
            int insertionPoint = size - scale;
            if (insertionPoint <= 0) {
                buf = new byte[size + 2 - insertionPoint + (negative ? 1 : 0)];
                if (negative) {
                    buf[0] = '-';
                    off = 1;
                }
                buf[off] = '0';
                buf[off + 1] = '.';
                off += 2;

                for (int i = 0; i < -insertionPoint; i++) {
                    buf[off++] = '0';
                }

                unscaledValString.getBytes(0, size, buf, off);
            } else {
                buf = new byte[size + (negative ? 2 : 1)];
                if (negative) {
                    buf[0] = '-';
                    off = 1;
                }

                unscaledValString.getBytes(0, insertionPoint, buf, off);
                off += insertionPoint;
                buf[off] = '.';
                unscaledValString.getBytes(insertionPoint, size, buf, off + 1);
            }
        }

        return new String(buf);
    }

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

    static String layout(long intCompact, int scale, boolean sci) {
        if (scale == 0) {
            return Long.toString(intCompact);
        }

        long unscaledVal;
        boolean negative = false;
        if (intCompact < 0) {
            unscaledVal = -intCompact;
            negative = true;
        } else {
            unscaledVal = intCompact;
        }

        int coeffLen = stringSize(unscaledVal);
        long adjusted = -(long) scale + (coeffLen - 1);

        byte[] buf;
        int off = 0;
        if ((scale >= 0) && (adjusted >= -6)) {
            int pad = scale - coeffLen;
            if (pad >= 0) {
                buf = new byte[coeffLen + 2 + pad + (negative ? 1 : 0)];
                if (negative) {
                    buf[0] = '-';
                    off = 1;
                }
                buf[off] = '0';
                buf[off + 1] = '.';
                off += 2;
                for (int i = 0; i < pad; i++) {
                    buf[off++] = '0';
                }
                getChars(unscaledVal, buf.length, buf);
            } else {
                buf = new byte[coeffLen + 1 + (negative ? 1 : 0)];
                if (negative) {
                    buf[0] = '-';
                    off = 1;
                }

                long power = POWER_TEN[scale - 1];
                long div = unscaledVal / power;
                long rem = unscaledVal - div * power;
                getChars(div, off + coeffLen - scale, buf);
                buf[off + coeffLen - scale] = '.';
                getChars(rem, off + coeffLen + 1, buf);
            }
        } else {
            if (sci) {
                if (coeffLen > 1) {
                    int adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                    buf = new byte[coeffLen + adjustedSize + 1 + (negative ? 1 : 0)];
                    if (negative) {
                        buf[0] = '-';
                        off = 1;
                    }
                    long power = POWER_TEN[coeffLen - 2];
                    long div = unscaledVal / power;
                    long rem = unscaledVal - div * power;
                    buf[off] = (byte) (div + '0');
                    buf[off + 1] = '.';
                    getChars(rem, off + coeffLen + 1, buf);
                    off += coeffLen + 1;
                } else {
                    int adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                    buf = new byte[adjustedSize + (negative ? 2 : 1)];
                    if (negative) {
                        buf[0] = '-';
                        off = 1;
                    }
                    buf[off++] = (byte) (unscaledVal + '0');
                }
            } else {
                int sig = (int) (adjusted % 3);
                if (sig < 0) {
                    sig += 3;                // [adjusted was negative]
                }
                adjusted -= sig;             // now a multiple of 3
                sig++;

                int adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                if (unscaledVal == 0) {
                    switch (sig) {
                        case 1: {
                            buf = new byte[adjustedSize + 1];
                            buf[0] = '0'; // exponent is a multiple of three
                            off = 1;
                            break;
                        }
                        case 2: {
                            adjusted += 3;
                            adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                            buf = new byte[adjustedSize + 4];
                            buf[0] = '0';
                            buf[1] = '.';
                            buf[2] = '0';
                            buf[3] = '0';
                            off = 4;
                            break;
                        }
                        case 3: {
                            adjusted += 3;
                            adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                            buf = new byte[adjustedSize + 3];
                            buf[0] = '0';
                            buf[1] = '.';
                            buf[2] = '0';
                            off = 3;
                            break;
                        }
                        default:
                            throw new AssertionError("Unexpected sig value " + sig);
                    }
                } else if (sig >= coeffLen) {
                    buf = new byte[adjustedSize + (negative ? 2 : 1) + sig - coeffLen];
                    if (negative) {
                        buf[0] = '-';
                        off = 1;
                    }
                    getChars(unscaledVal, off + coeffLen, buf);
                    off += coeffLen;
                    for (int i = sig - coeffLen; i > 0; i--) {
                        buf[off++] = '0';
                    }
                } else {
                    buf = new byte[adjustedSize + (negative ? 2 : 1) + coeffLen];
                    if (negative) {
                        buf[0] = '-';
                        off = 1;
                    }

                    long power = POWER_TEN[coeffLen - sig - 1];
                    long div = unscaledVal / power;
                    long rem = unscaledVal - div * power;
                    getChars(div, off + sig, buf);
                    buf[off + sig] = '.';
                    getChars(rem, off + coeffLen + 1, buf);
                    off += coeffLen + 1;
                }
            }

            if (adjusted != 0) {             // [!sci could have made 0]
                buf[off++] = 'E';
                buf[off] = (byte) (adjusted > 0 ? '+' : '-');
                getChars(Math.abs(adjusted), buf.length, buf);
            }
        }

        return new String(buf);
    }

    static String layout(BigInteger intVal, int scale, boolean sci) {
        if (scale == 0) {
            return intVal.toString();
        }

        BigInteger unscaledVal;
        boolean negative = false;
        int signum = intVal.signum();
        if (signum < 0) {
            unscaledVal = intVal.negate();
            negative = true;
        } else {
            unscaledVal = intVal;
        }

        byte[] buf;
        int off = 0;
        String unscaledValString = unscaledVal.toString(10);
        byte[] coeff = unscaledValString.getBytes(StandardCharsets.ISO_8859_1);
        int coeffLen = coeff.length;
        long adjusted = -(long) scale + (coeffLen - 1);
        if ((scale >= 0) && (adjusted >= -6)) {
            int pad = scale - coeffLen;
            if (pad >= 0) {
                buf = new byte[coeffLen + 2 + pad + (negative ? 1 : 0)];
                if (negative) {
                    buf[0] = '-';
                    off = 1;
                }
                buf[off] = '0';
                buf[off + 1] = '.';
                off += 2;
                for (int i = 0; i < pad; i++) {
                    buf[off++] = '0';
                }
                System.arraycopy(coeff, 0, buf, off, coeffLen);
            } else {
                buf = new byte[coeffLen + 1 + (negative ? 1 : 0)];
                if (negative) {
                    buf[0] = '-';
                    off = 1;
                }

                System.arraycopy(coeff, 0, buf, off, -pad);
                buf[off - pad] = '.';
                System.arraycopy(coeff, -pad, buf, off - pad + 1, scale);
            }
        } else {
            if (sci) {                       // Scientific notation
                if (coeffLen > 1) {
                    int adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                    buf = new byte[coeffLen + adjustedSize + 1 + (negative ? 1 : 0)];
                    if (negative) {
                        buf[0] = '-';
                        off = 1;
                    }
                    buf[off] = coeff[0];
                    buf[off + 1] = '.';
                    System.arraycopy(coeff, 1, buf, off + 2, coeffLen - 1);
                    off += coeffLen + 1;
                } else {
                    int adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                    buf = new byte[adjustedSize + (negative ? 2 : 1)];
                    if (negative) {
                        buf[0] = '-';
                        off = 1;
                    }
                    buf[off++] = coeff[0];
                }
            } else {
                int sig = (int) (adjusted % 3);
                if (sig < 0) {
                    sig += 3;                // [adjusted was negative]
                }
                adjusted -= sig;             // now a multiple of 3
                sig++;

                int adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                if (signum == 0) {
                    switch (sig) {
                        case 1: {
                            buf = new byte[adjustedSize + 1];
                            buf[0] = '0'; // exponent is a multiple of three
                            off = 1;
                            break;
                        }
                        case 2: {
                            adjusted += 3;
                            adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                            buf = new byte[adjustedSize + 4];
                            buf[0] = '0';
                            buf[1] = '.';
                            buf[2] = '0';
                            buf[3] = '0';
                            off = 4;
                            break;
                        }
                        case 3: {
                            adjusted += 3;
                            adjustedSize = adjusted != 0 ? stringSize(Math.abs(adjusted)) + 2 : 0;
                            buf = new byte[adjustedSize + 3];
                            buf[0] = '0';
                            buf[1] = '.';
                            buf[2] = '0';
                            off = 3;
                            break;
                        }
                        default:
                            throw new AssertionError("Unexpected sig value " + sig);
                    }
                } else if (sig >= coeffLen) {
                    buf = new byte[adjustedSize + (negative ? 2 : 1) + sig - coeffLen];
                    if (negative) {
                        buf[0] = '-';
                        off = 1;
                    }
                    System.arraycopy(coeff, 0, buf, off, coeffLen);
                    off += coeffLen;
                    for (int i = sig - coeffLen; i > 0; i--) {
                        buf[off++] = '0';
                    }
                } else {
                    buf = new byte[adjustedSize + (negative ? 2 : 1) + coeffLen];
                    if (negative) {
                        buf[0] = '-';
                        off = 1;
                    }

                    System.arraycopy(coeff, 0, buf, off, sig);
                    buf[off + sig] = '.';
                    System.arraycopy(coeff, sig, buf, off + sig + 1, coeffLen - sig);
                    off += coeffLen + 1;
                }
            }
            if (adjusted != 0) {             // [!sci could have made 0]
                buf[off++] = 'E';
                buf[off] = (byte) (adjusted > 0 ? '+' : '-');
                getChars(Math.abs(adjusted), buf.length, buf);
            }
        }

        return new String(buf);
    }

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

    public static final short[] PACKED_DIGITS = new short[]{
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

    static void getChars(long i, int index, byte[] buf) {
        long q;
        int charPos = index;

        boolean negative = (i < 0);
        if (negative) {
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            short v = PACKED_DIGITS[(int) (i - q * 100)];
            buf[--charPos] = (byte) (v >> 8);
            buf[--charPos] = (byte) v;
            i = q;
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 100) {
            q2 = i2 / 100;
            short v = PACKED_DIGITS[i2 - q2 * 100];
            buf[--charPos] = (byte) (v >> 8);
            buf[--charPos] = (byte) v;
            i2 = q2;
        }

        // We know there are at most two digits left at this point.
        if (i2 > 9) {
            short v = PACKED_DIGITS[i2];
            buf[--charPos] = (byte) (v >> 8);
            buf[--charPos] = (byte) v;
        } else {
            buf[--charPos] = (byte) ('0' + i2);
        }

        if (negative) {
            buf[charPos - 1] = (byte) '-';
        }
    }
}
