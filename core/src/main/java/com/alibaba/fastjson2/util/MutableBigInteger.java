package com.alibaba.fastjson2.util;

import java.util.Arrays;

public class MutableBigInteger {
    static final long LONG_MASK = 0xffffffffL;

    /**
     * The minimum {@code intLen} for cancelling powers of two before
     * dividing.
     * If the number of ints is less than this threshold,
     * {@code divideKnuth} does not eliminate common powers of two from
     * the dividend and divisor.
     */
    static final int KNUTH_POW2_THRESH_LEN = 6;

    /**
     * The minimum number of trailing zero ints for cancelling powers of two
     * before dividing.
     * If the dividend and divisor don't share at least this many zero ints
     * at the end, {@code divideKnuth} does not eliminate common powers
     * of two from the dividend and divisor.
     */
    static final int KNUTH_POW2_THRESH_ZEROS = 3;

    /**
     * Holds the magnitude of this MutableBigInteger in big endian order.
     * The magnitude may start at an offset into the value array, and it may
     * end before the length of the value array.
     */
    int[] value;

    /**
     * The number of ints of the value array that are currently used
     * to hold the magnitude of this MutableBigInteger. The magnitude starts
     * at an offset and offset + intLen may be less than value.length.
     */
    int intLen;

    /**
     * The offset into the value array where the magnitude of this
     * MutableBigInteger begins.
     */
    int offset;

    public MutableBigInteger() {
        value = new int[1];
        intLen = 0;
    }

    MutableBigInteger(int val) {
        init(val);
    }

    private void init(int val) {
        value = new int[]{val};
        intLen = val != 0 ? 1 : 0;
    }

    public MutableBigInteger(int[] val) {
        value = val;
        intLen = val.length;
    }

    MutableBigInteger(MutableBigInteger val) {
        intLen = val.intLen;
        value = Arrays.copyOfRange(val.value, val.offset, val.offset + intLen);
    }

    MutableBigInteger divideKnuth(MutableBigInteger b, MutableBigInteger quotient) {
        return divideKnuth(b, quotient, true);
    }

    MutableBigInteger divideKnuth(MutableBigInteger b, MutableBigInteger quotient, boolean needRemainder) {
        if (b.intLen == 0) {
            throw new ArithmeticException("BigInteger divide by zero");
        }

        // Dividend is zero
        if (intLen == 0) {
            quotient.intLen = quotient.offset = 0;
            return needRemainder ? new MutableBigInteger() : null;
        }

        int cmp = compare(b);
        // Dividend less than divisor
        if (cmp < 0) {
            quotient.intLen = quotient.offset = 0;
            return needRemainder ? new MutableBigInteger(this) : null;
        }
        // Dividend equal to divisor
        if (cmp == 0) {
            quotient.value[0] = quotient.intLen = 1;
            quotient.offset = 0;
            return needRemainder ? new MutableBigInteger() : null;
        }

        quotient.clear();
        // Special case one word divisor
        if (b.intLen == 1) {
            int r = divideOneWord(b.value[b.offset], quotient);
            if (needRemainder) {
                if (r == 0) {
                    return new MutableBigInteger();
                }
                return new MutableBigInteger(r);
            } else {
                return null;
            }
        }

        // Cancel common powers of two if we're above the KNUTH_POW2_* thresholds
        if (intLen >= KNUTH_POW2_THRESH_LEN) {
            int trailingZeroBits = Math.min(getLowestSetBit(), b.getLowestSetBit());
            if (trailingZeroBits >= KNUTH_POW2_THRESH_ZEROS * 32) {
                MutableBigInteger a = new MutableBigInteger(this);
                b = new MutableBigInteger(b);
                a.rightShift(trailingZeroBits);
                b.rightShift(trailingZeroBits);
                MutableBigInteger r = a.divideKnuth(b, quotient);
                r.leftShift(trailingZeroBits);
                return r;
            }
        }

        return divideMagnitude(b, quotient, needRemainder);
    }

    /**
     * Compare the magnitude of two MutableBigIntegers. Returns -1, 0 or 1
     * as this MutableBigInteger is numerically less than, equal to, or
     * greater than {@code b}.
     * Assumes no leading unnecessary zeros.
     */
    final int compare(MutableBigInteger b) {
        int blen = b.intLen;
        if (intLen < blen) {
            return -1;
        }
        if (intLen > blen) {
            return 1;
        }

        // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
        // comparison.
        int[] bval = b.value;
        for (int i = offset, j = b.offset; i < intLen + offset; i++, j++) {
            int b1 = value[i] + 0x80000000;
            int b2 = bval[j] + 0x80000000;
            if (b1 < b2) {
                return -1;
            }
            if (b1 > b2) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Clear out a MutableBigInteger for reuse.
     */
    void clear() {
        offset = intLen = 0;
        for (int index = 0, n = value.length; index < n; index++) {
            value[index] = 0;
        }
    }

    /**
     * This method is used for division of an n word dividend by a one word
     * divisor. The quotient is placed into quotient. The one word divisor is
     * specified by divisor.
     *
     * @return the remainder of the division is returned.
     */
    int divideOneWord(int divisor, MutableBigInteger quotient) {
        long divisorLong = divisor & LONG_MASK;

        // Special case of one word dividend
        if (intLen == 1) {
            int dividendValue = value[offset];
            int q = Integer.divideUnsigned(dividendValue, divisor);
            int r = Integer.remainderUnsigned(dividendValue, divisor);
            quotient.value[0] = q;
            quotient.intLen = (q == 0) ? 0 : 1;
            quotient.offset = 0;
            return r;
        }

        if (quotient.value.length < intLen) {
            quotient.value = new int[intLen];
        }
        quotient.offset = 0;
        quotient.intLen = intLen;

        long rem = 0;
        for (int xlen = intLen; xlen > 0; xlen--) {
            long dividendEstimate = (rem << 32) |
                    (value[offset + intLen - xlen] & LONG_MASK);
            int q = (int) Long.divideUnsigned(dividendEstimate, divisorLong);
            rem = Long.remainderUnsigned(dividendEstimate, divisorLong);
            quotient.value[intLen - xlen] = q;
        }

        quotient.normalize();
        return (int) rem;
    }

    /**
     * Return the index of the lowest set bit in this MutableBigInteger. If the
     * magnitude of this MutableBigInteger is zero, -1 is returned.
     */
    private final int getLowestSetBit() {
        if (intLen == 0) {
            return -1;
        }
        int j = intLen - 1, b;
        while ((j > 0) && (value[j + offset] == 0)) {
            j--;
        }
        b = value[j + offset];
        if (b == 0) {
            return -1;
        }
        return ((intLen - 1 - j) << 5) + Integer.numberOfTrailingZeros(b);
    }

    final void normalize() {
        if (intLen == 0) {
            offset = 0;
            return;
        }

        int index = offset;
        if (value[index] != 0) {
            return;
        }

        int indexBound = index + intLen;
        do {
            index++;
        } while (index < indexBound && value[index] == 0);

        int numZeros = index - offset;
        intLen -= numZeros;
        offset = (intLen == 0 ? 0 : offset + numZeros);
    }

    void rightShift(int n) {
        if (intLen == 0) {
            return;
        }
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        this.intLen -= nInts;
        if (nBits == 0) {
            return;
        }
        int bitsInHighWord = bitLengthForInt(value[offset]);
        if (nBits >= bitsInHighWord) {
            this.primitiveLeftShift(32 - nBits);
            this.intLen--;
        } else {
            primitiveRightShift(nBits);
        }
    }

    private void primitiveRightShift(int n) {
        primitiveRightShift(n, value, offset);
    }

    private void primitiveRightShift(int n, int[] result, int resFrom) {
        int[] val = value;
        int n2 = 32 - n;

        int b = val[offset];
        result[resFrom] = b >>> n;
        for (int i = 1; i < intLen; i++) {
            int c = b;
            b = val[offset + i];
            result[resFrom + i] = (c << n2) | (b >>> n);
        }
    }

    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    private void primitiveLeftShift(int n) {
        primitiveLeftShift(n, value, offset);
    }

    private void primitiveLeftShift(int n, int[] result, int resFrom) {
        int[] val = value;
        int n2 = 32 - n;
        final int m = intLen - 1;
        int b = val[offset];
        for (int i = 0; i < m; i++) {
            int c = val[offset + i + 1];
            result[resFrom + i] = (b << n) | (c >>> n2);
            b = c;
        }
        result[resFrom + m] = b << n;
    }

    void leftShift(int n) {
        /*
         * If there is enough storage space in this MutableBigInteger already
         * the available space will be used. Space to the right of the used
         * ints in the value array is faster to utilize, so the extra space
         * will be taken from the right if possible.
         */
        if (intLen == 0) {
            return;
        }

        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        int leadingZeros = Integer.numberOfLeadingZeros(value[offset]);

        // If shift can be done without moving words, do so
        if (n <= leadingZeros) {
            primitiveLeftShift(nBits);
            return;
        }

        int newLen = intLen + nInts;
        if (nBits > leadingZeros) {
            newLen++;
        }

        int[] result;
        final int newOffset;
        if (value.length < newLen) { // The array must grow
            result = new int[newLen];
            newOffset = 0;
        } else {
            result = value;
            newOffset = value.length - offset >= newLen ? offset : 0;
        }

        int trailingZerosPos = newOffset + intLen;
        if (nBits != 0) {
            // Do primitive shift directly for speed
            if (nBits <= leadingZeros) {
                primitiveLeftShift(nBits, result, newOffset); // newOffset <= offset
            } else {
                int lastInt = value[offset + intLen - 1];
                primitiveRightShift(32 - nBits, result, newOffset); // newOffset <= offset
                result[trailingZerosPos++] = lastInt << nBits;
            }
        } else if (result != value || newOffset != offset) {
            System.arraycopy(value, offset, result, newOffset, intLen);
        }

        // Add trailing zeros
        if (result == value) {
            Arrays.fill(result, trailingZerosPos, newOffset + newLen, 0);
        }

        value = result;
        intLen = newLen;
        offset = newOffset;
    }

    private MutableBigInteger divideMagnitude(MutableBigInteger div,
                                              MutableBigInteger quotient,
                                              boolean needRemainder) {
        // assert div.intLen > 1
        // D1 normalize the divisor
        int shift = Integer.numberOfLeadingZeros(div.value[div.offset]);
        // Copy divisor value to protect divisor
        final int dlen = div.intLen;
        int[] divisor;
        MutableBigInteger rem; // Remainder starts as dividend with space for a leading zero
        if (shift > 0) {
            divisor = new int[dlen];
            div.primitiveLeftShift(shift, divisor, 0);
            if (Integer.numberOfLeadingZeros(value[offset]) >= shift) {
                int[] remarr = new int[intLen + 1];
                rem = new MutableBigInteger(remarr);
                rem.intLen = intLen;
                rem.offset = 1;
                this.primitiveLeftShift(shift, remarr, 1);
            } else {
                int[] remarr = new int[intLen + 2];
                rem = new MutableBigInteger(remarr);
                rem.intLen = intLen + 1;
                rem.offset = 1;
                int rFrom = offset;
                int c = 0;
                int n2 = 32 - shift;
                for (int i = 1; i < intLen + 1; i++, rFrom++) {
                    int b = c;
                    c = value[rFrom];
                    remarr[i] = (b << shift) | (c >>> n2);
                }
                remarr[intLen + 1] = c << shift;
            }
        } else {
            divisor = Arrays.copyOfRange(div.value, div.offset, div.offset + div.intLen);
            rem = new MutableBigInteger(new int[intLen + 1]);
            System.arraycopy(value, offset, rem.value, 1, intLen);
            rem.intLen = intLen;
            rem.offset = 1;
        }

        int nlen = rem.intLen;

        // Set the quotient size
        final int limit = nlen - dlen + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;

        // Insert leading 0 in rem
        rem.offset = 0;
        rem.value[0] = 0;
        rem.intLen++;

        int dh = divisor[0];
        long dhLong = dh & LONG_MASK;
        int dl = divisor[1];

        // D2 Initialize j
        for (int j = 0; j < limit - 1; j++) {
            // D3 Calculate qhat
            // estimate qhat
            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j + rem.offset];
            int nh2 = nh + 0x80000000;
            int nm = rem.value[j + 1 + rem.offset];

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
                long nl = rem.value[j + 2 + rem.offset] & LONG_MASK;
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
            rem.value[j + rem.offset] = 0;
            int borrow = mulsub(rem.value, divisor, qhat, dlen, j + rem.offset);

            // D5 Test remainder
            if (borrow + 0x80000000 > nh2) {
                // D6 Add back
                divadd(divisor, rem.value, j + 1 + rem.offset);
                qhat--;
            }

            // Store the quotient digit
            q[j] = qhat;
        } // D7 loop on j
        // D3 Calculate qhat
        // estimate qhat
        int qhat = 0;
        int qrem = 0;
        boolean skipCorrection = false;
        int nh = rem.value[limit - 1 + rem.offset];
        int nh2 = nh + 0x80000000;
        int nm = rem.value[limit + rem.offset];

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
                long nl = rem.value[limit + 1 + rem.offset] & LONG_MASK;
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
            rem.value[limit - 1 + rem.offset] = 0;
            if (needRemainder) {
                borrow = mulsub(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);
            } else {
                borrow = mulsubBorrow(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);
            }

            // D5 Test remainder
            if (borrow + 0x80000000 > nh2) {
                // D6 Add back
                if (needRemainder) {
                    divadd(divisor, rem.value, limit - 1 + 1 + rem.offset);
                }
                qhat--;
            }

            // Store the quotient digit
            q[(limit - 1)] = qhat;
        }

        if (needRemainder) {
            // D8 Unnormalize
            if (shift > 0) {
                rem.rightShift(shift);
            }
            rem.normalize();
        }
        quotient.normalize();
        return needRemainder ? rem : null;
    }

    private boolean unsignedLongCompare(long one, long two) {
        return (one + Long.MIN_VALUE) > (two + Long.MIN_VALUE);
    }

    private int mulsub(int[] q, int[] a, int x, int len, int offset) {
        long xLong = x & LONG_MASK;
        long carry = 0;
        offset += len;

        for (int j = len - 1; j >= 0; j--) {
            long product = (a[j] & LONG_MASK) * xLong + carry;
            long difference = q[offset] - product;
            q[offset--] = (int) difference;
            carry = (product >>> 32)
                    + (((difference & LONG_MASK) >
                    (((~(int) product) & LONG_MASK))) ? 1 : 0);
        }
        return (int) carry;
    }

    private int mulsubBorrow(int[] q, int[] a, int x, int len, int offset) {
        long xLong = x & LONG_MASK;
        long carry = 0;
        offset += len;
        for (int j = len - 1; j >= 0; j--) {
            long product = (a[j] & LONG_MASK) * xLong + carry;
            long difference = q[offset--] - product;
            carry = (product >>> 32)
                    + (((difference & LONG_MASK) >
                    (((~(int) product) & LONG_MASK))) ? 1 : 0);
        }
        return (int) carry;
    }

    private int divadd(int[] a, int[] result, int offset) {
        long carry = 0;

        for (int j = a.length - 1; j >= 0; j--) {
            long sum = (a[j] & LONG_MASK) +
                    (result[j + offset] & LONG_MASK) + carry;
            result[j + offset] = (int) sum;
            carry = sum >>> 32;
        }
        return (int) carry;
    }

    public long longValue(int sign) {
        if (intLen == 0 || sign == 0) {
            return 0;
        }

        int[] mag = getMagnitudeArray();
        long result = 0;
        for (int i = 1; i >= 0; i--) {
            result = (result << 32) + (getInt(mag, sign, i) & LONG_MASK);
        }
        return result;
    }

    public int intValue() {
        return value[value.length - 1];
    }

    private int[] getMagnitudeArray() {
        if (offset > 0 || value.length != intLen) {
            // Shrink value to be the total magnitude
            int[] tmp = Arrays.copyOfRange(value, offset, offset + intLen);
            Arrays.fill(value, 0);
            offset = 0;
            intLen = tmp.length;
            value = tmp;
        }
        return value;
    }

    private static int getInt(int[] mag, int signum, int n) {
        if (n < 0) {
            return 0;
        }
        if (n >= mag.length) {
            return signum < 0 ? -1 : 0;
        }

        int magInt = mag[mag.length - n - 1];

        return (signum >= 0 ? magInt :
                (n <= firstNonzeroIntNum(mag) ? -magInt : ~magInt));
    }

    private static int firstNonzeroIntNum(int[] mag) {
        int mlen = mag.length;
        int i = mag.length - 1;
        while (i >= 0 && mag[i] == 0) {
            i--;
        }
        return mlen - i + 1; // offset by two to initialize
    }
}
