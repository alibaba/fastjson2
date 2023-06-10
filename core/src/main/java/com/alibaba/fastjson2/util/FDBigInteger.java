package com.alibaba.fastjson2.util;

import java.util.Arrays;

public final class FDBigInteger {
    private static final int[] SMALL_5_POW = {
            1,
            5,
            5 * 5,
            5 * 5 * 5,
            5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5,
            5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5
    };

    private static final int MAX_FIVE_POW = 340;

    private static final FDBigInteger[] POW_5_CACHE;

    static {
        POW_5_CACHE = new FDBigInteger[MAX_FIVE_POW];
        int i = 0;
        while (i < SMALL_5_POW.length) {
            FDBigInteger pow5 = new FDBigInteger(new int[]{SMALL_5_POW[i]}, 0);
            pow5.immutable = true;
            POW_5_CACHE[i] = pow5;
            i++;
        }
        FDBigInteger prev = POW_5_CACHE[i - 1];
        while (i < MAX_FIVE_POW) {
            POW_5_CACHE[i] = prev = prev.mult(5);
            prev.immutable = true;
            i++;
        }
    }

    private int[] data;  // value: data[0] is least significant
    private int offset;  // number of least significant zero padding ints
    private int nWords;  // data[nWords-1]!=0, all values above are zero
    boolean immutable;

    private FDBigInteger(int[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.nWords = data.length;
        trimLeadingZeros();
    }

    public FDBigInteger(long lValue, char[] digits, int kDigits, int nDigits) {
        int n = Math.max((nDigits + 8) / 9, 2);        // estimate size needed.
        data = new int[n];      // allocate enough space
        data[0] = (int) lValue;    // starting value
        data[1] = (int) (lValue >>> 32);
        offset = 0;
        nWords = 2;
        int i = kDigits;
        int limit = nDigits - 5;       // slurp digits 5 at a time.
        int v;
        while (i < limit) {
            int ilim = i + 5;
            v = (int) digits[i++] - (int) '0';
            while (i < ilim) {
                v = 10 * v + (int) digits[i++] - (int) '0';
            }
            multAddMe(100000, v); // ... where 100000 is 10^5.
        }
        int factor = 1;
        v = 0;
        while (i < nDigits) {
            v = 10 * v + (int) digits[i++] - (int) '0';
            factor *= 10;
        }
        if (factor != 1) {
            multAddMe(factor, v);
        }
        trimLeadingZeros();
    }

    private void multAddMe(int iv, int addend) {
        long v = iv & 0XFFFFFFFFL;
        // unroll 0th iteration, doing addition.
        long p = v * (data[0] & 0XFFFFFFFFL) + (addend & 0XFFFFFFFFL);
        data[0] = (int) p;
        p >>>= 32;
        for (int i = 1; i < nWords; i++) {
            p += v * (data[i] & 0XFFFFFFFFL);
            data[i] = (int) p;
            p >>>= 32;
        }
        if (p != 0L) {
            data[nWords++] = (int) p; // will fail noisily if illegal!
        }
    }

    private void trimLeadingZeros() {
        int i = nWords;
        if (i > 0 && (data[--i] == 0)) {
            //for (; i > 0 && data[i - 1] == 0; i--) ;
            while (i > 0 && data[i - 1] == 0) {
                i--;
            }
            this.nWords = i;
            if (i == 0) { // all words are zero
                this.offset = 0;
            }
        }
    }

    public FDBigInteger multByPow52(int p5, int p2) {
        if (this.nWords == 0) {
            return this;
        }
        FDBigInteger res = this;
        if (p5 != 0) {
            int[] r;
            int extraSize = (p2 != 0) ? 1 : 0;
            if (p5 < SMALL_5_POW.length) {
                r = new int[this.nWords + 1 + extraSize];
                mult(this.data, this.nWords, SMALL_5_POW[p5], r);
                res = new FDBigInteger(r, this.offset);
            } else {
                FDBigInteger pow5 = big5pow(p5);
                r = new int[this.nWords + pow5.nWords + pow5.offset + extraSize];
                mult(this.data, this.nWords, pow5.data, pow5.nWords, r);
                res = new FDBigInteger(r, this.offset + pow5.offset);
            }
        }
        return res.leftShift(p2);
    }

    private static FDBigInteger big5pow(int p) {
        if (p < MAX_FIVE_POW) {
            return POW_5_CACHE[p];
        }
        return big5powRec(p);
    }

    private FDBigInteger mult(int i) {
        if (this.nWords == 0) {
            return this;
        }
        int[] r = new int[nWords + 1];
        mult(data, nWords, i, r);
        return new FDBigInteger(r, offset);
    }

    private static void mult(int[] src, int srcLen, int value, int[] r) {
        long val = value & 0XFFFFFFFFL;
        long carry = 0;
        for (int i = 0; i < srcLen; i++) {
            long product = (src[i] & 0XFFFFFFFFL) * val + carry;
            r[i] = (int) product;
            carry = product >>> 32;
        }
        r[srcLen] = (int) carry;
    }

    private static void mult(int[] s1, int s1Len, int[] s2, int s2Len, int[] dst) {
        for (int i = 0; i < s1Len; i++) {
            long v = s1[i] & 0XFFFFFFFFL;
            long p = 0L;
            for (int j = 0; j < s2Len; j++) {
                p += (dst[i + j] & 0XFFFFFFFFL) + v * (s2[j] & 0XFFFFFFFFL);
                dst[i + j] = (int) p;
                p >>>= 32;
            }
            dst[i + s2Len] = (int) p;
        }
    }

    private static void leftShift(int[] src, int idx, int[] result, int bitcount, int anticount, int prev) {
        for (; idx > 0; idx--) {
            int v = (prev << bitcount);
            prev = src[idx - 1];
            v |= (prev >>> anticount);
            result[idx] = v;
        }
        int v = prev << bitcount;
        result[0] = v;
    }

    public FDBigInteger leftShift(int shift) {
        if (shift == 0 || nWords == 0) {
            return this;
        }
        int wordcount = shift >> 5;
        int bitcount = shift & 0x1f;
        if (this.immutable) {
            if (bitcount == 0) {
                return new FDBigInteger(Arrays.copyOf(data, nWords), offset + wordcount);
            } else {
                int anticount = 32 - bitcount;
                int idx = nWords - 1;
                int prev = data[idx];
                int hi = prev >>> anticount;
                int[] result;
                if (hi != 0) {
                    result = new int[nWords + 1];
                    result[nWords] = hi;
                } else {
                    result = new int[nWords];
                }
                leftShift(data, idx, result, bitcount, anticount, prev);
                return new FDBigInteger(result, offset + wordcount);
            }
        } else {
            if (bitcount != 0) {
                int anticount = 32 - bitcount;
                if ((data[0] << bitcount) == 0) {
                    int idx = 0;
                    int prev = data[idx];
                    for (; idx < nWords - 1; idx++) {
                        int v = (prev >>> anticount);
                        prev = data[idx + 1];
                        v |= (prev << bitcount);
                        data[idx] = v;
                    }
                    int v = prev >>> anticount;
                    data[idx] = v;
                    if (v == 0) {
                        nWords--;
                    }
                    offset++;
                } else {
                    int idx = nWords - 1;
                    int prev = data[idx];
                    int hi = prev >>> anticount;
                    int[] result = data;
                    int[] src = data;
                    if (hi != 0) {
                        if (nWords == data.length) {
                            data = result = new int[nWords + 1];
                        }
                        result[nWords++] = hi;
                    }
                    leftShift(src, idx, result, bitcount, anticount, prev);
                }
            }
            offset += wordcount;
            return this;
        }
    }

    private static FDBigInteger big5powRec(int p) {
        if (p < MAX_FIVE_POW) {
            return POW_5_CACHE[p];
        }
        // construct the value.
        // recursively.
        int q, r;
        // in order to compute 5^p,
        // compute its square root, 5^(p/2) and square.
        // or, let q = p / 2, r = p -q, then
        // 5^p = 5^(q+r) = 5^q * 5^r
        q = p >> 1;
        r = p - q;
        FDBigInteger bigq = big5powRec(q);
        if (r < SMALL_5_POW.length) {
            return bigq.mult(SMALL_5_POW[r]);
        } else {
            FDBigInteger other = big5powRec(r);
            if (bigq.nWords == 0) {
                return bigq;
            }
            if (bigq.nWords + bigq.offset == 1) {
                return other.mult(bigq.data[0]);
            }
            if (other.nWords == 0) {
                return other;
            }
            if (other.nWords + other.offset == 1) {
                return bigq.mult(other.data[0]);
            }
            int[] r1 = new int[bigq.nWords + other.nWords];
            mult(bigq.data, bigq.nWords, other.data, other.nWords, r1);
            return new FDBigInteger(r1, bigq.offset + other.offset);
        }
    }

    public static FDBigInteger valueOfMulPow52(long value, int p5, int p2) {
        assert p5 >= 0 : p5;
        assert p2 >= 0 : p2;
        int v0 = (int) value;
        int v1 = (int) (value >>> 32);
        int wordcount = p2 >> 5;
        int bitcount = p2 & 0x1f;
        if (p5 != 0) {
            if (p5 < SMALL_5_POW.length) {
                long pow5 = SMALL_5_POW[p5] & 0XFFFFFFFFL;
                long carry = (v0 & 0XFFFFFFFFL) * pow5;
                v0 = (int) carry;
                carry >>>= 32;
                carry = (v1 & 0XFFFFFFFFL) * pow5 + carry;
                v1 = (int) carry;
                int v2 = (int) (carry >>> 32);
                if (bitcount == 0) {
                    return new FDBigInteger(new int[]{v0, v1, v2}, wordcount);
                } else {
                    return new FDBigInteger(new int[]{
                            v0 << bitcount,
                            (v1 << bitcount) | (v0 >>> (32 - bitcount)),
                            (v2 << bitcount) | (v1 >>> (32 - bitcount)),
                            v2 >>> (32 - bitcount)
                    }, wordcount);
                }
            } else {
                FDBigInteger pow5 = big5pow(p5);
                int[] r;
                final int[] src = pow5.data;
                final int srcLen = pow5.nWords;
                long v = v0 & 0XFFFFFFFFL;
                long carry = 0;

                r = v1 == 0
                        ? new int[pow5.nWords + 1 + ((p2 != 0) ? 1 : 0)]
                        : new int[pow5.nWords + 2 + ((p2 != 0) ? 1 : 0)];
                for (int i = 0; i < srcLen; i++) {
                    long product = v * (src[i] & 0XFFFFFFFFL) + carry;
                    r[i] = (int) product;
                    carry = product >>> 32;
                }
                r[srcLen] = (int) carry;

                if (v1 != 0) {
                    v = v1 & 0XFFFFFFFFL;
                    carry = 0;
                    for (int j = 0; j < srcLen; j++) {
                        long product = (r[j + 1] & 0XFFFFFFFFL) + v * (src[j] & 0XFFFFFFFFL) + carry;
                        r[j + 1] = (int) product;
                        carry = product >>> 32;
                    }
                    r[srcLen + 1] = (int) carry;
                }

                return (new FDBigInteger(r, pow5.offset)).leftShift(p2);
            }
        } else if (p2 != 0) {
            if (bitcount == 0) {
                return new FDBigInteger(new int[]{v0, v1}, wordcount);
            } else {
                return new FDBigInteger(new int[]{
                        v0 << bitcount,
                        (v1 << bitcount) | (v0 >>> (32 - bitcount)),
                        v1 >>> (32 - bitcount)
                }, wordcount);
            }
        }
        return new FDBigInteger(new int[]{v0, v1}, 0);
    }

    public int cmp(FDBigInteger other) {
        int aSize = nWords + offset;
        int bSize = other.nWords + other.offset;
        if (aSize > bSize) {
            return 1;
        } else if (aSize < bSize) {
            return -1;
        }
        int aLen = nWords;
        int bLen = other.nWords;
        while (aLen > 0 && bLen > 0) {
            int a = data[--aLen];
            int b = other.data[--bLen];
            if (a != b) {
                return ((a & 0XFFFFFFFFL) < (b & 0XFFFFFFFFL)) ? -1 : 1;
            }
        }
        if (aLen > 0) {
            return checkZeroTail(data, aLen);
        }
        if (bLen > 0) {
            return -checkZeroTail(other.data, bLen);
        }
        return 0;
    }

    private static int checkZeroTail(int[] a, int from) {
        while (from > 0) {
            if (a[--from] != 0) {
                return 1;
            }
        }
        return 0;
    }

    public FDBigInteger leftInplaceSub(FDBigInteger subtrahend) {
        FDBigInteger minuend;
        if (this.immutable) {
            minuend = new FDBigInteger(this.data.clone(), this.offset);
        } else {
            minuend = this;
        }
        int offsetDiff = subtrahend.offset - minuend.offset;
        int[] sData = subtrahend.data;
        int[] mData = minuend.data;
        int subLen = subtrahend.nWords;
        int minLen = minuend.nWords;
        if (offsetDiff < 0) {
            // need to expand minuend
            int rLen = minLen - offsetDiff;
            if (rLen < mData.length) {
                System.arraycopy(mData, 0, mData, -offsetDiff, minLen);
                Arrays.fill(mData, 0, -offsetDiff, 0);
            } else {
                int[] r = new int[rLen];
                System.arraycopy(mData, 0, r, -offsetDiff, minLen);
                minuend.data = mData = r;
            }
            minuend.offset = subtrahend.offset;
            minuend.nWords = minLen = rLen;
            offsetDiff = 0;
        }
        long borrow = 0L;
        int mIndex = offsetDiff;
        for (int sIndex = 0; sIndex < subLen && mIndex < minLen; sIndex++, mIndex++) {
            long diff = (mData[mIndex] & 0XFFFFFFFFL) - (sData[sIndex] & 0XFFFFFFFFL) + borrow;
            mData[mIndex] = (int) diff;
            borrow = diff >> 32; // signed shift
        }
        for (; borrow != 0 && mIndex < minLen; mIndex++) {
            long diff = (mData[mIndex] & 0XFFFFFFFFL) + borrow;
            mData[mIndex] = (int) diff;
            borrow = diff >> 32; // signed shift
        }
        assert borrow == 0L : borrow; // borrow out of subtract,
        // result should be positive
        minuend.trimLeadingZeros();
        return minuend;
    }

    public FDBigInteger rightInplaceSub(FDBigInteger subtrahend) {
        FDBigInteger minuend = this;
        if (subtrahend.immutable) {
            subtrahend = new FDBigInteger(subtrahend.data.clone(), subtrahend.offset);
        }
        int offsetDiff = minuend.offset - subtrahend.offset;
        int[] sData = subtrahend.data;
        int[] mData = minuend.data;
        int subLen = subtrahend.nWords;
        int minLen = minuend.nWords;
        if (offsetDiff < 0) {
            if (minLen < sData.length) {
                System.arraycopy(sData, 0, sData, -offsetDiff, subLen);
                Arrays.fill(sData, 0, -offsetDiff, 0);
            } else {
                int[] r = new int[minLen];
                System.arraycopy(sData, 0, r, -offsetDiff, subLen);
                subtrahend.data = sData = r;
            }
            subtrahend.offset = minuend.offset;
            offsetDiff = 0;
        } else {
            int rLen = minLen + offsetDiff;
            if (rLen >= sData.length) {
                subtrahend.data = sData = Arrays.copyOf(sData, rLen);
            }
        }

        int sIndex = 0;
        long borrow = 0L;
        for (; sIndex < offsetDiff; sIndex++) {
            long diff = -(sData[sIndex] & 0XFFFFFFFFL) + borrow;
            sData[sIndex] = (int) diff;
            borrow = diff >> 32; // signed shift
        }

        for (int mIndex = 0; mIndex < minLen; sIndex++, mIndex++) {
            long diff = (mData[mIndex] & 0XFFFFFFFFL) - (sData[sIndex] & 0XFFFFFFFFL) + borrow;
            sData[sIndex] = (int) diff;
            borrow = diff >> 32; // signed shift
        }
        assert borrow == 0L : borrow; // borrow out of subtract,
        // result should be positive
        subtrahend.nWords = sIndex;
        subtrahend.trimLeadingZeros();
        return subtrahend;
    }

    public int cmpPow52(int p5, int p2) {
        if (p5 == 0) {
            int wordcount = p2 >> 5;
            int bitcount = p2 & 0x1f;
            int size = this.nWords + this.offset;
            if (size > wordcount + 1) {
                return 1;
            } else if (size < wordcount + 1) {
                return -1;
            }
            int a = this.data[this.nWords - 1];
            int b = 1 << bitcount;
            if (a != b) {
                return ((a & 0XFFFFFFFFL) < (b & 0XFFFFFFFFL)) ? -1 : 1;
            }
            return checkZeroTail(this.data, this.nWords - 1);
        }
        return this.cmp(big5pow(p5).leftShift(p2));
    }
}
