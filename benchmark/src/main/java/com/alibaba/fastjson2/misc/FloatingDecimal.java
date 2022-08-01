package com.alibaba.fastjson2.misc;

public class FloatingDecimal {
    static final int EXP_SHIFT = DoubleConsts.SIGNIFICAND_WIDTH - 1;
    static final long FRACT_HOB = (1L << EXP_SHIFT); // assumed High-Order bit
    static final long EXP_ONE = ((long) DoubleConsts.EXP_BIAS) << EXP_SHIFT; // exponent of 1.0
    static final int MAX_SMALL_BIN_EXP = 62;
    static final int MIN_SMALL_BIN_EXP = -(63 / 3);
    static final int MAX_DECIMAL_DIGITS = 15;
    static final int MAX_DECIMAL_EXPONENT = 308;
    static final int MIN_DECIMAL_EXPONENT = -324;
    static final int BIG_DECIMAL_EXPONENT = 324; // i.e. abs(MIN_DECIMAL_EXPONENT)
    static final int MAX_NDIGITS = 1100;

    static final int SINGLE_EXP_SHIFT = FloatConsts.SIGNIFICAND_WIDTH - 1;
    static final int SINGLE_FRACT_HOB = 1 << SINGLE_EXP_SHIFT;
    static final int SINGLE_MAX_DECIMAL_DIGITS = 7;
    static final int SINGLE_MAX_DECIMAL_EXPONENT = 38;
    static final int SINGLE_MIN_DECIMAL_EXPONENT = -45;
    static final int SINGLE_MAX_NDIGITS = 200;

    static final int INT_DECIMAL_DIGITS = 9;

    public static double parseDouble(char[] in, int len) throws NumberFormatException {
        boolean isNegative = false;
        int decExp;
        char c;

        parseNumber:
        try {
            // throws NullPointerException if null
            if (len == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = 0;

            char[] digits = new char[len];
//            char[] digits = arrays[len];
//            Arrays.fill(digits, '\0');

            int nDigits = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;

            skipLeadingZerosLoop:
            while (i < len) {
                c = in[i];
                if (c == '0') {
                    nLeadZero++;
                } else if (c == '.') {
                    // decPt = i;
                } else {
                    break skipLeadingZerosLoop;
                }
                i++;
            }
            digitLoop:
            while (i < len) {
                c = in[i];
                if (c >= '1' && c <= '9') {
                    digits[nDigits++] = c;
                    nTrailZero = 0;
                } else if (c == '0') {
                    digits[nDigits++] = c;
                    nTrailZero++;
                } else if (c == '.') {
                    // decPt = i;
                } else {
                    break digitLoop;
                }
                i++;
            }
            nDigits -= nTrailZero;
            //
            // At this point, we've scanned all the digits and decimal
            // point we're going to see. Trim off leading and trailing
            // zeros, which will just confuse us later, and adjust
            // our initial decimal exponent accordingly.
            // To review:
            // we have seen i total characters.
            // nLeadZero of them were zeros before any other digits.
            // nTrailZero of them were zeros after any other digits.
            // if ( decSeen ), then a . was seen after decPt characters
            // ( including leading zeros which have been discarded )
            // nDigits characters were neither lead nor trailing
            // zeros, nor point
            //
            //
            // special hack: if we saw no non-zero digits, then the
            // answer is zero!
            // Unfortunately, we feel honor-bound to keep parsing!
            //
            boolean isZero = (nDigits == 0);
            if (isZero && nLeadZero == 0) {
                // we saw NO DIGITS AT ALL,
                // not even a crummy 0!
                // this is not allowed.
                break parseNumber; // go throw exception
            }
            decExp = 1 - nLeadZero;

            //
            // We parsed everything we could.
            // If there are leftovers, then this is not good input!
            //
            if (i < len && (i != len - 1)) {
                break parseNumber; // go throw exception
            }
            if (isZero) {
                return 0;
            }
            return doubleValue(isNegative, decExp, digits, nDigits);
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new NumberFormatException("For input string: \"" + in + "\"");
    }

    static double doubleValue(boolean isNegative, int decExp, char[] digits, int nDigits) {
        int kDigits = Math.min(nDigits, MAX_DECIMAL_DIGITS + 1);
        //
        // convert the lead kDigits to a long integer.
        //
        // (special performance hack: start to do it using int)
        int iValue = (int) digits[0] - (int) '0';
        int iDigits = Math.min(kDigits, INT_DECIMAL_DIGITS);
        for (int i = 1; i < iDigits; i++) {
            iValue = iValue * 10 + (int) digits[i] - (int) '0';
        }
        long lValue = (long) iValue;
        for (int i = iDigits; i < kDigits; i++) {
            lValue = lValue * 10L + (long) ((int) digits[i] - (int) '0');
        }
        double dValue = (double) lValue;
        int exp = decExp - kDigits;

        if (nDigits <= MAX_DECIMAL_DIGITS) {
            if (exp == 0 || dValue == 0.0) {
                return (isNegative) ? -dValue : dValue; // small floating integer
            } else if (exp >= 0) {
                if (exp <= MAX_SMALL_TEN) {
                    double rValue = dValue * SMALL_10_POW[exp];
                    return (isNegative) ? -rValue : rValue;
                }
                int slop = MAX_DECIMAL_DIGITS - kDigits;
                if (exp <= MAX_SMALL_TEN + slop) {
                    dValue *= SMALL_10_POW[slop];
                    double rValue = dValue * SMALL_10_POW[exp - slop];
                    return (isNegative) ? -rValue : rValue;
                }
            } else {
                if (exp >= -MAX_SMALL_TEN) {
                    double rValue = dValue / SMALL_10_POW[-exp];
                    return (isNegative) ? -rValue : rValue;
                }
            }
        }

        if (exp > 0) {
            if (decExp > MAX_DECIMAL_EXPONENT + 1) {
                return (isNegative) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
            if ((exp & 15) != 0) {
                dValue *= SMALL_10_POW[exp & 15];
            }
            if ((exp >>= 4) != 0) {
                int j;
                for (j = 0; exp > 1; j++, exp >>= 1) {
                    if ((exp & 1) != 0) {
                        dValue *= BIG_10_POW[j];
                    }
                }

                double t = dValue * BIG_10_POW[j];
                if (Double.isInfinite(t)) {
                    t = dValue / 2.0;
                    t *= BIG_10_POW[j];
                    if (Double.isInfinite(t)) {
                        return (isNegative) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                    }
                    t = Double.MAX_VALUE;
                }
                dValue = t;
            }
        } else if (exp < 0) {
            exp = -exp;
            if (decExp < MIN_DECIMAL_EXPONENT - 1) {
                return (isNegative) ? -0.0 : 0.0;
            }
            if ((exp & 15) != 0) {
                dValue /= SMALL_10_POW[exp & 15];
            }
            if ((exp >>= 4) != 0) {
                int j;
                for (j = 0; exp > 1; j++, exp >>= 1) {
                    if ((exp & 1) != 0) {
                        dValue *= TINY_10_POW[j];
                    }
                }

                double t = dValue * TINY_10_POW[j];
                if (t == 0.0) {
                    t = dValue * 2.0;
                    t *= TINY_10_POW[j];
                    if (t == 0.0) {
                        return (isNegative) ? -0.0 : 0.0;
                    }
                    t = Double.MIN_VALUE;
                }
                dValue = t;
            }
        }

        if (nDigits > MAX_NDIGITS) {
            nDigits = MAX_NDIGITS + 1;
            digits[MAX_NDIGITS] = '1';
        }
        FDBigInteger bigD0 = new FDBigInteger(lValue, digits, kDigits, nDigits);
        exp = decExp - nDigits;

        long ieeeBits = Double.doubleToRawLongBits(dValue); // IEEE-754 bits of double candidate
        final int B5 = Math.max(0, -exp); // powers of 5 in bigB, value is not modified inside correctionLoop
        final int D5 = Math.max(0, exp); // powers of 5 in bigD, value is not modified inside correctionLoop
        bigD0 = bigD0.multByPow52(D5, 0);
        bigD0.makeImmutable();   // prevent bigD0 modification inside correctionLoop
        FDBigInteger bigD = null;
        int prevD2 = 0;

        correctionLoop:
        while (true) {
            // here ieeeBits can't be NaN, Infinity or zero
            int binexp = (int) (ieeeBits >>> EXP_SHIFT);
            long bigBbits = ieeeBits & DoubleConsts.SIGNIF_BIT_MASK;
            if (binexp > 0) {
                bigBbits |= FRACT_HOB;
            } else { // Normalize denormalized numbers.
                assert bigBbits != 0L : bigBbits; // doubleToBigInt(0.0)
                int leadingZeros = Long.numberOfLeadingZeros(bigBbits);
                int shift = leadingZeros - (63 - EXP_SHIFT);
                bigBbits <<= shift;
                binexp = 1 - shift;
            }
            binexp -= DoubleConsts.EXP_BIAS;
            int lowOrderZeros = Long.numberOfTrailingZeros(bigBbits);
            bigBbits >>>= lowOrderZeros;
            final int bigIntExp = binexp - EXP_SHIFT + lowOrderZeros;
            final int bigIntNBits = EXP_SHIFT + 1 - lowOrderZeros;

            int B2 = B5; // powers of 2 in bigB
            int D2 = D5; // powers of 2 in bigD
            int Ulp2;   // powers of 2 in halfUlp.
            if (bigIntExp >= 0) {
                B2 += bigIntExp;
            } else {
                D2 -= bigIntExp;
            }
            Ulp2 = B2;
            // shift bigB and bigD left by a number s. t.
            // halfUlp is still an integer.
            int hulpbias;
            if (binexp <= -DoubleConsts.EXP_BIAS) {
                // This is going to be a denormalized number
                // (if not actually zero).
                // half an ULP is at 2^-(DoubleConsts.EXP_BIAS+EXP_SHIFT+1)
                hulpbias = binexp + lowOrderZeros + DoubleConsts.EXP_BIAS;
            } else {
                hulpbias = 1 + lowOrderZeros;
            }
            B2 += hulpbias;
            D2 += hulpbias;
            // if there are common factors of 2, we might just as well
            // factor them out, as they add nothing useful.
            int common2 = Math.min(B2, Math.min(D2, Ulp2));
            B2 -= common2;
            D2 -= common2;
            Ulp2 -= common2;
            // do multiplications by powers of 5 and 2
            FDBigInteger bigB = FDBigInteger.valueOfMulPow52(bigBbits, B5, B2);
            if (bigD == null || prevD2 != D2) {
                bigD = bigD0.leftShift(D2);
                prevD2 = D2;
            }

            FDBigInteger diff;
            int cmpResult;
            boolean overvalue;
            if ((cmpResult = bigB.cmp(bigD)) > 0) {
                overvalue = true; // our candidate is too big.
                diff = bigB.leftInplaceSub(bigD); // bigB is not user further - reuse
                if ((bigIntNBits == 1) && (bigIntExp > -DoubleConsts.EXP_BIAS + 1)) {
                    // candidate is a normalized exact power of 2 and
                    // is too big (larger than Double.MIN_NORMAL). We will be subtracting.
                    // For our purposes, ulp is the ulp of the
                    // next smaller range.
                    Ulp2 -= 1;
                    if (Ulp2 < 0) {
                        // rats. Cannot de-scale ulp this far.
                        // must scale diff in other direction.
                        Ulp2 = 0;
                        diff = diff.leftShift(1);
                    }
                }
            } else if (cmpResult < 0) {
                overvalue = false; // our candidate is too small.
                diff = bigD.rightInplaceSub(bigB); // bigB is not user further - reuse
            } else {
                // the candidate is exactly right!
                // this happens with surprising frequency
                break correctionLoop;
            }
            cmpResult = diff.cmpPow52(B5, Ulp2);
            if ((cmpResult) < 0) {
                // difference is small.
                // this is close enough
                break correctionLoop;
            } else if (cmpResult == 0) {
                // difference is exactly half an ULP
                // round to some other value maybe, then finish
                if ((ieeeBits & 1) != 0) { // half ties to even
                    ieeeBits += overvalue ? -1 : 1; // nextDown or nextUp
                }
                break correctionLoop;
            } else {
                // difference is non-trivial.
                // could scale addend by ratio of difference to
                // halfUlp here, if we bothered to compute that difference.
                // Most of the time ( I hope ) it is about 1 anyway.
                ieeeBits += overvalue ? -1 : 1; // nextDown or nextUp
                if (ieeeBits == 0 || ieeeBits == DoubleConsts.EXP_BIT_MASK) { // 0.0 or Double.POSITIVE_INFINITY
                    break correctionLoop; // oops. Fell off end of range.
                }
                continue; // try again.
            }
        }
        if (isNegative) {
            ieeeBits |= DoubleConsts.SIGN_BIT_MASK;
        }
        return Double.longBitsToDouble(ieeeBits);
    }

    public static float parseFloat(char[] in, int len) throws NumberFormatException {
        boolean isNegative = false;
        int decExp;
        char c;

        parseNumber:
        try {
            // throws NullPointerException if null
            if (len == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = 0;

            char[] digits = new char[len];
//            char[] digits = arrays[len];
//            Arrays.fill(digits, '\0');

            int nDigits = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;

            skipLeadingZerosLoop:
            while (i < len) {
                c = in[i];
                if (c == '0') {
                    nLeadZero++;
                } else if (c == '.') {
                    // decPt = i;
                } else {
                    break skipLeadingZerosLoop;
                }
                i++;
            }
            digitLoop:
            while (i < len) {
                c = in[i];
                if (c >= '1' && c <= '9') {
                    digits[nDigits++] = c;
                    nTrailZero = 0;
                } else if (c == '0') {
                    digits[nDigits++] = c;
                    nTrailZero++;
                } else if (c == '.') {
                    // decPt = i;
                } else {
                    break digitLoop;
                }
                i++;
            }
            nDigits -= nTrailZero;
            //
            // At this point, we've scanned all the digits and decimal
            // point we're going to see. Trim off leading and trailing
            // zeros, which will just confuse us later, and adjust
            // our initial decimal exponent accordingly.
            // To review:
            // we have seen i total characters.
            // nLeadZero of them were zeros before any other digits.
            // nTrailZero of them were zeros after any other digits.
            // if ( decSeen ), then a . was seen after decPt characters
            // ( including leading zeros which have been discarded )
            // nDigits characters were neither lead nor trailing
            // zeros, nor point
            //
            //
            // special hack: if we saw no non-zero digits, then the
            // answer is zero!
            // Unfortunately, we feel honor-bound to keep parsing!
            //
            boolean isZero = (nDigits == 0);
            if (isZero && nLeadZero == 0) {
                // we saw NO DIGITS AT ALL,
                // not even a crummy 0!
                // this is not allowed.
                break parseNumber; // go throw exception
            }
            decExp = 1 - nLeadZero;

            //
            // We parsed everything we could.
            // If there are leftovers, then this is not good input!
            //
            if (i < len && (i != len - 1)) {
                break parseNumber; // go throw exception
            }
            if (isZero) {
                return 0;
            }
            return floatValue(isNegative, decExp, digits, nDigits);
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new NumberFormatException("For input string: \"" + in + "\"");
    }

    static float floatValue(boolean isNegative, int decExponent, char[] digits, int nDigits) {
        int kDigits = Math.min(nDigits, SINGLE_MAX_DECIMAL_DIGITS + 1);
        //
        // convert the lead kDigits to an integer.
        //
        int iValue = (int) digits[0] - (int) '0';
        for (int i = 1; i < kDigits; i++) {
            iValue = iValue * 10 + (int) digits[i] - (int) '0';
        }
        float fValue = (float) iValue;
        int exp = decExponent - kDigits;
        //
        // iValue now contains an integer with the value of
        // the first kDigits digits of the number.
        // fValue contains the (float) of the same.
        //

        if (nDigits <= SINGLE_MAX_DECIMAL_DIGITS) {
            //
            // possibly an easy case.
            // We know that the digits can be represented
            // exactly. And if the exponent isn't too outrageous,
            // the whole thing can be done with one operation,
            // thus one rounding error.
            // Note that all our constructors trim all leading and
            // trailing zeros, so simple values (including zero)
            // will always end up here.
            //
            if (exp == 0 || fValue == 0.0f) {
                return (isNegative) ? -fValue : fValue; // small floating integer
            } else if (exp >= 0) {
                if (exp <= SINGLE_MAX_SMALL_TEN) {
                    //
                    // Can get the answer with one operation,
                    // thus one roundoff.
                    //
                    fValue *= SINGLE_SMALL_10_POW[exp];
                    return (isNegative) ? -fValue : fValue;
                }
                int slop = SINGLE_MAX_DECIMAL_DIGITS - kDigits;
                if (exp <= SINGLE_MAX_SMALL_TEN + slop) {
                    //
                    // We can multiply fValue by 10^(slop)
                    // and it is still "small" and exact.
                    // Then we can multiply by 10^(exp-slop)
                    // with one rounding.
                    //
                    fValue *= SINGLE_SMALL_10_POW[slop];
                    fValue *= SINGLE_SMALL_10_POW[exp - slop];
                    return (isNegative) ? -fValue : fValue;
                }
                //
                // Else we have a hard case with a positive exp.
                //
            } else {
                if (exp >= -SINGLE_MAX_SMALL_TEN) {
                    //
                    // Can get the answer in one division.
                    //
                    fValue /= SINGLE_SMALL_10_POW[-exp];
                    return (isNegative) ? -fValue : fValue;
                }
                //
                // Else we have a hard case with a negative exp.
                //
            }
        } else if ((decExponent >= nDigits) && (nDigits + decExponent <= MAX_DECIMAL_DIGITS)) {
            //
            // In double-precision, this is an exact floating integer.
            // So we can compute to double, then shorten to float
            // with one round, and get the right answer.
            //
            // First, finish accumulating digits.
            // Then convert that integer to a double, multiply
            // by the appropriate power of ten, and convert to float.
            //
            long lValue = (long) iValue;
            for (int i = kDigits; i < nDigits; i++) {
                lValue = lValue * 10L + (long) ((int) digits[i] - (int) '0');
            }
            double dValue = (double) lValue;
            exp = decExponent - nDigits;
            dValue *= SMALL_10_POW[exp];
            fValue = (float) dValue;
            return (isNegative) ? -fValue : fValue;
        }
        //
        // Harder cases:
        // The sum of digits plus exponent is greater than
        // what we think we can do with one error.
        //
        // Start by approximating the right answer by,
        // naively, scaling by powers of 10.
        // Scaling uses doubles to avoid overflow/underflow.
        //
        double dValue = fValue;
        if (exp > 0) {
            if (decExponent > SINGLE_MAX_DECIMAL_EXPONENT + 1) {
                //
                // Lets face it. This is going to be
                // Infinity. Cut to the chase.
                //
                return (isNegative) ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            }
            if ((exp & 15) != 0) {
                dValue *= SMALL_10_POW[exp & 15];
            }
            if ((exp >>= 4) != 0) {
                int j;
                for (j = 0; exp > 0; j++, exp >>= 1) {
                    if ((exp & 1) != 0) {
                        dValue *= BIG_10_POW[j];
                    }
                }
            }
        } else if (exp < 0) {
            exp = -exp;
            if (decExponent < SINGLE_MIN_DECIMAL_EXPONENT - 1) {
                //
                // Lets face it. This is going to be
                // zero. Cut to the chase.
                //
                return (isNegative) ? -0.0f : 0.0f;
            }
            if ((exp & 15) != 0) {
                dValue /= SMALL_10_POW[exp & 15];
            }
            if ((exp >>= 4) != 0) {
                int j;
                for (j = 0; exp > 0; j++, exp >>= 1) {
                    if ((exp & 1) != 0) {
                        dValue *= TINY_10_POW[j];
                    }
                }
            }
        }
        fValue = Math.max(Float.MIN_VALUE, Math.min(Float.MAX_VALUE, (float) dValue));

        //
        // fValue is now approximately the result.
        // The hard part is adjusting it, by comparison
        // with FDBigInteger arithmetic.
        // Formulate the EXACT big-number result as
        // bigD0 * 10^exp
        //
        if (nDigits > SINGLE_MAX_NDIGITS) {
            nDigits = SINGLE_MAX_NDIGITS + 1;
            digits[SINGLE_MAX_NDIGITS] = '1';
        }
        FDBigInteger bigD0 = new FDBigInteger(iValue, digits, kDigits, nDigits);
        exp = decExponent - nDigits;

        int ieeeBits = Float.floatToRawIntBits(fValue); // IEEE-754 bits of float candidate
        final int B5 = Math.max(0, -exp); // powers of 5 in bigB, value is not modified inside correctionLoop
        final int D5 = Math.max(0, exp); // powers of 5 in bigD, value is not modified inside correctionLoop
        bigD0 = bigD0.multByPow52(D5, 0);
        bigD0.makeImmutable();   // prevent bigD0 modification inside correctionLoop
        FDBigInteger bigD = null;
        int prevD2 = 0;

        correctionLoop:
        while (true) {
            // here ieeeBits can't be NaN, Infinity or zero
            int binexp = ieeeBits >>> SINGLE_EXP_SHIFT;
            int bigBbits = ieeeBits & FloatConsts.SIGNIF_BIT_MASK;
            if (binexp > 0) {
                bigBbits |= SINGLE_FRACT_HOB;
            } else { // Normalize denormalized numbers.
                assert bigBbits != 0 : bigBbits; // floatToBigInt(0.0)
                int leadingZeros = Integer.numberOfLeadingZeros(bigBbits);
                int shift = leadingZeros - (31 - SINGLE_EXP_SHIFT);
                bigBbits <<= shift;
                binexp = 1 - shift;
            }
            binexp -= FloatConsts.EXP_BIAS;
            int lowOrderZeros = Integer.numberOfTrailingZeros(bigBbits);
            bigBbits >>>= lowOrderZeros;
            final int bigIntExp = binexp - SINGLE_EXP_SHIFT + lowOrderZeros;
            final int bigIntNBits = SINGLE_EXP_SHIFT + 1 - lowOrderZeros;

            //
            // Scale bigD, bigB appropriately for
            // big-integer operations.
            // Naively, we multiply by powers of ten
            // and powers of two. What we actually do
            // is keep track of the powers of 5 and
            // powers of 2 we would use, then factor out
            // common divisors before doing the work.
            //
            int B2 = B5; // powers of 2 in bigB
            int D2 = D5; // powers of 2 in bigD
            int Ulp2;   // powers of 2 in halfUlp.
            if (bigIntExp >= 0) {
                B2 += bigIntExp;
            } else {
                D2 -= bigIntExp;
            }
            Ulp2 = B2;
            // shift bigB and bigD left by a number s. t.
            // halfUlp is still an integer.
            int hulpbias;
            if (binexp <= -FloatConsts.EXP_BIAS) {
                // This is going to be a denormalized number
                // (if not actually zero).
                // half an ULP is at 2^-(FloatConsts.EXP_BIAS+SINGLE_EXP_SHIFT+1)
                hulpbias = binexp + lowOrderZeros + FloatConsts.EXP_BIAS;
            } else {
                hulpbias = 1 + lowOrderZeros;
            }
            B2 += hulpbias;
            D2 += hulpbias;
            // if there are common factors of 2, we might just as well
            // factor them out, as they add nothing useful.
            int common2 = Math.min(B2, Math.min(D2, Ulp2));
            B2 -= common2;
            D2 -= common2;
            Ulp2 -= common2;
            // do multiplications by powers of 5 and 2
            FDBigInteger bigB = FDBigInteger.valueOfMulPow52(bigBbits, B5, B2);
            if (bigD == null || prevD2 != D2) {
                bigD = bigD0.leftShift(D2);
                prevD2 = D2;
            }
            //
            // to recap:
            // bigB is the scaled-big-int version of our floating-point
            // candidate.
            // bigD is the scaled-big-int version of the exact value
            // as we understand it.
            // halfUlp is 1/2 an ulp of bigB, except for special cases
            // of exact powers of 2
            //
            // the plan is to compare bigB with bigD, and if the difference
            // is less than halfUlp, then we're satisfied. Otherwise,
            // use the ratio of difference to halfUlp to calculate a fudge
            // factor to add to the floating value, then go 'round again.
            //
            FDBigInteger diff;
            int cmpResult;
            boolean overvalue;
            if ((cmpResult = bigB.cmp(bigD)) > 0) {
                overvalue = true; // our candidate is too big.
                diff = bigB.leftInplaceSub(bigD); // bigB is not user further - reuse
                if ((bigIntNBits == 1) && (bigIntExp > -FloatConsts.EXP_BIAS + 1)) {
                    // candidate is a normalized exact power of 2 and
                    // is too big (larger than Float.MIN_NORMAL). We will be subtracting.
                    // For our purposes, ulp is the ulp of the
                    // next smaller range.
                    Ulp2 -= 1;
                    if (Ulp2 < 0) {
                        // rats. Cannot de-scale ulp this far.
                        // must scale diff in other direction.
                        Ulp2 = 0;
                        diff = diff.leftShift(1);
                    }
                }
            } else if (cmpResult < 0) {
                overvalue = false; // our candidate is too small.
                diff = bigD.rightInplaceSub(bigB); // bigB is not user further - reuse
            } else {
                // the candidate is exactly right!
                // this happens with surprising frequency
                break correctionLoop;
            }
            cmpResult = diff.cmpPow52(B5, Ulp2);
            if ((cmpResult) < 0) {
                // difference is small.
                // this is close enough
                break correctionLoop;
            } else if (cmpResult == 0) {
                // difference is exactly half an ULP
                // round to some other value maybe, then finish
                if ((ieeeBits & 1) != 0) { // half ties to even
                    ieeeBits += overvalue ? -1 : 1; // nextDown or nextUp
                }
                break correctionLoop;
            } else {
                // difference is non-trivial.
                // could scale addend by ratio of difference to
                // halfUlp here, if we bothered to compute that difference.
                // Most of the time ( I hope ) it is about 1 anyway.
                ieeeBits += overvalue ? -1 : 1; // nextDown or nextUp
                if (ieeeBits == 0 || ieeeBits == FloatConsts.EXP_BIT_MASK) { // 0.0 or Float.POSITIVE_INFINITY
                    break correctionLoop; // oops. Fell off end of range.
                }
                continue; // try again.
            }
        }
        if (isNegative) {
            ieeeBits |= FloatConsts.SIGN_BIT_MASK;
        }
        return Float.intBitsToFloat(ieeeBits);
    }

    /**
     * All the positive powers of 10 that can be
     * represented exactly in double/float.
     */
    private static final double[] SMALL_10_POW = {
            1.0e0,
            1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5,
            1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10,
            1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15,
            1.0e16, 1.0e17, 1.0e18, 1.0e19, 1.0e20,
            1.0e21, 1.0e22
    };

    private static final float[] SINGLE_SMALL_10_POW = {
            1.0e0f,
            1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
            1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };

    private static final double[] BIG_10_POW = {
            1e16, 1e32, 1e64, 1e128, 1e256};
    private static final double[] TINY_10_POW = {
            1e-16, 1e-32, 1e-64, 1e-128, 1e-256};

    private static final int MAX_SMALL_TEN = SMALL_10_POW.length - 1;
    private static final int SINGLE_MAX_SMALL_TEN = SINGLE_SMALL_10_POW.length - 1;

    static interface DoubleConsts {
        public static final double MIN_NORMAL = 2.2250738585072014E-308;

        /**
         * The number of logical bits in the significand of a
         * <code>double</code> number, including the implicit bit.
         */
        public static final int SIGNIFICAND_WIDTH = 53;

        /**
         * Maximum exponent a finite <code>double</code> number may have.
         * It is equal to the value returned by
         * <code>Math.ilogb(Double.MAX_VALUE)</code>.
         */
        public static final int MAX_EXPONENT = 1023;

        /**
         * Minimum exponent a normalized <code>double</code> number may
         * have.  It is equal to the value returned by
         * <code>Math.ilogb(Double.MIN_NORMAL)</code>.
         */
        public static final int MIN_EXPONENT = -1022;

        public static final int MIN_SUB_EXPONENT = MIN_EXPONENT -
                (SIGNIFICAND_WIDTH - 1);

        public static final int EXP_BIAS = 1023;

        /**
         * Bit mask to isolate the sign bit of a <code>double</code>.
         */
        public static final long SIGN_BIT_MASK = 0x8000000000000000L;

        /**
         * Bit mask to isolate the exponent field of a
         * <code>double</code>.
         */
        public static final long EXP_BIT_MASK = 0x7FF0000000000000L;

        /**
         * Bit mask to isolate the significand field of a
         * <code>double</code>.
         */
        public static final long SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL;
    }
}
