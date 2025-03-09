package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;

final class MutableBigInteger {
    private static final int[][] BIG_TEN_POWERS_MAGIC_TABLE = {
            {1},
            {10},
            {100},
            {1000},
            {10000},
            {100000},
            {1000000},
            {10000000},
            {100000000},
            {1000000000},
            {2, 1410065408},
            {23, 1215752192},
            {232, -727379968},
            {2328, 1316134912},
            {23283, 276447232},
            {232830, -1530494976},
            {2328306, 1874919424},
            {23283064, 1569325056},
            {232830643, -1486618624},
            {-1966660860, -1981284352},
            {5, 1808227885, 1661992960},
            {54, 902409669, -559939584},
            {542, 434162106, -1304428544},
            {5421, 46653770, -159383552},
            {54210, 466537709, -1593835520},
            {542101, 370409800, 1241513984},
            {5421010, -590869294, -469762048},
            {54210108, -1613725636, -402653184},
            {542101086, 1042612833, 268435456},
            {1, 1126043566, 1836193738, -1610612736},
            {12, -1624466224, 1182068202, 1073741824},
            {126, 935206946, -1064219866, -2147483648},
            {1262, 762134875, -2052264063, 0},
            {12621, -968585837, 952195850, 0},
            {126217, -1095923776, 932023908, 0},
            {1262177, 1925664130, 730304488, 0},
            {12621774, 2076772117, -1286889712, 0},
            {126217744, -707115303, 16004768, 0},
            {1262177448, 1518781562, 160047680, 0},
            {2, -263127405, -1992053564, 1600476800, 0},
            {29, 1663693251, 1554300843, -1175101184, 0},
            {293, -542936671, -1636860747, 1133890048, 0},
            {2938, -1134399408, 811261716, -1546001408, 0},
            {29387, 1540907809, -477317426, 1719855104, 0},
            {293873, -1770791086, -478206960, 18681856, 0},
            {2938735, -528041668, -487102304, 186818560, 0},
            {29387358, -985449376, -576055744, 1868185600, 0},
            {293873587, -1264559160, -1465590140, 1501986816, 0},
            {-1356231419, 239310294, -1770999509, 2134966272, 0},
            {6, -677412302, -1901864351, -530125902, -125173760, 0},
            {68, 1815811577, -1838774318, -1006291715, -1251737600, 0},
            {684, 978246591, -1207873989, -1472982551, 367525888, 0},
            {6842, 1192531325, 806162004, -1844923622, -619708416, 0},
            {68422, -959588637, -528314547, -1269367028, -1902116864, 0},
            {684227, -1005951770, -988178167, 191231613, -1841299456, 0},
            {6842277, -1469583101, -1291847078, 1912316135, -1233125376, 0},
            {68422776, -1810929116, -33568888, 1943292173, 553648128, 0},
            {684227765, -929421967, -335688876, -2041914749, 1241513984, 0},
            {1, -1747656935, -704285069, 938078541, 1055688992, -469762048, 0},
            {15, -296700158, 1547083904, 790850820, 1966955336, -402653184, 0},
            {159, 1327965719, -1709030143, -681426388, -1805283111, 268435456, 0},
            {1593, 394755308, 89567762, 1775670717, -872961926, -1610612736, 0},
            {15930, -347414216, 895677624, 576837993, -139684662, 1073741824, 0},
            {159309, 820825138, 366841649, 1473412643, -1396846618, -2147483648, 0}
    };

    static final long LONG_MASK = 0xffffffffL;
    static final int KNUTH_POW2_THRESH_LEN = 6;
    static final int KNUTH_POW2_THRESH_ZEROS = 3;

    static long divideKnuthLong(long intCompact, int ql, int scale) {
        int[] pow10 = MutableBigInteger.BIG_TEN_POWERS_MAGIC_TABLE[scale];
        int[] magic_a, magic_b;
        int v0 = (int) (intCompact >>> 32), v1 = (int) intCompact;
        if (ql <= 0) {
            int n = -ql;
            int nInts = n >>> 5;
            int nBits = n & 0x1f;
            if (nBits == 0) {
                magic_a = new int[2 + nInts];
                magic_a[0] = v0;
                magic_a[1] = v1;
            } else {
                int i = 0;
                int nBits2 = 32 - nBits;
                int highBits = v0 >>> nBits2;
                if (highBits != 0) {
                    magic_a = new int[3 + nInts];
                    magic_a[i++] = highBits;
                } else {
                    magic_a = new int[2 + nInts];
                }
                magic_a[i] = v0 << nBits | v1 >>> nBits2;
                magic_a[i + 1] = v1 << nBits;
            }
            magic_b = pow10;
        } else {
            magic_a = new int[]{v0, v1};
            magic_b = shiftLeft(pow10, ql);
        }

        if (magic_a.length < magic_b.length) {
            return 0;
        }

        if (magic_a.length == magic_b.length && equals(magic_a, magic_b)) {
            return 1;
        }

        // Special case one word divisor
        if (magic_b.length == 1) {
            return divideOneWordLong(magic_a, magic_b[0]);
        }

        // Cancel common powers of two if we're above the KNUTH_POW2_* thresholds
        if (magic_a.length >= KNUTH_POW2_THRESH_LEN) {
            int trailingZeroBits = Math.min(getLowestSetBit(magic_a), getLowestSetBit(magic_b));
            if (trailingZeroBits >= KNUTH_POW2_THRESH_ZEROS * 32) {
                throw new JSONException("assert error");
            }
        }

        int intLen = magic_a.length;
        // assert div.intLen > 1
        // D1 normalize the divisor
        int shift = Integer.numberOfLeadingZeros(magic_b[0]);
        // Copy divisor value to protect divisor
        final int dlen = magic_b.length;
        int[] divisor;
        int[] remarr;
        int nlen = intLen;
        if (shift > 0) {
            divisor = new int[dlen];
            primitiveLeftShift(magic_b, shift, divisor, 0);
            if (Integer.numberOfLeadingZeros(magic_a[0]) >= shift) {
                remarr = new int[intLen + 1];
                primitiveLeftShift(magic_a, shift, remarr, 1);
            } else {
                remarr = new int[intLen + 2];
                nlen++;
                int rFrom = 0;
                int c = 0;
                int n2 = 32 - shift;
                for (int i = 1; i < intLen + 1; i++, rFrom++) {
                    int b = c;
                    c = magic_a[rFrom];
                    remarr[i] = (b << shift) | (c >>> n2);
                }
                remarr[intLen + 1] = c << shift;
            }
        } else {
            divisor = magic_b.clone();
            remarr = new int[intLen + 1];
            System.arraycopy(magic_a, 0, remarr, 1, intLen);
        }

        final int limit = nlen - dlen + 1;
        int[] q = new int[limit];

        // Insert leading 0 in rem
        remarr[0] = 0;

        int dh = divisor[0];
        long dhLong = dh & LONG_MASK;
        int dl = divisor[1];

        // D2 Initialize j
        for (int j = 0; j < limit - 1; j++) {
            // D3 Calculate qhat
            // estimate qhat
            int qhat;
            int qrem;
            boolean skipCorrection = false;
            int nh = remarr[j];
            int nh2 = nh + 0x80000000;
            int nm = remarr[j + 1];

            if (nh == dh) {
                qhat = ~0;
                qrem = nh + nm;
                skipCorrection = qrem + 0x80000000 < nh2;
            } else {
                long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
                qhat = (int) Long.divideUnsigned(nChunk, dhLong);
                qrem = (int) Long.remainderUnsigned(nChunk, dhLong);
            }

            if (qhat == 0) {
                continue;
            }

            if (!skipCorrection) { // Correct qhat
                long nl = remarr[j + 2] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) ((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >= dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs)) {
                            qhat--;
                        }
                    }
                }
            }

            // D4 Multiply and subtract
            remarr[j] = 0;
            int borrow; // = mulsub(remarr, divisor, qhat, dlen, j);
            {
                long xLong = qhat & LONG_MASK;
                long carry = 0;
                int offset = j + dlen;

                for (int k = dlen - 1; k >= 0; k--) {
                    long product = (divisor[k] & LONG_MASK) * xLong + carry;
                    long difference = remarr[offset] - product;
                    remarr[offset--] = (int) difference;
                    carry = (product >>> 32)
                            + (((difference & LONG_MASK) >
                            (((~(int) product) & LONG_MASK))) ? 1 : 0);
                }
                borrow = (int) carry;
            }

            // D5 Test remainder
            if (borrow + 0x80000000 > nh2) {
                // D6 Add back
                divadd(divisor, remarr, j + 1);
                qhat--;
            }

            // Store the quotient digit
            q[j] = qhat;
        } // D7 loop on j
        // D3 Calculate qhat
        // estimate qhat
        int qhat;
        int qrem;
        boolean skipCorrection = false;
        int nh = remarr[limit - 1];
        int nh2 = nh + 0x80000000;
        int nm = remarr[limit];

        if (nh == dh) {
            qhat = ~0;
            qrem = nh + nm;
            skipCorrection = qrem + 0x80000000 < nh2;
        } else {
            long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
            qhat = (int) Long.divideUnsigned(nChunk, dhLong);
            qrem = (int) Long.remainderUnsigned(nChunk, dhLong);
        }
        if (qhat != 0) {
            if (!skipCorrection) { // Correct qhat
                long nl = remarr[limit + 1] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) ((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >= dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs)) {
                            qhat--;
                        }
                    }
                }
            }

            // D4 Multiply and subtract
            int borrow;
            remarr[limit - 1] = 0;
            {
                // mulsubBorrow
                int offset = limit - 1 + dlen;
                long xLong = qhat & LONG_MASK;
                long carry = 0;
                for (int j = dlen - 1; j >= 0; j--) {
                    long product = (divisor[j] & LONG_MASK) * xLong + carry;
                    long difference = remarr[offset--] - product;
                    carry = (product >>> 32)
                            + (((difference & LONG_MASK) >
                            (((~(int) product) & LONG_MASK))) ? 1 : 0);
                }
                borrow = (int) carry;
            }

            // D5 Test remainder
            if (borrow + 0x80000000 > nh2) {
                // D6 Add back
                qhat--;
            }

            // Store the quotient digit
            q[(limit - 1)] = qhat;
        }

        int index = 0;
        while (index < limit && q[index] == 0) {
            index++;
        }
        if (limit == index) {
            return 0;
        }
        return ((q[limit - 2] & LONG_MASK) << 32) + (q[limit - 1] & LONG_MASK);
    }

    private static boolean equals(int[] magic_a, int[] magic_b) {
        boolean equals = true;
        // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
        // comparison.
        for (int i = 0, j = 0; i < magic_a.length; i++, j++) {
            if (magic_a[i] + 0x80000000 != magic_b[j] + 0x80000000) {
                return false;
            }
        }
        return true;
    }

    private static int[] shiftLeft(int[] mag, int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int[] newMag;
        if (nBits == 0) {
            newMag = new int[magLen + nInts];
            System.arraycopy(mag, 0, newMag, 0, magLen);
        } else {
            int i = 0;
            int nBits2 = 32 - nBits;
            int highBits = mag[0] >>> nBits2;
            if (highBits != 0) {
                newMag = new int[magLen + nInts + 1];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen + nInts];
            }
            int j = 0;
            while (j < magLen - 1) {
                newMag[i++] = mag[j++] << nBits | mag[j] >>> nBits2;
            }
            newMag[i] = mag[j] << nBits;
        }
        return newMag;
    }

    private static int getLowestSetBit(int[] value) {
        if (value.length == 0) {
            return -1;
        }
        int j = value.length - 1, b;
        while ((j > 0) && (value[j] == 0)) {
            j--;
        }
        b = value[j];
        if (b == 0) {
            return -1;
        }
        return ((value.length - 1 - j) << 5) + Integer.numberOfTrailingZeros(b);
    }

    private static long divideOneWordLong(int[] value, int divisor) {
        long divisorLong = divisor & LONG_MASK;
        int intLen = value.length;

        // Special case of one word dividend
        if (value.length == 1) {
            return Integer.divideUnsigned(value[0], divisor);
        }

        int[] qValue = new int[intLen];
        long rem = 0;
        for (int xlen = intLen; xlen > 0; xlen--) {
            long dividendEstimate = (rem << 32) |
                    (value[intLen - xlen] & LONG_MASK);
            int q = (int) Long.divideUnsigned(dividendEstimate, divisorLong);
            rem = Long.remainderUnsigned(dividendEstimate, divisorLong);
            qValue[intLen - xlen] = q;
        }

        return longValue(qValue, intLen);
    }

    private static void primitiveLeftShift(int[] val, int n, int[] result, int resFrom) {
        int n2 = 32 - n;
        final int m = val.length - 1;
        int b = val[0];
        for (int i = 0; i < m; i++) {
            int c = val[i + 1];
            result[resFrom + i] = (b << n) | (c >>> n2);
            b = c;
        }
        result[resFrom + m] = b << n;
    }

    private static long longValue(int[] value, int intLen) {
        if (intLen == 0) {
            return 0;
        }

        int index = 0;
        while (index < intLen && value[index] == 0) {
            index++;
        }
        if (intLen == index) {
            return 0;
        }
        return ((value[intLen - 2] & LONG_MASK) << 32)
                + (value[intLen - 1] & LONG_MASK);
    }

    private static boolean unsignedLongCompare(long one, long two) {
        return (one + Long.MIN_VALUE) > (two + Long.MIN_VALUE);
    }

    private static int divadd(int[] a, int[] result, int offset) {
        long carry = 0;

        for (int j = a.length - 1; j >= 0; j--) {
            long sum = (a[j] & LONG_MASK) +
                    (result[j + offset] & LONG_MASK) + carry;
            result[j + offset] = (int) sum;
            carry = sum >>> 32;
        }
        return (int) carry;
    }
}
