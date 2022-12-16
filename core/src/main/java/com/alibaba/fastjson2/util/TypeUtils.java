package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.*;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

public class TypeUtils {
    public static final Class CLASS_JSON_OBJECT_1x;
    public static final Field FIELD_JSON_OBJECT_1x_map;
    public static final Class CLASS_JSON_ARRAY_1x;

    public static final Class CLASS_SINGLE_SET = Collections.singleton(1).getClass();
    public static final Class CLASS_SINGLE_List = Collections.singletonList(1).getClass();
    public static final Class CLASS_UNMODIFIABLE_COLLECTION = Collections.unmodifiableCollection(new ArrayList<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_LIST = Collections.unmodifiableList(new ArrayList<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_SET = Collections.unmodifiableSet(new HashSet<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_NAVIGABLE_SET = Collections.unmodifiableNavigableSet(new TreeSet<>()).getClass();
    public static final ParameterizedType PARAM_TYPE_LIST_STR = new ParameterizedTypeImpl(List.class, String.class);

    public static Type intern(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type rawType = paramType.getRawType();
            Type[] actualTypeArguments = paramType.getActualTypeArguments();
            if (rawType == List.class) {
                if (actualTypeArguments.length == 1) {
                    if (actualTypeArguments[0] == String.class) {
                        return PARAM_TYPE_LIST_STR;
                    }
                }
            }
        }
        return type;
    }

    public static double parseDouble(byte[] in, int off, int len) throws NumberFormatException {
        boolean isNegative = false;
        boolean signSeen = false;
        int decExp;
        byte c;
        int end = off + len;

        parseNumber:
        try {
            // throws NullPointerException if null
            if (len == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = off;
            switch (in[i]) {
                case '-':
                    isNegative = true;
                case '+':
                    i++;
                    signSeen = true;
            }

            char[] digits = new char[len];

            int nDigits = 0;
            boolean decSeen = false;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;

            skipLeadingZerosLoop:
            while (i < end) {
                c = in[i];
                if (c == '0') {
                    nLeadZero++;
                } else if (c == '.') {
                    if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i - off;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                } else {
                    break skipLeadingZerosLoop;
                }
                i++;
            }
            digitLoop:
            while (i < end) {
                c = in[i];
                if (c >= '1' && c <= '9') {
                    digits[nDigits++] = (char) c;
                    nTrailZero = 0;
                } else if (c == '0') {
                    digits[nDigits++] = (char) c;
                    nTrailZero++;
                } else if (c == '.') {
                    if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i - off;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                } else {
                    break digitLoop;
                }
                i++;
            }
            nDigits -= nTrailZero;

            boolean isZero = (nDigits == 0);
            if (isZero && nLeadZero == 0) {
                break parseNumber; // go throw exception
            }
            if (decSeen) {
                decExp = decPt - nLeadZero;
            } else {
                decExp = nDigits + nTrailZero;
            }

            if ((i < end) && (((c = in[i]) == 'e') || (c == 'E'))) {
                int expSign = 1;
                int expVal = 0;
                int reallyBig = Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch (in[++i]) {
                    case '-':
                        expSign = -1;
                    case '+':
                        i++;
                }
                int expAt = i;
                expLoop:
                while (i < end) {
                    if (expVal >= reallyBig) {
                        expOverflow = true;
                    }
                    c = in[i++];
                    if (c >= '0' && c <= '9') {
                        expVal = expVal * 10 + ((int) c - (int) '0');
                    } else {
                        i--;           // back up.
                        break expLoop; // stop parsing exponent.
                    }
                }
                int expLimit = FloatingDecimal.BIG_DECIMAL_EXPONENT + nDigits + nTrailZero;
                if (expOverflow || (expVal > expLimit)) {
                    decExp = expSign * expLimit;
                } else {
                    decExp = decExp + expSign * expVal;
                }

                if (i == expAt) {
                    break parseNumber; // certainly bad
                }
            }

            if (i < end && (i != end - 1)) {
                break parseNumber; // go throw exception
            }
            if (isZero) {
                return 0;
            }
            return doubleValue(isNegative, decExp, digits, nDigits);
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new NumberFormatException("For input string: \"" + new String(in, off, len) + "\"");
    }

    public static double parseDouble(char[] in, int off, int len) throws NumberFormatException {
        boolean isNegative = false;
        boolean signSeen = false;
        int decExp;
        char c;
        int end = off + len;

        parseNumber:
        try {
            // throws NullPointerException if null
            if (len == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = off;
            switch (in[i]) {
                case '-':
                    isNegative = true;
                case '+':
                    i++;
                    signSeen = true;
            }

            char[] digits = new char[len];

            int nDigits = 0;
            boolean decSeen = false;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;

            skipLeadingZerosLoop:
            while (i < end) {
                c = in[i];
                if (c == '0') {
                    nLeadZero++;
                } else if (c == '.') {
                    if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i - off;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                } else {
                    break skipLeadingZerosLoop;
                }
                i++;
            }
            digitLoop:
            while (i < end) {
                c = in[i];
                if (c >= '1' && c <= '9') {
                    digits[nDigits++] = c;
                    nTrailZero = 0;
                } else if (c == '0') {
                    digits[nDigits++] = c;
                    nTrailZero++;
                } else if (c == '.') {
                    if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i - off;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                } else {
                    break digitLoop;
                }
                i++;
            }
            nDigits -= nTrailZero;

            boolean isZero = (nDigits == 0);
            if (isZero && nLeadZero == 0) {
                break parseNumber; // go throw exception
            }
            if (decSeen) {
                decExp = decPt - nLeadZero;
            } else {
                decExp = nDigits + nTrailZero;
            }

            if ((i < end) && (((c = in[i]) == 'e') || (c == 'E'))) {
                int expSign = 1;
                int expVal = 0;
                int reallyBig = Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch (in[++i]) {
                    case '-':
                        expSign = -1;
                    case '+':
                        i++;
                }
                int expAt = i;
                expLoop:
                while (i < end) {
                    if (expVal >= reallyBig) {
                        expOverflow = true;
                    }
                    c = in[i++];
                    if (c >= '0' && c <= '9') {
                        expVal = expVal * 10 + ((int) c - (int) '0');
                    } else {
                        i--;           // back up.
                        break expLoop; // stop parsing exponent.
                    }
                }
                int expLimit = FloatingDecimal.BIG_DECIMAL_EXPONENT + nDigits + nTrailZero;
                if (expOverflow || (expVal > expLimit)) {
                    decExp = expSign * expLimit;
                } else {
                    decExp = decExp + expSign * expVal;
                }

                if (i == expAt) {
                    break parseNumber; // certainly bad
                }
            }

            if (i < end && (i != end - 1)) {
                break parseNumber; // go throw exception
            }
            if (isZero) {
                return 0;
            }
            return doubleValue(isNegative, decExp, digits, nDigits);
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new NumberFormatException("For input string: \"" + new String(in, off, len) + "\"");
    }

    public static float parseFloat(byte[] in, int off, int len) throws NumberFormatException {
        boolean isNegative = false;
        boolean signSeen = false;
        int decExp;
        byte c;
        int end = off + len;

        parseNumber:
        try {
            // throws NullPointerException if null
            if (len == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = off;
            switch (in[i]) {
                case '-':
                    isNegative = true;
                case '+':
                    i++;
                    signSeen = true;
            }

            char[] digits = new char[len];

            int nDigits = 0;
            boolean decSeen = false;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;

            skipLeadingZerosLoop:
            while (i < end) {
                c = in[i];
                if (c == '0') {
                    nLeadZero++;
                } else if (c == '.') {
                    if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i - off;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                } else {
                    break skipLeadingZerosLoop;
                }
                i++;
            }
            digitLoop:
            while (i < end) {
                c = in[i];
                if (c >= '1' && c <= '9') {
                    digits[nDigits++] = (char) c;
                    nTrailZero = 0;
                } else if (c == '0') {
                    digits[nDigits++] = (char) c;
                    nTrailZero++;
                } else if (c == '.') {
                    if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i - off;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                } else {
                    break digitLoop;
                }
                i++;
            }
            nDigits -= nTrailZero;

            boolean isZero = (nDigits == 0);
            if (isZero && nLeadZero == 0) {
                break parseNumber; // go throw exception
            }
            if (decSeen) {
                decExp = decPt - nLeadZero;
            } else {
                decExp = nDigits + nTrailZero;
            }

            if ((i < end) && (((c = in[i]) == 'e') || (c == 'E'))) {
                int expSign = 1;
                int expVal = 0;
                int reallyBig = Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch (in[++i]) {
                    case '-':
                        expSign = -1;
                    case '+':
                        i++;
                }
                int expAt = i;
                expLoop:
                while (i < end) {
                    if (expVal >= reallyBig) {
                        expOverflow = true;
                    }
                    c = in[i++];
                    if (c >= '0' && c <= '9') {
                        expVal = expVal * 10 + ((int) c - (int) '0');
                    } else {
                        i--;           // back up.
                        break expLoop; // stop parsing exponent.
                    }
                }
                int expLimit = FloatingDecimal.BIG_DECIMAL_EXPONENT + nDigits + nTrailZero;
                if (expOverflow || (expVal > expLimit)) {
                    decExp = expSign * expLimit;
                } else {
                    decExp = decExp + expSign * expVal;
                }

                if (i == expAt) {
                    break parseNumber; // certainly bad
                }
            }

            if (i < end && (i != end - 1)) {
                break parseNumber; // go throw exception
            }
            if (isZero) {
                return 0;
            }
            return floatValue(isNegative, decExp, digits, nDigits);
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new NumberFormatException("For input string: \"" + new String(in, off, len) + "\"");
    }

    public static float parseFloat(char[] in, int off, int len) throws NumberFormatException {
        boolean isNegative = false;
        boolean signSeen = false;
        int decExp;
        char c;
        int end = off + len;

        parseNumber:
        try {
            // throws NullPointerException if null
            if (len == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = off;
            switch (in[i]) {
                case '-':
                    isNegative = true;
                case '+':
                    i++;
                    signSeen = true;
            }

            char[] digits = new char[len];

            int nDigits = 0;
            boolean decSeen = false;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;

            skipLeadingZerosLoop:
            while (i < end) {
                c = in[i];
                if (c == '0') {
                    nLeadZero++;
                } else if (c == '.') {
                    if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i - off;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                } else {
                    break skipLeadingZerosLoop;
                }
                i++;
            }
            digitLoop:
            while (i < end) {
                c = in[i];
                if (c >= '1' && c <= '9') {
                    digits[nDigits++] = c;
                    nTrailZero = 0;
                } else if (c == '0') {
                    digits[nDigits++] = c;
                    nTrailZero++;
                } else if (c == '.') {
                    if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i - off;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                } else {
                    break digitLoop;
                }
                i++;
            }
            nDigits -= nTrailZero;

            boolean isZero = (nDigits == 0);
            if (isZero && nLeadZero == 0) {
                break parseNumber; // go throw exception
            }
            if (decSeen) {
                decExp = decPt - nLeadZero;
            } else {
                decExp = nDigits + nTrailZero;
            }

            if ((i < end) && (((c = in[i]) == 'e') || (c == 'E'))) {
                int expSign = 1;
                int expVal = 0;
                int reallyBig = Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch (in[++i]) {
                    case '-':
                        expSign = -1;
                    case '+':
                        i++;
                }
                int expAt = i;
                expLoop:
                while (i < end) {
                    if (expVal >= reallyBig) {
                        expOverflow = true;
                    }
                    c = in[i++];
                    if (c >= '0' && c <= '9') {
                        expVal = expVal * 10 + ((int) c - (int) '0');
                    } else {
                        i--;           // back up.
                        break expLoop; // stop parsing exponent.
                    }
                }
                int expLimit = FloatingDecimal.BIG_DECIMAL_EXPONENT + nDigits + nTrailZero;
                if (expOverflow || (expVal > expLimit)) {
                    decExp = expSign * expLimit;
                } else {
                    decExp = decExp + expSign * expVal;
                }

                if (i == expAt) {
                    break parseNumber; // certainly bad
                }
            }

            if (i < end && (i != end - 1)) {
                break parseNumber; // go throw exception
            }
            if (isZero) {
                return 0;
            }
            return floatValue(isNegative, decExp, digits, nDigits);
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new NumberFormatException("For input string: \"" + new String(in, off, len) + "\"");
    }

    public static double doubleValue(boolean isNegative, int decExp, char[] digits, int nDigits) {
        int kDigits = Math.min(nDigits, FloatingDecimal.MAX_DECIMAL_DIGITS + 1);

        int iValue = (int) digits[0] - (int) '0';
        int iDigits = Math.min(kDigits, FloatingDecimal.INT_DECIMAL_DIGITS);
        for (int i = 1; i < iDigits; i++) {
            iValue = iValue * 10 + (int) digits[i] - (int) '0';
        }
        long lValue = (long) iValue;
        for (int i = iDigits; i < kDigits; i++) {
            lValue = lValue * 10L + (long) ((int) digits[i] - (int) '0');
        }
        double dValue = (double) lValue;
        int exp = decExp - kDigits;

        if (nDigits <= FloatingDecimal.MAX_DECIMAL_DIGITS) {
            if (exp == 0 || dValue == 0.0) {
                return (isNegative) ? -dValue : dValue; // small floating integer
            } else if (exp >= 0) {
                if (exp <= FloatingDecimal.MAX_SMALL_TEN) {
                    double rValue = dValue * FloatingDecimal.SMALL_10_POW[exp];
                    return (isNegative) ? -rValue : rValue;
                }
                int slop = FloatingDecimal.MAX_DECIMAL_DIGITS - kDigits;
                if (exp <= FloatingDecimal.MAX_SMALL_TEN + slop) {
                    dValue *= FloatingDecimal.SMALL_10_POW[slop];
                    double rValue = dValue * FloatingDecimal.SMALL_10_POW[exp - slop];
                    return (isNegative) ? -rValue : rValue;
                }
            } else {
                if (exp >= -FloatingDecimal.MAX_SMALL_TEN) {
                    double rValue = dValue / FloatingDecimal.SMALL_10_POW[-exp];
                    return (isNegative) ? -rValue : rValue;
                }
            }
        }

        if (exp > 0) {
            if (decExp > FloatingDecimal.MAX_DECIMAL_EXPONENT + 1) {
                return (isNegative) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
            if ((exp & 15) != 0) {
                dValue *= FloatingDecimal.SMALL_10_POW[exp & 15];
            }
            if ((exp >>= 4) != 0) {
                int j;
                for (j = 0; exp > 1; j++, exp >>= 1) {
                    if ((exp & 1) != 0) {
                        dValue *= FloatingDecimal.BIG_10_POW[j];
                    }
                }

                double t = dValue * FloatingDecimal.BIG_10_POW[j];
                if (Double.isInfinite(t)) {
                    t = dValue / 2.0;
                    t *= FloatingDecimal.BIG_10_POW[j];
                    if (Double.isInfinite(t)) {
                        return (isNegative) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                    }
                    t = Double.MAX_VALUE;
                }
                dValue = t;
            }
        } else if (exp < 0) {
            exp = -exp;
            if (decExp < FloatingDecimal.MIN_DECIMAL_EXPONENT - 1) {
                return (isNegative) ? -0.0 : 0.0;
            }
            if ((exp & 15) != 0) {
                dValue /= FloatingDecimal.SMALL_10_POW[exp & 15];
            }
            if ((exp >>= 4) != 0) {
                int j;
                for (j = 0; exp > 1; j++, exp >>= 1) {
                    if ((exp & 1) != 0) {
                        dValue *= FloatingDecimal.TINY_10_POW[j];
                    }
                }

                double t = dValue * FloatingDecimal.TINY_10_POW[j];
                if (t == 0.0) {
                    t = dValue * 2.0;
                    t *= FloatingDecimal.TINY_10_POW[j];
                    if (t == 0.0) {
                        return (isNegative) ? -0.0 : 0.0;
                    }
                    t = Double.MIN_VALUE;
                }
                dValue = t;
            }
        }

        if (nDigits > FloatingDecimal.MAX_NDIGITS) {
            nDigits = FloatingDecimal.MAX_NDIGITS + 1;
            digits[FloatingDecimal.MAX_NDIGITS] = '1';
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
            int binexp = (int) (ieeeBits >>> FloatingDecimal.EXP_SHIFT);
            long bigBbits = ieeeBits & FloatingDecimal.DOUBLE_SIGNIF_BIT_MASK;
            if (binexp > 0) {
                bigBbits |= FloatingDecimal.FRACT_HOB;
            } else { // Normalize denormalized numbers.
                assert bigBbits != 0L : bigBbits; // doubleToBigInt(0.0)
                int leadingZeros = Long.numberOfLeadingZeros(bigBbits);
                int shift = leadingZeros - (63 - FloatingDecimal.EXP_SHIFT);
                bigBbits <<= shift;
                binexp = 1 - shift;
            }
            binexp -= FloatingDecimal.DOUBLE_EXP_BIAS;
            int lowOrderZeros = Long.numberOfTrailingZeros(bigBbits);
            bigBbits >>>= lowOrderZeros;
            final int bigIntExp = binexp - FloatingDecimal.EXP_SHIFT + lowOrderZeros;
            final int bigIntNBits = FloatingDecimal.EXP_SHIFT + 1 - lowOrderZeros;

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
            if (binexp <= -FloatingDecimal.DOUBLE_EXP_BIAS) {
                // This is going to be a denormalized number
                // (if not actually zero).
                // half an ULP is at 2^-(EXP_BIAS+EXP_SHIFT+1)
                hulpbias = binexp + lowOrderZeros + FloatingDecimal.DOUBLE_EXP_BIAS;
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
                if ((bigIntNBits == 1) && (bigIntExp > -FloatingDecimal.DOUBLE_EXP_BIAS + 1)) {
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
                if (ieeeBits == 0 || ieeeBits == FloatingDecimal.DOUBLE_EXP_BIT_MASK) { // 0.0 or Double.POSITIVE_INFINITY
                    break correctionLoop; // oops. Fell off end of range.
                }
                continue; // try again.
            }
        }
        if (isNegative) {
            ieeeBits |= FloatingDecimal.DOUBLE_SIGN_BIT_MASK;
        }
        return Double.longBitsToDouble(ieeeBits);
    }

    public static float floatValue(boolean isNegative, int decExponent, char[] digits, int nDigits) {
        int kDigits = Math.min(nDigits, FloatingDecimal.SINGLE_MAX_DECIMAL_DIGITS + 1);
        int iValue = (int) digits[0] - (int) '0';
        for (int i = 1; i < kDigits; i++) {
            iValue = iValue * 10 + (int) digits[i] - (int) '0';
        }
        float fValue = (float) iValue;
        int exp = decExponent - kDigits;

        if (nDigits <= FloatingDecimal.SINGLE_MAX_DECIMAL_DIGITS) {
            if (exp == 0 || fValue == 0.0f) {
                return (isNegative) ? -fValue : fValue; // small floating integer
            } else if (exp >= 0) {
                if (exp <= FloatingDecimal.SINGLE_MAX_SMALL_TEN) {
                    fValue *= FloatingDecimal.SINGLE_SMALL_10_POW[exp];
                    return (isNegative) ? -fValue : fValue;
                }
                int slop = FloatingDecimal.SINGLE_MAX_DECIMAL_DIGITS - kDigits;
                if (exp <= FloatingDecimal.SINGLE_MAX_SMALL_TEN + slop) {
                    fValue *= FloatingDecimal.SINGLE_SMALL_10_POW[slop];
                    fValue *= FloatingDecimal.SINGLE_SMALL_10_POW[exp - slop];
                    return (isNegative) ? -fValue : fValue;
                }
            } else {
                if (exp >= -FloatingDecimal.SINGLE_MAX_SMALL_TEN) {
                    fValue /= FloatingDecimal.SINGLE_SMALL_10_POW[-exp];
                    return (isNegative) ? -fValue : fValue;
                }
            }
        } else if ((decExponent >= nDigits) && (nDigits + decExponent <= FloatingDecimal.MAX_DECIMAL_DIGITS)) {
            long lValue = (long) iValue;
            for (int i = kDigits; i < nDigits; i++) {
                lValue = lValue * 10L + (long) ((int) digits[i] - (int) '0');
            }
            double dValue = (double) lValue;
            exp = decExponent - nDigits;
            dValue *= FloatingDecimal.SMALL_10_POW[exp];
            fValue = (float) dValue;
            return (isNegative) ? -fValue : fValue;
        }
        double dValue = fValue;
        if (exp > 0) {
            if (decExponent > FloatingDecimal.SINGLE_MAX_DECIMAL_EXPONENT + 1) {
                return (isNegative) ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            }
            if ((exp & 15) != 0) {
                dValue *= FloatingDecimal.SMALL_10_POW[exp & 15];
            }
            if ((exp >>= 4) != 0) {
                int j;
                for (j = 0; exp > 0; j++, exp >>= 1) {
                    if ((exp & 1) != 0) {
                        dValue *= FloatingDecimal.BIG_10_POW[j];
                    }
                }
            }
        } else if (exp < 0) {
            exp = -exp;
            if (decExponent < FloatingDecimal.SINGLE_MIN_DECIMAL_EXPONENT - 1) {
                return (isNegative) ? -0.0f : 0.0f;
            }
            if ((exp & 15) != 0) {
                dValue /= FloatingDecimal.SMALL_10_POW[exp & 15];
            }
            if ((exp >>= 4) != 0) {
                int j;
                for (j = 0; exp > 0; j++, exp >>= 1) {
                    if ((exp & 1) != 0) {
                        dValue *= FloatingDecimal.TINY_10_POW[j];
                    }
                }
            }
        }
        fValue = Math.max(Float.MIN_VALUE, Math.min(Float.MAX_VALUE, (float) dValue));

        if (nDigits > FloatingDecimal.SINGLE_MAX_NDIGITS) {
            nDigits = FloatingDecimal.SINGLE_MAX_NDIGITS + 1;
            digits[FloatingDecimal.SINGLE_MAX_NDIGITS] = '1';
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
            int binexp = ieeeBits >>> FloatingDecimal.SINGLE_EXP_SHIFT;
            int bigBbits = ieeeBits & FloatingDecimal.FLOAT_SIGNIF_BIT_MASK;
            if (binexp > 0) {
                bigBbits |= FloatingDecimal.SINGLE_FRACT_HOB;
            } else { // Normalize denormalized numbers.
                assert bigBbits != 0 : bigBbits; // floatToBigInt(0.0)
                int leadingZeros = Integer.numberOfLeadingZeros(bigBbits);
                int shift = leadingZeros - (31 - FloatingDecimal.SINGLE_EXP_SHIFT);
                bigBbits <<= shift;
                binexp = 1 - shift;
            }
            binexp -= FloatingDecimal.FLOAT_EXP_BIAS;
            int lowOrderZeros = Integer.numberOfTrailingZeros(bigBbits);
            bigBbits >>>= lowOrderZeros;
            final int bigIntExp = binexp - FloatingDecimal.SINGLE_EXP_SHIFT + lowOrderZeros;
            final int bigIntNBits = FloatingDecimal.SINGLE_EXP_SHIFT + 1 - lowOrderZeros;

            int B2 = B5; // powers of 2 in bigB
            int D2 = D5; // powers of 2 in bigD
            int Ulp2;   // powers of 2 in halfUlp.
            if (bigIntExp >= 0) {
                B2 += bigIntExp;
            } else {
                D2 -= bigIntExp;
            }
            Ulp2 = B2;

            int hulpbias;
            if (binexp <= -FloatingDecimal.FLOAT_EXP_BIAS) {
                hulpbias = binexp + lowOrderZeros + FloatingDecimal.FLOAT_EXP_BIAS;
            } else {
                hulpbias = 1 + lowOrderZeros;
            }
            B2 += hulpbias;
            D2 += hulpbias;

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
                if ((bigIntNBits == 1) && (bigIntExp > -FloatingDecimal.FLOAT_EXP_BIAS + 1)) {
                    Ulp2 -= 1;
                    if (Ulp2 < 0) {
                        Ulp2 = 0;
                        diff = diff.leftShift(1);
                    }
                }
            } else if (cmpResult < 0) {
                overvalue = false; // our candidate is too small.
                diff = bigD.rightInplaceSub(bigB); // bigB is not user further - reuse
            } else {
                break correctionLoop;
            }
            cmpResult = diff.cmpPow52(B5, Ulp2);
            if ((cmpResult) < 0) {
                break correctionLoop;
            } else if (cmpResult == 0) {
                if ((ieeeBits & 1) != 0) { // half ties to even
                    ieeeBits += overvalue ? -1 : 1; // nextDown or nextUp
                }
                break correctionLoop;
            } else {
                ieeeBits += overvalue ? -1 : 1; // nextDown or nextUp
                if (ieeeBits == 0 || ieeeBits == FloatingDecimal.FLOAT_EXP_BIT_MASK) { // 0.0 or Float.POSITIVE_INFINITY
                    break correctionLoop; // oops. Fell off end of range.
                }
                continue; // try again.
            }
        }
        if (isNegative) {
            ieeeBits |= FloatingDecimal.FLOAT_SIGN_BIT_MASK;
        }
        return Float.intBitsToFloat(ieeeBits);
    }

    static class Cache {
        volatile char[] chars;
    }

    static final Cache CACHE = new Cache();
    static final AtomicReferenceFieldUpdater<Cache, char[]> CHARS_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(Cache.class, char[].class, "chars");

    public static Class<?> getMapping(Type type) {
        if (type == null) {
            return null;
        }

        if (type.getClass() == Class.class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return getMapping(((ParameterizedType) type).getRawType());
        }

        if (type instanceof TypeVariable) {
            Type boundType = ((TypeVariable<?>) type).getBounds()[0];
            if (boundType instanceof Class) {
                return (Class) boundType;
            }
            return getMapping(boundType);
        }

        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getMapping(upperBounds[0]);
            }
        }

        if (type instanceof GenericArrayType) {
            Type genericComponentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(genericComponentType);
            return getArrayClass(componentClass);
        }

        return Object.class;
    }

    public static Date toDate(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Date) {
            return (Date) obj;
        }

        if (obj instanceof Instant) {
            Instant instant = (Instant) obj;
            return new Date(
                    instant.toEpochMilli());
        }

        if (obj instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime) obj;
            return new Date(
                    zdt.toInstant().toEpochMilli());
        }

        if (obj instanceof LocalDate) {
            LocalDate localDate = (LocalDate) obj;
            ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
            return new Date(
                    zdt.toInstant().toEpochMilli());
        }

        if (obj instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) obj;
            ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
            return new Date(
                    zdt.toInstant().toEpochMilli());
        }

        if (obj instanceof String) {
            return DateUtils.parseDate((String) obj);
        }

        if (obj instanceof Long || obj instanceof Integer) {
            return new Date(((Number) obj).longValue());
        }

        throw new JSONException("can not cast to Date from " + obj.getClass());
    }

    public static Instant toInstant(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Instant) {
            return (Instant) obj;
        }

        if (obj instanceof Date) {
            return ((Date) obj).toInstant();
        }

        if (obj instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime) obj;
            return zdt.toInstant();
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }

            JSONReader jsonReader;
            if (str.charAt(0) != '"') {
                jsonReader = JSONReader.of('"' + str + '"');
            } else {
                jsonReader = JSONReader.of(str);
            }
            return jsonReader.read(Instant.class);
        }

        if (obj instanceof Map) {
            return (Instant) ObjectReaderImplInstant.INSTANCE.createInstance((Map) obj, 0L);
        }

        throw new JSONException("can not cast to Date from " + obj.getClass());
    }

    public static Object[] cast(Object obj, Type[] types) {
        if (obj == null) {
            return null;
        }

        Object[] array = new Object[types.length];

        if (obj instanceof Collection) {
            int i = 0;
            for (Object item : (Collection) obj) {
                int index = i++;
                array[index] = TypeUtils.cast(item, types[index]);
            }
        } else {
            Class<?> objectClass = obj.getClass();
            if (objectClass.isArray()) {
                int length = Array.getLength(obj);
                for (int i = 0; i < array.length && i < length; i++) {
                    Object item = Array.get(obj, i);
                    array[i] = TypeUtils.cast(item, types[i]);
                }
            } else {
                throw new JSONException("can not cast to types " + JSON.toJSONString(types) + " from " + objectClass);
            }
        }

        return array;
    }

    public static String[] toStringArray(Object object) {
        if (object == null || object instanceof String[]) {
            return (String[]) object;
        }

        if (object instanceof Collection) {
            Collection collection = (Collection) object;
            String[] array = new String[collection.size()];
            int i = 0;
            for (Object item : (Collection) object) {
                int index = i++;
                array[index] = item == null || (item instanceof String) ? (String) item : item.toString();
            }
            return array;
        }

        Class<?> objectClass = object.getClass();
        if (objectClass.isArray()) {
            int length = Array.getLength(object);
            String[] array = new String[length];
            for (int i = 0; i < array.length; i++) {
                Object item = Array.get(object, i);
                array[i] = item == null || (item instanceof String) ? (String) item : item.toString();
            }
            return array;
        }

        return cast(object, String[].class);
    }

    public static <T> T cast(Object obj, Type type) {
        if (type instanceof Class) {
            return (T) cast(obj, (Class) type);
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        if (obj instanceof Collection) {
            ObjectReader objectReader = provider.getObjectReader(type);
            return (T) objectReader.createInstance((Collection) obj);
        }

        if (obj instanceof Map) {
            ObjectReader objectReader = provider.getObjectReader(type);
            return (T) objectReader.createInstance((Map) obj, 0L);
        }

        String json = JSON.toJSONString(obj);
        return JSON.parseObject(json, type);
    }

    public static <T> T cast(Object obj, Class<T> targetClass) {
        return cast(obj, targetClass, JSONFactory.getDefaultObjectReaderProvider());
    }

    public static <T> T cast(Object obj, Class<T> targetClass, ObjectReaderProvider provider) {
        if (obj == null) {
            return null;
        }

        if (targetClass.isInstance(obj)) {
            return (T) obj;
        }

        if (targetClass == Date.class) {
            return (T) toDate(obj);
        }

        if (targetClass == Instant.class) {
            return (T) toInstant(obj);
        }

        if (targetClass == String.class) {
            if (obj instanceof Character) {
                return (T) obj.toString();
            }

            return (T) JSON.toJSONString(obj);
        }

        if (targetClass == AtomicInteger.class) {
            return (T) new AtomicInteger(toIntValue(obj));
        }

        if (targetClass == AtomicLong.class) {
            return (T) new AtomicLong(toLongValue(obj));
        }

        if (targetClass == AtomicBoolean.class) {
            return (T) new AtomicBoolean((Boolean) obj);
        }

        if (obj instanceof Map) {
            ObjectReader objectReader = provider.getObjectReader(targetClass);
            return (T) objectReader.createInstance((Map) obj, 0L);
        }

        Function typeConvert = provider.getTypeConvert(obj.getClass(), targetClass);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (obj instanceof String) {
            String json = (String) obj;
            if (json.isEmpty() || "null".equals(json)) {
                return null;
            }

            JSONReader jsonReader;
            char first = json.trim().charAt(0);
            if (first == '"' || first == '{' || first == '[') {
                jsonReader = JSONReader.of(json);
            } else {
                jsonReader = JSONReader.of(
                        JSON.toJSONString(json));
            }

            ObjectReader objectReader = JSONFactory
                    .getDefaultObjectReaderProvider()
                    .getObjectReader(targetClass);
            return (T) objectReader.readObject(jsonReader, null, null, 0);
        }

        if (targetClass.isEnum()) {
            if (obj instanceof Integer) {
                int ordinal = ((Integer) obj).intValue();
                ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(targetClass);
                if (objectReader instanceof ObjectReaderImplEnum) {
                    Enum e = ((ObjectReaderImplEnum) objectReader).getEnumByOrdinal(ordinal);
                    return (T) e;
                }
            }
        }

        if (obj instanceof Collection) {
            ObjectReader objectReader = provider.getObjectReader(targetClass);
            return (T) objectReader.createInstance((Collection) obj);
        }

        throw new JSONException("can not cast to " + targetClass.getName() + ", from " + obj.getClass());
    }

    static final Map<Class, String> NAME_MAPPINGS = new IdentityHashMap<>();
    static final Map<String, Class> TYPE_MAPPINGS = new ConcurrentHashMap<>();

    static {
        CLASS_JSON_OBJECT_1x = loadClass("com.alibaba.fastjson.JSONObject");
        {
            Field field = null;
            if (CLASS_JSON_OBJECT_1x != null) {
                try {
                    field = CLASS_JSON_OBJECT_1x.getDeclaredField("map");
                    field.setAccessible(true);
                } catch (Throwable ignore) {
                    // ignore
                }
            }
            FIELD_JSON_OBJECT_1x_map = field;
        }

        CLASS_JSON_ARRAY_1x = loadClass("com.alibaba.fastjson.JSONArray");

        NAME_MAPPINGS.put(byte.class, "B");
        NAME_MAPPINGS.put(short.class, "S");
        NAME_MAPPINGS.put(int.class, "I");
        NAME_MAPPINGS.put(long.class, "J");
        NAME_MAPPINGS.put(float.class, "F");
        NAME_MAPPINGS.put(double.class, "D");
        NAME_MAPPINGS.put(char.class, "C");
        NAME_MAPPINGS.put(boolean.class, "Z");

        NAME_MAPPINGS.put(Object[].class, "[O");
        NAME_MAPPINGS.put(Object[][].class, "[[O");
        NAME_MAPPINGS.put(byte[].class, "[B");
        NAME_MAPPINGS.put(byte[][].class, "[[B");
        NAME_MAPPINGS.put(short[].class, "[S");
        NAME_MAPPINGS.put(short[][].class, "[[S");
        NAME_MAPPINGS.put(int[].class, "[I");
        NAME_MAPPINGS.put(int[][].class, "[[I");
        NAME_MAPPINGS.put(long[].class, "[J");
        NAME_MAPPINGS.put(long[][].class, "[[J");
        NAME_MAPPINGS.put(float[].class, "[F");
        NAME_MAPPINGS.put(float[][].class, "[[F");
        NAME_MAPPINGS.put(double[].class, "[D");
        NAME_MAPPINGS.put(double[][].class, "[[D");
        NAME_MAPPINGS.put(char[].class, "[C");
        NAME_MAPPINGS.put(char[][].class, "[[C");
        NAME_MAPPINGS.put(boolean[].class, "[Z");
        NAME_MAPPINGS.put(boolean[][].class, "[[Z");

        NAME_MAPPINGS.put(Byte[].class, "[Byte");
        NAME_MAPPINGS.put(Byte[][].class, "[[Byte");
        NAME_MAPPINGS.put(Short[].class, "[Short");
        NAME_MAPPINGS.put(Short[][].class, "[[Short");
        NAME_MAPPINGS.put(Integer[].class, "[Integer");
        NAME_MAPPINGS.put(Integer[][].class, "[[Integer");
        NAME_MAPPINGS.put(Long[].class, "[Long");
        NAME_MAPPINGS.put(Long[][].class, "[[Long");
        NAME_MAPPINGS.put(Float[].class, "[Float");
        NAME_MAPPINGS.put(Float[][].class, "[[Float");
        NAME_MAPPINGS.put(Double[].class, "[Double");
        NAME_MAPPINGS.put(Double[][].class, "[[Double");
        NAME_MAPPINGS.put(Character[].class, "[Character");
        NAME_MAPPINGS.put(Character[][].class, "[[Character");
        NAME_MAPPINGS.put(Boolean[].class, "[Boolean");
        NAME_MAPPINGS.put(Boolean[][].class, "[[Boolean");

        NAME_MAPPINGS.put(String[].class, "[String");
        NAME_MAPPINGS.put(String[][].class, "[[String");

        NAME_MAPPINGS.put(BigDecimal[].class, "[BigDecimal");
        NAME_MAPPINGS.put(BigDecimal[][].class, "[[BigDecimal");

        NAME_MAPPINGS.put(BigInteger[].class, "[BigInteger");
        NAME_MAPPINGS.put(BigInteger[][].class, "[[BigInteger");

        NAME_MAPPINGS.put(UUID[].class, "[UUID");
        NAME_MAPPINGS.put(UUID[][].class, "[[UUID");

        NAME_MAPPINGS.put(Object.class, "Object");
        NAME_MAPPINGS.put(Object[].class, "[O");

        NAME_MAPPINGS.put(HashMap.class, "M");
        TYPE_MAPPINGS.put("HashMap", HashMap.class);
        TYPE_MAPPINGS.put("java.util.HashMap", HashMap.class);

        NAME_MAPPINGS.put(LinkedHashMap.class, "LM");
        TYPE_MAPPINGS.put("LinkedHashMap", LinkedHashMap.class);
        TYPE_MAPPINGS.put("java.util.LinkedHashMap", LinkedHashMap.class);

        NAME_MAPPINGS.put(TreeMap.class, "TM");
        TYPE_MAPPINGS.put("TreeMap", TreeMap.class);

        NAME_MAPPINGS.put(ArrayList.class, "A");
        TYPE_MAPPINGS.put("ArrayList", ArrayList.class);
        TYPE_MAPPINGS.put("java.util.ArrayList", ArrayList.class);

        NAME_MAPPINGS.put(LinkedList.class, "LA");
        TYPE_MAPPINGS.put("LA", LinkedList.class);
        TYPE_MAPPINGS.put("LinkedList", LinkedList.class);
        TYPE_MAPPINGS.put("java.util.LinkedList", LinkedList.class);
        TYPE_MAPPINGS.put("java.util.concurrent.ConcurrentLinkedQueue", ConcurrentLinkedQueue.class);
        TYPE_MAPPINGS.put("java.util.concurrent.ConcurrentLinkedDeque", ConcurrentLinkedDeque.class);

        //java.util.LinkedHashMap.class,

        NAME_MAPPINGS.put(HashSet.class, "HashSet");
        NAME_MAPPINGS.put(TreeSet.class, "TreeSet");
        NAME_MAPPINGS.put(LinkedHashSet.class, "LinkedHashSet");
        NAME_MAPPINGS.put(ConcurrentHashMap.class, "ConcurrentHashMap");
        NAME_MAPPINGS.put(ConcurrentLinkedQueue.class, "ConcurrentLinkedQueue");
        NAME_MAPPINGS.put(ConcurrentLinkedDeque.class, "ConcurrentLinkedDeque");
        NAME_MAPPINGS.put(JSONObject.class, "JSONObject");
        NAME_MAPPINGS.put(JSONArray.class, "JSONArray");
        NAME_MAPPINGS.put(Currency.class, "Currency");
        NAME_MAPPINGS.put(TimeUnit.class, "TimeUnit");

        Class<?>[] classes = new Class[]{
                Object.class,
                Cloneable.class,
                AutoCloseable.class,
                java.lang.Exception.class,
                java.lang.RuntimeException.class,
                java.lang.IllegalAccessError.class,
                java.lang.IllegalAccessException.class,
                java.lang.IllegalArgumentException.class,
                java.lang.IllegalMonitorStateException.class,
                java.lang.IllegalStateException.class,
                java.lang.IllegalThreadStateException.class,
                java.lang.IndexOutOfBoundsException.class,
                java.lang.InstantiationError.class,
                java.lang.InstantiationException.class,
                java.lang.InternalError.class,
                java.lang.InterruptedException.class,
                java.lang.LinkageError.class,
                java.lang.NegativeArraySizeException.class,
                java.lang.NoClassDefFoundError.class,
                java.lang.NoSuchFieldError.class,
                java.lang.NoSuchFieldException.class,
                java.lang.NoSuchMethodError.class,
                java.lang.NoSuchMethodException.class,
                java.lang.NullPointerException.class,
                java.lang.NumberFormatException.class,
                java.lang.OutOfMemoryError.class,
                java.lang.SecurityException.class,
                java.lang.StackOverflowError.class,
                java.lang.StringIndexOutOfBoundsException.class,
                java.lang.TypeNotPresentException.class,
                java.lang.VerifyError.class,
                java.lang.StackTraceElement.class,
                java.util.Hashtable.class,
                java.util.TreeMap.class,
                java.util.IdentityHashMap.class,
                java.util.WeakHashMap.class,
                java.util.HashSet.class,
                java.util.LinkedHashSet.class,
                java.util.TreeSet.class,
                java.util.LinkedList.class,
                java.util.concurrent.TimeUnit.class,
                java.util.concurrent.ConcurrentHashMap.class,
                java.util.concurrent.atomic.AtomicInteger.class,
                java.util.concurrent.atomic.AtomicLong.class,
                java.util.Collections.EMPTY_MAP.getClass(),
                java.lang.Boolean.class,
                java.lang.Character.class,
                java.lang.Byte.class,
                java.lang.Short.class,
                java.lang.Integer.class,
                java.lang.Long.class,
                java.lang.Float.class,
                java.lang.Double.class,
                java.lang.Number.class,
                java.lang.String.class,
                java.math.BigDecimal.class,
                java.math.BigInteger.class,
                java.util.BitSet.class,
                java.util.Calendar.class,
                java.util.Date.class,
                java.util.Locale.class,
                java.util.UUID.class,
                java.util.Currency.class,
                java.text.SimpleDateFormat.class,
                JSONObject.class,
                JSONArray.class,
                java.util.concurrent.ConcurrentSkipListMap.class,
                java.util.concurrent.ConcurrentSkipListSet.class,
        };
        for (Class clazz : classes) {
            TYPE_MAPPINGS.put(clazz.getSimpleName(), clazz);
            TYPE_MAPPINGS.put(clazz.getName(), clazz);
            NAME_MAPPINGS.put(clazz, clazz.getSimpleName());
        }

        TYPE_MAPPINGS.put("JO10", JSONObject1O.class);
        TYPE_MAPPINGS.put("[O", Object[].class);
        TYPE_MAPPINGS.put("[Ljava.lang.Object;", Object[].class);
        TYPE_MAPPINGS.put("[java.lang.Object", Object[].class);
        TYPE_MAPPINGS.put("[Object", Object[].class);
        TYPE_MAPPINGS.put("StackTraceElement", StackTraceElement.class);
        TYPE_MAPPINGS.put("[StackTraceElement", StackTraceElement[].class);

        String[] items = new String[]{
                "java.util.Collections$UnmodifiableMap",
                "java.util.Collections$UnmodifiableCollection",
        };

        for (String className : items) {
            Class<?> clazz = loadClass(className);
            TYPE_MAPPINGS.put(clazz.getName(), clazz);
        }

        {
            if (CLASS_JSON_OBJECT_1x != null) {
                TYPE_MAPPINGS.putIfAbsent("JO1", CLASS_JSON_OBJECT_1x);
                TYPE_MAPPINGS.putIfAbsent(CLASS_JSON_OBJECT_1x.getName(), CLASS_JSON_OBJECT_1x);
            }
            if (CLASS_JSON_ARRAY_1x != null) {
                TYPE_MAPPINGS.putIfAbsent("JA1", CLASS_JSON_ARRAY_1x);
                TYPE_MAPPINGS.putIfAbsent(CLASS_JSON_ARRAY_1x.getName(), CLASS_JSON_ARRAY_1x);
            }
        }

        NAME_MAPPINGS.put(new HashMap().keySet().getClass(), "Set");
        NAME_MAPPINGS.put(new LinkedHashMap().keySet().getClass(), "Set");
        NAME_MAPPINGS.put(new TreeMap<>().keySet().getClass(), "Set");
        NAME_MAPPINGS.put(((Map) new ConcurrentHashMap()).keySet().getClass(), "Set"); // bug fix for android9
        NAME_MAPPINGS.put(((Map) new ConcurrentSkipListMap()).keySet().getClass(), "Set"); // bug fix for android9
        TYPE_MAPPINGS.put("Set", HashSet.class);

        NAME_MAPPINGS.put(new HashMap().values().getClass(), "List");
        NAME_MAPPINGS.put(new LinkedHashMap().values().getClass(), "List");
        NAME_MAPPINGS.put(new TreeMap().values().getClass(), "List");
        NAME_MAPPINGS.put(new ConcurrentHashMap().values().getClass(), "List");
        NAME_MAPPINGS.put(new ConcurrentSkipListMap().values().getClass(), "List");

        TYPE_MAPPINGS.put("List", ArrayList.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$Map1", HashMap.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$MapN", LinkedHashMap.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$Set12", LinkedHashSet.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$SetN", LinkedHashSet.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$List12", ArrayList.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$ListN", ArrayList.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$SubList", ArrayList.class);

        for (Map.Entry<Class, String> entry : NAME_MAPPINGS.entrySet()) {
            TYPE_MAPPINGS.putIfAbsent(entry.getValue(), entry.getKey());
        }
    }

    public static String getTypeName(Class type) {
        String mapTypeName = NAME_MAPPINGS.get(type);
        if (mapTypeName != null) {
            return mapTypeName;
        }

        if (Proxy.isProxyClass(type)) {
            Class[] interfaces = type.getInterfaces();
            if (interfaces.length > 0) {
                type = interfaces[0];
            }
        }

        String typeName = type.getTypeName();
        switch (typeName) {
            case "com.alibaba.fastjson.JSONObject":
                NAME_MAPPINGS.putIfAbsent(type, "JO1");
                return NAME_MAPPINGS.get(type);
            case "com.alibaba.fastjson.JSONArray":
                NAME_MAPPINGS.putIfAbsent(type, "JA1");
                return NAME_MAPPINGS.get(type);
//            case "org.apache.commons.lang3.tuple.ImmutablePair":
//                NAME_MAPPINGS.putIfAbsent(type, "org.apache.commons.lang3.tuple.Pair");
//                return NAME_MAPPINGS.get(type);
            default:
                break;
        }

        return typeName;
    }

    public static Class getMapping(String typeName) {
        return TYPE_MAPPINGS.get(typeName);
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null || value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return new BigDecimal(str);
        }

        throw new JSONException("can not cast to decimal from " + value.getClass());
    }

    public static BigInteger toBigInteger(Object value) {
        if (value == null || value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return BigInteger.valueOf(((Number) value).longValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return new BigInteger(str);
        }

        throw new JSONException("can not cast to bigint");
    }

    public static Long toLong(Object value) {
        if (value == null || value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return Long.valueOf(((Number) value).longValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Long.parseLong(str);
        }

        throw new JSONException("can not cast to long, class " + value.getClass());
    }

    public static long toLongValue(Object value) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }
            return Long.parseLong(str);
        }

        throw new JSONException("can not cast to long from " + value.getClass());
    }

    public static Boolean parseBoolean(byte[] bytes, int off, int len) {
        switch (len) {
            case 0:
                return null;
            case 1: {
                byte b0 = bytes[off];
                if (b0 == '1' || b0 == 'Y') {
                    return Boolean.TRUE;
                }
                if (b0 == '0' || b0 == 'N') {
                    return Boolean.FALSE;
                }
                break;
            }
            case 4:
                if (bytes[off] == 't' && bytes[off + 1] == 'r' && bytes[off + 2] == 'u' && bytes[off + 3] == 'e') {
                    return Boolean.TRUE;
                }
                break;
            case 5:
                if (bytes[off] == 'f'
                        && bytes[off + 1] == 'a'
                        && bytes[off + 2] == 'l'
                        && bytes[off + 3] == 's'
                        && bytes[off + 4] == 'e') {
                    return Boolean.FALSE;
                }
                break;
            default:
                break;
        }

        String str = new String(bytes, off, len);
        return Boolean.parseBoolean(str);
    }

    public static Integer parseInt(byte[] bytes, int off, int len) {
        switch (len) {
            case 0:
                return null;
            case 1: {
                byte b0 = bytes[off];
                if (b0 >= '0' && b0 <= '9') {
                    return b0 - '0';
                }
                break;
            }
            case 2: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9') {
                    return (b0 - '0') * 10
                            + (b1 - '0');
                }
                break;
            }
            case 3: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9') {
                    return (b0 - '0') * 100
                            + (b1 - '0') * 10
                            + (b2 - '0');
                }
                break;
            }
            case 4: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                ) {
                    return (b0 - '0') * 1000
                            + (b1 - '0') * 100
                            + (b2 - '0') * 10
                            + (b3 - '0');
                }
                break;
            }
            case 5: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                byte b4 = bytes[off + 4];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                        && b4 >= '0' && b4 <= '9'
                ) {
                    return (b0 - '0') * 10_000
                            + (b1 - '0') * 1000
                            + (b2 - '0') * 100
                            + (b3 - '0') * 10
                            + (b4 - '0');
                }
                break;
            }
            case 6: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                byte b4 = bytes[off + 4];
                byte b5 = bytes[off + 5];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                        && b4 >= '0' && b4 <= '9'
                        && b5 >= '0' && b5 <= '9'
                ) {
                    return (b0 - '0') * 100_000
                            + (b1 - '0') * 10_000
                            + (b2 - '0') * 1_000
                            + (b3 - '0') * 100
                            + (b4 - '0') * 10
                            + (b5 - '0');
                }
                break;
            }
            case 7: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                byte b4 = bytes[off + 4];
                byte b5 = bytes[off + 5];
                byte b6 = bytes[off + 6];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                        && b4 >= '0' && b4 <= '9'
                        && b5 >= '0' && b5 <= '9'
                        && b6 >= '0' && b6 <= '9'
                ) {
                    return (b0 - '0') * 1_000_000
                            + (b1 - '0') * 100_000
                            + (b2 - '0') * 10_000
                            + (b3 - '0') * 1_000
                            + (b4 - '0') * 100
                            + (b5 - '0') * 10
                            + (b6 - '0');
                }
                break;
            }
            case 8: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                byte b4 = bytes[off + 4];
                byte b5 = bytes[off + 5];
                byte b6 = bytes[off + 6];
                byte b7 = bytes[off + 7];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                        && b4 >= '0' && b4 <= '9'
                        && b5 >= '0' && b5 <= '9'
                        && b6 >= '0' && b6 <= '9'
                        && b7 >= '0' && b7 <= '9'
                ) {
                    return (b0 - '0') * 10_000_000
                            + (b1 - '0') * 1_000_000
                            + (b2 - '0') * 100_000
                            + (b3 - '0') * 10_000
                            + (b4 - '0') * 1_000
                            + (b5 - '0') * 100
                            + (b6 - '0') * 10
                            + (b7 - '0');
                }
                break;
            }
            default:
                break;
        }

        String str = new String(bytes, off, len);
        return Integer.parseInt(str);
    }

    public static Long parseLong(byte[] bytes, int off, int len) {
        switch (len) {
            case 0:
                return null;
            case 1: {
                byte b0 = bytes[off];
                if (b0 >= '0' && b0 <= '9') {
                    return (long) (b0 - '0');
                }
                break;
            }
            case 2: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9') {
                    return (long) (b0 - '0') * 10
                            + (b1 - '0');
                }
                break;
            }
            case 3: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9') {
                    return (long) (b0 - '0') * 100
                            + (b1 - '0') * 10
                            + (b2 - '0');
                }
                break;
            }
            case 4: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9') {
                    return (long) (b0 - '0') * 1000
                            + (b1 - '0') * 100
                            + (b2 - '0') * 10
                            + (b3 - '0');
                }
                break;
            }
            case 5: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                byte b4 = bytes[off + 4];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                        && b4 >= '0' && b4 <= '9'
                ) {
                    return (long) (b0 - '0') * 10_000
                            + (b1 - '0') * 1000
                            + (b2 - '0') * 100
                            + (b3 - '0') * 10
                            + (b4 - '0');
                }
                break;
            }
            case 6: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                byte b4 = bytes[off + 4];
                byte b5 = bytes[off + 5];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                        && b4 >= '0' && b4 <= '9'
                        && b5 >= '0' && b5 <= '9'
                ) {
                    return (long) (b0 - '0') * 100_000
                            + (b1 - '0') * 10_000
                            + (b2 - '0') * 1_000
                            + (b3 - '0') * 100
                            + (b4 - '0') * 10
                            + (b5 - '0');
                }
                break;
            }
            case 7: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                byte b4 = bytes[off + 4];
                byte b5 = bytes[off + 5];
                byte b6 = bytes[off + 6];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                        && b4 >= '0' && b4 <= '9'
                        && b5 >= '0' && b5 <= '9'
                        && b6 >= '0' && b6 <= '9'
                ) {
                    return (long) (b0 - '0') * 1_000_000
                            + (b1 - '0') * 100_000
                            + (b2 - '0') * 10_000
                            + (b3 - '0') * 1_000
                            + (b4 - '0') * 100
                            + (b5 - '0') * 10
                            + (b6 - '0');
                }
                break;
            }
            case 8: {
                byte b0 = bytes[off];
                byte b1 = bytes[off + 1];
                byte b2 = bytes[off + 2];
                byte b3 = bytes[off + 3];
                byte b4 = bytes[off + 4];
                byte b5 = bytes[off + 5];
                byte b6 = bytes[off + 6];
                byte b7 = bytes[off + 7];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9'
                        && b2 >= '0' && b2 <= '9'
                        && b3 >= '0' && b3 <= '9'
                        && b4 >= '0' && b4 <= '9'
                        && b5 >= '0' && b5 <= '9'
                        && b6 >= '0' && b6 <= '9'
                        && b7 >= '0' && b7 <= '9'
                ) {
                    return (long) (b0 - '0') * 10_000_000
                            + (b1 - '0') * 1_000_000
                            + (b2 - '0') * 100_000
                            + (b3 - '0') * 10_000
                            + (b4 - '0') * 1_000
                            + (b5 - '0') * 100
                            + (b6 - '0') * 10
                            + (b7 - '0');
                }
                break;
            }
            default:
                break;
        }

        String str = new String(bytes, off, len);
        return Long.parseLong(str);
    }

    public static BigDecimal parseBigDecimal(byte[] bytes, int off, int len) {
        if (len == 0) {
            return null;
        }

        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) bytes[off + i];
        }
        return new BigDecimal(chars, 0, chars.length);
    }

    public static Integer toInteger(Object value) {
        if (value == null || value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Integer.parseInt(str);
        }

        if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
            return null;
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? 1 : 0;
        }

        throw new JSONException("can not cast to integer");
    }

    public static Byte toByte(Object value) {
        if (value == null || value instanceof Byte) {
            return (Byte) value;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Byte.parseByte(str);
        }

        throw new JSONException("can not cast to byte");
    }

    public static byte toByteValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Byte) {
            return (Byte) value;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }
            return Byte.parseByte(str);
        }

        throw new JSONException("can not cast to byte");
    }

    public static Short toShort(Object value) {
        if (value == null || value instanceof Short) {
            return (Short) value;
        }

        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Short.parseShort(str);
        }

        throw new JSONException("can not cast to byte");
    }

    public static short toShortValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Short) {
            return (Short) value;
        }

        if (value instanceof Number) {
            return (byte) ((Number) value).shortValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }
            return Short.parseShort(str);
        }

        throw new JSONException("can not cast to byte");
    }

    public static int toIntValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }

            if (str.indexOf('.') != -1) {
                return new BigDecimal(str).intValueExact();
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static boolean toBooleanValue(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return false;
            }
            return Boolean.parseBoolean(str);
        }

        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            if (intValue == 1) {
                return true;
            }
            if (intValue == 0) {
                return false;
            }
        }

        throw new JSONException("can not cast to boolean");
    }

    public static Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Boolean.parseBoolean(str);
        }

        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            if (intValue == 1) {
                return true;
            }
            if (intValue == 0) {
                return false;
            }
        }

        throw new JSONException("can not cast to boolean");
    }

    public static float toFloatValue(Object value) {
        if (value == null) {
            return 0F;
        }

        if (value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }
            return Float.parseFloat(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static Float toFloat(Object value) {
        if (value == null || value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Float.parseFloat(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static double toDoubleValue(Object value) {
        if (value == null) {
            return 0D;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0D;
            }
            return Double.parseDouble(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static Double toDouble(Object value) {
        if (value == null || value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Double.parseDouble(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static int compare(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            return ((Comparable) a).compareTo(b);
        }

        Class typeA = a.getClass();
        Class typeB = b.getClass();

        if (typeA == BigDecimal.class) {
            if (typeB == Integer.class) {
                b = new BigDecimal((Integer) b);
            } else if (typeB == Long.class) {
                b = new BigDecimal((Long) b);
            } else if (typeB == Float.class) {
                b = new BigDecimal((Float) b);
            } else if (typeB == Double.class) {
                b = new BigDecimal((Double) b);
            } else if (typeB == BigInteger.class) {
                b = new BigDecimal((BigInteger) b);
            }
        } else if (typeA == BigInteger.class) {
            if (typeB == Integer.class) {
                b = BigInteger.valueOf((Integer) b);
            } else if (typeB == Long.class) {
                b = BigInteger.valueOf((Long) b);
            } else if (typeB == Float.class) {
                b = new BigDecimal((Float) b);
                a = new BigDecimal((BigInteger) a);
            } else if (typeB == Double.class) {
                b = new BigDecimal((Double) b);
                a = new BigDecimal((BigInteger) a);
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal((BigInteger) a);
            }
        } else if (typeA == Long.class) {
            if (typeB == Integer.class) {
                b = new Long((Integer) b);
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal((Long) a);
            } else if (typeB == Float.class) {
                a = new Float((Long) a);
            } else if (typeB == Double.class) {
                a = new Double((Long) a);
            } else if (typeB == BigInteger.class) {
                a = BigInteger.valueOf((Long) a);
            } else if (typeB == String.class) {
                a = BigDecimal.valueOf((Long) a);
                b = new BigDecimal((String) b);
            }
        } else if (typeA == Integer.class) {
            if (typeB == Long.class) {
                a = new Long((Integer) a);
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal((Integer) a);
            } else if (typeB == BigInteger.class) {
                a = BigInteger.valueOf((Integer) a);
            } else if (typeB == Float.class) {
                a = new Float((Integer) a);
            } else if (typeB == Double.class) {
                a = new Double((Integer) a);
            } else if (typeB == String.class) {
                a = BigDecimal.valueOf((Integer) a);
                b = new BigDecimal((String) b);
            }
        } else if (typeA == Double.class) {
            if (typeB == Integer.class) {
                b = new Double((Integer) b);
            } else if (typeB == Long.class) {
                b = new Double((Long) b);
            } else if (typeB == Float.class) {
                b = new Double((Float) b);
            } else if (typeB == BigDecimal.class) {
                a = BigDecimal.valueOf((Double) a);
            } else if (typeB == String.class) {
                a = BigDecimal.valueOf((Double) a);
                b = new BigDecimal((String) b);
            } else if (typeB == BigInteger.class) {
                a = BigDecimal.valueOf((Double) a);
                b = new BigDecimal((BigInteger) b);
            }
        } else if (typeA == Float.class) {
            if (typeB == Integer.class) {
                b = new Float((Integer) b);
            } else if (typeB == Long.class) {
                b = new Float((Long) b);
            } else if (typeB == Double.class) {
                a = new Double((Float) a);
            } else if (typeB == BigDecimal.class) {
                a = BigDecimal.valueOf((Float) a);
            } else if (typeB == String.class) {
                a = BigDecimal.valueOf((Float) a);
                b = new BigDecimal((String) b);
            } else if (typeB == BigInteger.class) {
                a = BigDecimal.valueOf((Float) a);
                b = new BigDecimal((BigInteger) b);
            }
        } else if (typeA == String.class) {
            String strA = (String) a;
            if (typeB == Integer.class) {
                NumberFormatException error = null;
                try {
                    a = Integer.parseInt(strA);
                } catch (NumberFormatException ex) {
                    error = ex;
                }
                if (error != null) {
                    try {
                        a = Long.parseLong(strA);
                        b = Long.valueOf((Integer) b);
                        error = null;
                    } catch (NumberFormatException ex) {
                        error = ex;
                    }
                }
                if (error != null) {
                    a = new BigDecimal(strA);
                    b = BigDecimal.valueOf((Integer) b);
                }
            } else if (typeB == Long.class) {
                a = new BigDecimal(strA);
                b = BigDecimal.valueOf((Long) b);
            } else if (typeB == Float.class) {
                a = Float.parseFloat(strA);
            } else if (typeB == Double.class) {
                a = Double.parseDouble(strA);
            } else if (typeB == BigInteger.class) {
                a = new BigInteger(strA);
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal(strA);
            }
        }

        return ((Comparable) a).compareTo(b);
    }

    public static Object getDefaultValue(Type paramType) {
        if (paramType == int.class) {
            return 0;
        }

        if (paramType == long.class) {
            return 0L;
        }

        if (paramType == float.class) {
            return 0F;
        }

        if (paramType == double.class) {
            return 0D;
        }

        if (paramType == boolean.class) {
            return Boolean.FALSE;
        }

        if (paramType == short.class) {
            return (short) 0;
        }

        if (paramType == byte.class) {
            return (byte) 0;
        }

        if (paramType == char.class) {
            return (char) 0;
        }

        if (paramType == Optional.class) {
            return Optional.empty();
        }

        if (paramType == OptionalInt.class) {
            return OptionalInt.empty();
        }

        if (paramType == OptionalLong.class) {
            return OptionalLong.empty();
        }

        if (paramType == OptionalDouble.class) {
            return OptionalDouble.empty();
        }

        return null;
    }

    public static Class loadClass(String className) {
        if (className.length() >= 192) {
            return null;
        }

        switch (className) {
            case "O":
            case "Object":
            case "java.lang.Object":
                return Object.class;
            case "java.util.Collections$EmptyMap":
                return Collections.EMPTY_MAP.getClass();
            case "java.util.Collections$EmptyList":
                return Collections.EMPTY_LIST.getClass();
            case "java.util.Collections$EmptySet":
                return Collections.EMPTY_SET.getClass();
            case "java.util.Optional":
                return Optional.class;
            case "java.util.OptionalInt":
                return OptionalInt.class;
            case "java.util.OptionalLong":
                return OptionalLong.class;
            case "List":
            case "java.util.List":
                return List.class;
            case "A":
            case "ArrayList":
            case "java.util.ArrayList":
                return ArrayList.class;
            case "LA":
            case "LinkedList":
            case "java.util.LinkedList":
                return LinkedList.class;
            case "Map":
            case "java.util.Map":
                return Map.class;
            case "M":
            case "HashMap":
            case "java.util.HashMap":
                return HashMap.class;
            case "LM":
            case "LinkedHashMap":
            case "java.util.LinkedHashMap":
                return LinkedHashMap.class;
            case "ConcurrentHashMap":
                return ConcurrentHashMap.class;
            case "ConcurrentLinkedQueue":
                return ConcurrentLinkedQueue.class;
            case "ConcurrentLinkedDeque":
                return ConcurrentLinkedDeque.class;
            case "JSONObject":
                return JSONObject.class;
            case "JO1":
                className = "com.alibaba.fastjson.JSONObject";
                break;
            case "Set":
            case "java.util.Set":
                return Set.class;
            case "HashSet":
            case "java.util.HashSet":
                return HashSet.class;
            case "LinkedHashSet":
            case "java.util.LinkedHashSet":
                return LinkedHashSet.class;
            case "TreeSet":
            case "java.util.TreeSet":
                return TreeSet.class;
            case "java.lang.Class":
                return Class.class;
            case "java.lang.Integer":
                return Integer.class;
            case "java.lang.Long":
                return Long.class;
            case "String":
            case "java.lang.String":
                return String.class;
            case "[String":
                return String[].class;
            case "I":
            case "int":
                return int.class;
            case "S":
            case "short":
                return short.class;
            case "J":
            case "long":
                return long.class;
            case "Z":
            case "boolean":
                return boolean.class;
            case "B":
            case "byte":
                return byte.class;
            case "F":
            case "float":
                return float.class;
            case "D":
            case "double":
                return double.class;
            case "C":
            case "char":
                return char.class;
            case "[B":
            case "byte[]":
                return byte[].class;
            case "[S":
            case "short[]":
                return short[].class;
            case "[I":
            case "int[]":
                return int[].class;
            case "[J":
            case "long[]":
                return long[].class;
            case "[F":
            case "float[]":
                return float[].class;
            case "[D":
            case "double[]":
                return double[].class;
            case "[C":
            case "char[]":
                return char[].class;
            case "[Z":
            case "boolean[]":
                return boolean[].class;
            case "[O":
                return Object[].class;
            case "UUID":
                return UUID.class;
            case "Date":
                return Date.class;
            case "Calendar":
                return Calendar.class;
            case "java.io.IOException":
                return java.io.IOException.class;
            case "java.util.Collections$UnmodifiableRandomAccessList":
                return CLASS_UNMODIFIABLE_LIST;
            case "java.util.Arrays$ArrayList":
                return Arrays.asList(1).getClass();
            case "java.util.Collections$SingletonList":
                return CLASS_SINGLE_List;
            case "java.util.Collections$SingletonSet":
                return CLASS_SINGLE_SET;
            default:
                break;
        }

        Class mapping = TYPE_MAPPINGS.get(className);
        if (mapping != null) {
            return mapping;
        }

        if (className.startsWith("java.util.ImmutableCollections$")) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return CLASS_UNMODIFIABLE_LIST;
            }
        }

        if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
            className = className.substring(1, className.length() - 1);
        }

        if (className.charAt(0) == '[' || className.endsWith("[]")) {
            String itemClassName = className.charAt(0) == '[' ? className.substring(1) : className.substring(0, className.length() - 2);
            Class itemClass = loadClass(itemClassName);
            if (itemClass == null) {
                throw new JSONException("load class error " + className);
            }
            return Array.newInstance(itemClass, 0).getClass();
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                return contextClassLoader.loadClass(className);
            } catch (ClassNotFoundException ignored) {
            }
        }

        try {
            return JSON.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException ignored) {
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
        }

        return null;
    }

    public static Class<?> getArrayClass(Class componentClass) {
        if (componentClass == int.class) {
            return int[].class;
        }
        if (componentClass == byte.class) {
            return byte[].class;
        }
        if (componentClass == short.class) {
            return short[].class;
        }
        if (componentClass == long.class) {
            return long[].class;
        }
        if (componentClass == String.class) {
            return String[].class;
        }
        if (componentClass == Object.class) {
            return Object[].class;
        }
        return Array.newInstance(componentClass, 1).getClass();
    }

    public static Class<?> getClass(Type type) {
        if (type == null) {
            return null;
        }

        if (type.getClass() == Class.class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }

        if (type instanceof TypeVariable) {
            Type boundType = ((TypeVariable<?>) type).getBounds()[0];
            if (boundType instanceof Class) {
                return (Class) boundType;
            }
            return getClass(boundType);
        }

        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getClass(upperBounds[0]);
            }
        }

        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type componentType = genericArrayType.getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            return getArrayClass(componentClass);
        }

        return Object.class;
    }

    public static boolean isProxy(Class<?> clazz) {
        for (Class<?> item : clazz.getInterfaces()) {
            String interfaceName = item.getName();
            switch (interfaceName) {
                case "org.springframework.cglib.proxy.Factory":
                case "javassist.util.proxy.ProxyObject":
                case "org.apache.ibatis.javassist.util.proxy.ProxyObject":
                case "org.hibernate.proxy.HibernateProxy":
                case "org.springframework.context.annotation.ConfigurationClassEnhancer$EnhancedConfiguration":
                case "org.mockito.cglib.proxy.Factory":
                case "net.sf.cglib.proxy.Factory":
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    public static Map getInnerMap(Map object) {
        if (object == null || CLASS_JSON_OBJECT_1x == null || !CLASS_JSON_OBJECT_1x.isInstance(object) || FIELD_JSON_OBJECT_1x_map == null) {
            return object;
        }

        try {
            object = (Map) FIELD_JSON_OBJECT_1x_map.get(object);
        } catch (IllegalAccessException ignore) {
            // ignore
        }

        return object;
    }
}
