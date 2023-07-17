package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.time.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterPrimitiveImpl;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.alibaba.fastjson2.util.JDKUtils.FIELD_DECIMAL_INT_COMPACT_OFFSET;

public class TypeUtils {
    public static final Class CLASS_JSON_OBJECT_1x;
    public static final Field FIELD_JSON_OBJECT_1x_map;
    public static final Class CLASS_JSON_ARRAY_1x;

    public static final Class CLASS_SINGLE_SET = Collections.singleton(1).getClass();
    public static final Class CLASS_SINGLE_LIST = Collections.singletonList(1).getClass();
    public static final Class CLASS_UNMODIFIABLE_COLLECTION = Collections.unmodifiableCollection(new ArrayList<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_LIST = Collections.unmodifiableList(new ArrayList<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_SET = Collections.unmodifiableSet(new HashSet<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet<>()).getClass();
    public static final ParameterizedType PARAM_TYPE_LIST_STR = new ParameterizedTypeImpl(List.class, String.class);

    public static final BigInteger BIGINT_INT32_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    public static final BigInteger BIGINT_INT32_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    public static final BigInteger BIGINT_INT64_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    public static final BigInteger BIGINT_INT64_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    /**
     * All the positive powers of 10 that can be
     * represented exactly in double/float.
     */
    public static final double[] SMALL_10_POW = {
            1.0e0, 1.0e1, 1.0e2, 1.0e3, 1.0e4,
            1.0e5, 1.0e6, 1.0e7, 1.0e8, 1.0e9,
            1.0e10, 1.0e11, 1.0e12, 1.0e13, 1.0e14,
            1.0e15, 1.0e16, 1.0e17, 1.0e18, 1.0e19,
            1.0e20, 1.0e21, 1.0e22
    };

    static final float[] SINGLE_SMALL_10_POW = {
            1.0e0f, 1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f,
            1.0e5f, 1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f,
            1.0e10f
    };

    static final double[] BIG_10_POW = {
            1e16, 1e32, 1e64, 1e128, 1e256};
    static final double[] TINY_10_POW = {
            1e-16, 1e-32, 1e-64, 1e-128, 1e-256};

    public static <T> T newProxyInstance(Class<T> objectClass, JSONObject object) {
        return (T) Proxy.newProxyInstance(objectClass.getClassLoader(), new Class[]{objectClass}, object);
    }

    static class X2 {
        static final String[] chars;
        static final String[] chars2;
        static final char START = ' '; // 32
        static final char END = '~'; // 126
        static final int SIZE2 = (END - START + 1);

        static {
            String[] array0 = new String[128];
            for (char i = 0; i < array0.length; i++) {
                array0[i] = Character.toString(i);
            }
            chars = array0;

            String[] array1 = new String[SIZE2 * SIZE2];
            char[] c2 = new char[2];
            for (char i = START; i <= END; i++) {
                for (char j = START; j <= END; j++) {
                    int value = (i - START) * SIZE2 + (j - START);
                    c2[0] = i;
                    c2[1] = j;
                    array1[value] = new String(c2);
                }
            }
            chars2 = array1;
        }
    }

    static char[] toAsciiCharArray(byte[] bytes) {
        char[] charArray = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            charArray[i] = (char) bytes[i];
        }
        return charArray;
    }

    public static String toString(char ch) {
        if (ch < X2.chars.length) {
            return X2.chars[ch];
        }
        return Character.toString(ch);
    }

    public static String toString(byte ch) {
        if (ch >= 0 && ch < X2.chars.length) {
            return X2.chars[ch];
        }
        return new String(new byte[]{ch}, IOUtils.ISO_8859_1);
    }

    public static String toString(char c0, char c1) {
        if (c0 >= X2.START && c0 <= X2.END && c1 >= X2.START && c1 <= X2.END) {
            int value = (c0 - X2.START) * X2.SIZE2 + (c1 - X2.START);
            return X2.chars2[value];
        }
        return new String(new char[]{c0, c1});
    }

    public static String toString(byte c0, byte c1) {
        if (c0 >= X2.START && c0 <= X2.END && c1 >= X2.START && c1 <= X2.END) {
            int value = (c0 - X2.START) * X2.SIZE2 + (c1 - X2.START);
            return X2.chars2[value];
        }
        return new String(new byte[]{c0, c1}, IOUtils.ISO_8859_1);
    }

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
                final int BIG_DECIMAL_EXPONENT = 324;
                int expLimit = BIG_DECIMAL_EXPONENT + nDigits + nTrailZero;
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
                final int BIG_DECIMAL_EXPONENT = 324;
                int expLimit = BIG_DECIMAL_EXPONENT + nDigits + nTrailZero;
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
                final int BIG_DECIMAL_EXPONENT = 324;
                int expLimit = BIG_DECIMAL_EXPONENT + nDigits + nTrailZero;
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
                final int BIG_DECIMAL_EXPONENT = 324;
                int expLimit = BIG_DECIMAL_EXPONENT + nDigits + nTrailZero;
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
        final int MAX_DECIMAL_EXPONENT = 308;
        final int MIN_DECIMAL_EXPONENT = -324;
        final int MAX_NDIGITS = 1100;
        final int INT_DECIMAL_DIGITS = 9;
        final int MAX_DECIMAL_DIGITS = 15;
        final int DOUBLE_EXP_BIAS = 1023;
        final int EXP_SHIFT = 53 /*DOUBLE_SIGNIFICAND_WIDTH*/ - 1;
        final long FRACT_HOB = (1L << EXP_SHIFT); // assumed High-Order bit
        final int MAX_SMALL_TEN = SMALL_10_POW.length - 1;
        final int SINGLE_MAX_SMALL_TEN = SINGLE_SMALL_10_POW.length - 1;

        int kDigits = Math.min(nDigits, MAX_DECIMAL_DIGITS + 1);

        int iValue = (int) digits[0] - (int) '0';
        int iDigits = Math.min(kDigits, INT_DECIMAL_DIGITS);
        for (int i = 1; i < iDigits; i++) {
            iValue = iValue * 10 + (int) digits[i] - (int) '0';
        }
        long lValue = iValue;
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
        bigD0.immutable = true;   // prevent bigD0 modification inside correctionLoop
        FDBigInteger bigD = null;
        int prevD2 = 0;

        correctionLoop:
        while (true) {
            // here ieeeBits can't be NaN, Infinity or zero
            int binexp = (int) (ieeeBits >>> EXP_SHIFT);
            final long DOUBLE_SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL;
            long bigBbits = ieeeBits & DOUBLE_SIGNIF_BIT_MASK;
            if (binexp > 0) {
                bigBbits |= FRACT_HOB;
            } else { // Normalize denormalized numbers.
                assert bigBbits != 0L : bigBbits; // doubleToBigInt(0.0)
                int leadingZeros = Long.numberOfLeadingZeros(bigBbits);
                int shift = leadingZeros - (63 - EXP_SHIFT);
                bigBbits <<= shift;
                binexp = 1 - shift;
            }
            binexp -= DOUBLE_EXP_BIAS;
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
            if (binexp <= -DOUBLE_EXP_BIAS) {
                // This is going to be a denormalized number
                // (if not actually zero).
                // half an ULP is at 2^-(EXP_BIAS+EXP_SHIFT+1)
                hulpbias = binexp + lowOrderZeros + DOUBLE_EXP_BIAS;
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
                if ((bigIntNBits == 1) && (bigIntExp > -DOUBLE_EXP_BIAS + 1)) {
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
                final long DOUBLE_EXP_BIT_MASK = 0x7FF0000000000000L;
                if (ieeeBits == 0 || ieeeBits == DOUBLE_EXP_BIT_MASK) { // 0.0 or Double.POSITIVE_INFINITY
                    break correctionLoop; // oops. Fell off end of range.
                }
                continue; // try again.
            }
        }
        if (isNegative) {
            final long DOUBLE_SIGN_BIT_MASK = 0x8000000000000000L;
            ieeeBits |= DOUBLE_SIGN_BIT_MASK;
        }
        return Double.longBitsToDouble(ieeeBits);
    }

    public static float floatValue(boolean isNegative, int decExponent, char[] digits, int nDigits) {
        final int SINGLE_MAX_NDIGITS = 200;
        final int SINGLE_MAX_DECIMAL_DIGITS = 7;
        final int MAX_DECIMAL_DIGITS = 15;
        final int FLOAT_EXP_BIAS = 127;
        final int SINGLE_EXP_SHIFT = 24 /*FLOAT_SIGNIFICAND_WIDTH*/ - 1;
        final int SINGLE_MAX_SMALL_TEN = SINGLE_SMALL_10_POW.length - 1;

        int kDigits = Math.min(nDigits, SINGLE_MAX_DECIMAL_DIGITS + 1);
        int iValue = (int) digits[0] - (int) '0';
        for (int i = 1; i < kDigits; i++) {
            iValue = iValue * 10 + (int) digits[i] - (int) '0';
        }
        float fValue = (float) iValue;
        int exp = decExponent - kDigits;

        if (nDigits <= SINGLE_MAX_DECIMAL_DIGITS) {
            if (exp == 0 || fValue == 0.0f) {
                return (isNegative) ? -fValue : fValue; // small floating integer
            } else if (exp >= 0) {
                if (exp <= SINGLE_MAX_SMALL_TEN) {
                    fValue *= SINGLE_SMALL_10_POW[exp];
                    return (isNegative) ? -fValue : fValue;
                }
                int slop = SINGLE_MAX_DECIMAL_DIGITS - kDigits;
                if (exp <= SINGLE_MAX_SMALL_TEN + slop) {
                    fValue *= SINGLE_SMALL_10_POW[slop];
                    fValue *= SINGLE_SMALL_10_POW[exp - slop];
                    return (isNegative) ? -fValue : fValue;
                }
            } else {
                if (exp >= -SINGLE_MAX_SMALL_TEN) {
                    fValue /= SINGLE_SMALL_10_POW[-exp];
                    return (isNegative) ? -fValue : fValue;
                }
            }
        } else if ((decExponent >= nDigits) && (nDigits + decExponent <= MAX_DECIMAL_DIGITS)) {
            long lValue = iValue;
            for (int i = kDigits; i < nDigits; i++) {
                lValue = lValue * 10L + (long) ((int) digits[i] - (int) '0');
            }
            double dValue = (double) lValue;
            exp = decExponent - nDigits;
            dValue *= SMALL_10_POW[exp];
            fValue = (float) dValue;
            return (isNegative) ? -fValue : fValue;
        }
        double dValue = fValue;
        if (exp > 0) {
            final int SINGLE_MAX_DECIMAL_EXPONENT = 38;
            if (decExponent > SINGLE_MAX_DECIMAL_EXPONENT + 1) {
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
            final int SINGLE_MIN_DECIMAL_EXPONENT = -45;
            if (decExponent < SINGLE_MIN_DECIMAL_EXPONENT - 1) {
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
        bigD0.immutable = true;   // prevent bigD0 modification inside correctionLoop
        FDBigInteger bigD = null;
        int prevD2 = 0;

        correctionLoop:
        while (true) {
            // here ieeeBits can't be NaN, Infinity or zero
            int binexp = ieeeBits >>> SINGLE_EXP_SHIFT;
            final int FLOAT_SIGNIF_BIT_MASK = 0x007FFFFF;
            int bigBbits = ieeeBits & FLOAT_SIGNIF_BIT_MASK;
            if (binexp > 0) {
                final int SINGLE_FRACT_HOB = 1 << 23;
                bigBbits |= SINGLE_FRACT_HOB;
            } else { // Normalize denormalized numbers.
                assert bigBbits != 0 : bigBbits; // floatToBigInt(0.0)
                int leadingZeros = Integer.numberOfLeadingZeros(bigBbits);
                int shift = leadingZeros - (31 - SINGLE_EXP_SHIFT);
                bigBbits <<= shift;
                binexp = 1 - shift;
            }
            binexp -= FLOAT_EXP_BIAS;
            int lowOrderZeros = Integer.numberOfTrailingZeros(bigBbits);
            bigBbits >>>= lowOrderZeros;
            final int bigIntExp = binexp - SINGLE_EXP_SHIFT + lowOrderZeros;
            final int bigIntNBits = SINGLE_EXP_SHIFT + 1 - lowOrderZeros;

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
            if (binexp <= -FLOAT_EXP_BIAS) {
                hulpbias = binexp + lowOrderZeros + FLOAT_EXP_BIAS;
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
                if ((bigIntNBits == 1) && (bigIntExp > -FLOAT_EXP_BIAS + 1)) {
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
                final int FLOAT_EXP_BIT_MASK = 0x7F800000;
                if (ieeeBits == 0 || ieeeBits == FLOAT_EXP_BIT_MASK) { // 0.0 or Float.POSITIVE_INFINITY
                    break correctionLoop; // oops. Fell off end of range.
                }
                continue; // try again.
            }
        }
        if (isNegative) {
            final int FLOAT_SIGN_BIT_MASK = 0x80000000;
            ieeeBits |= FLOAT_SIGN_BIT_MASK;
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
            ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.DEFAULT_ZONE_ID);
            return new Date(
                    zdt.toInstant().toEpochMilli());
        }

        if (obj instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) obj;
            ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.DEFAULT_ZONE_ID);
            return new Date(
                    zdt.toInstant().toEpochMilli());
        }

        if (obj instanceof String) {
            long millis = DateUtils.parseMillis((String) obj, ZoneId.DEFAULT_ZONE_ID);
            if (millis == 0) {
                return null;
            } else {
                return new Date(millis);
            }
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
            Instant.ofEpochMilli(((Date) obj).getTime());
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

            try (JSONReader jsonReader = JSONReader.of(
                    str.charAt(0) != '"'
                            ? '"' + str + '"'
                            : str
            )) {
                return jsonReader.read(Instant.class);
            }
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

        ObjectReaderProvider provider = JSONFactory.defaultObjectReaderProvider;

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
        return cast(obj, targetClass, JSONFactory.defaultObjectReaderProvider);
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
                int intValue = ((Integer) obj).intValue();
                ObjectReader objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(targetClass);
                if (objectReader instanceof ObjectReaderImplEnum) {
                    return (T) ((ObjectReaderImplEnum) objectReader).of(intValue);
                }

                if (objectReader instanceof ObjectReaderImplEnum2X4) {
                    return (T) ((ObjectReaderImplEnum2X4) objectReader).getEnumByOrdinal(intValue);
                }
            }
        }

        if (obj instanceof Collection) {
            ObjectReader objectReader = provider.getObjectReader(targetClass);
            return (T) objectReader.createInstance((Collection) obj);
        }

        String className = targetClass.getName();
        if (obj instanceof Integer || obj instanceof Long) {
            long millis = ((Number) obj).longValue();
            switch (className) {
                case "java.sql.Date":
                    return (T) new java.sql.Date(millis);
                case "java.sql.Timestamp":
                    return (T) new java.sql.Timestamp(millis);
                case "java.sql.Time":
                    return (T) new java.sql.Time(millis);
                default:
                    break;
            }
        }

        ObjectWriter objectWriter = JSONFactory
                .defaultObjectWriterProvider
                .getObjectWriter(obj.getClass());
        if (objectWriter instanceof ObjectWriterPrimitiveImpl) {
            Function function = ((ObjectWriterPrimitiveImpl<?>) objectWriter).getFunction();
            if (function != null) {
                Object apply = function.apply(obj);
                if (targetClass.isInstance(apply)) {
                    return (T) apply;
                }
            }
        }

        throw new JSONException("can not cast to " + className + ", from " + obj.getClass());
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

        //java.util.LinkedHashMap.class,

        NAME_MAPPINGS.put(HashSet.class, "HashSet");
        NAME_MAPPINGS.put(TreeSet.class, "TreeSet");
        NAME_MAPPINGS.put(LinkedHashSet.class, "LinkedHashSet");
        NAME_MAPPINGS.put(ConcurrentHashMap.class, "ConcurrentHashMap");
        NAME_MAPPINGS.put(ConcurrentLinkedQueue.class, "ConcurrentLinkedQueue");
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
                TYPE_MAPPINGS.put("JO1", CLASS_JSON_OBJECT_1x);
                TYPE_MAPPINGS.put(CLASS_JSON_OBJECT_1x.getName(), CLASS_JSON_OBJECT_1x);
            }
            if (CLASS_JSON_ARRAY_1x != null) {
                TYPE_MAPPINGS.put("JA1", CLASS_JSON_ARRAY_1x);
                TYPE_MAPPINGS.put(CLASS_JSON_ARRAY_1x.getName(), CLASS_JSON_ARRAY_1x);
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
            String entryValue = entry.getValue();
            Class origin = TYPE_MAPPINGS.get(entryValue);
            if (origin == null) {
                TYPE_MAPPINGS.put(entryValue, entry.getKey());
            }
        }
    }

    public static String getTypeName(Type type) {
        if (type instanceof Class) {
            return getTypeName((Class) type);
        }
        return "<non-class>";
    }

    public static String getTypeName(Class type) {
        String typeName = ((Class<?>) type).getName();

        switch (typeName) {
            case "com.alibaba.fastjson.JSONObject":
                return "JO1";
            case "com.alibaba.fastjson.JSONArray":
                return "JA1";
            case "com.alibaba.fastjson2.JSONObject":
                return "JSONObject";
            case "com.alibaba.fastjson2.JSONArray":
                return "JSONArray";
            case "java.util.HashMap":
                return "M";
            case "java.util.ArrayList":
                return "A";
            case "java.lang.Object":
                return "Object";
            case "java.util.List":
                return typeName;
            default:
                break;
        }

        String mapTypeName = NAME_MAPPINGS.get(type);
        if (mapTypeName != null) {
            return mapTypeName;
        }

        int index = typeName.indexOf('$');
        if (index != -1 && TypeUtils.isInteger(typeName.substring(index + 1))) {
            Class superclass = type.getSuperclass();
            if (Map.class.isAssignableFrom(superclass)) {
                return getTypeName(superclass);
            }
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

        return cast(value, BigDecimal.class);
    }

    public static BigDecimal toBigDecimal(long i) {
        return BigDecimal.valueOf(i);
    }

    public static BigDecimal toBigDecimal(float f) {
        byte[] bytes = new byte[15];
        int size = DoubleToDecimal.toString(f, bytes, 0, true);
        return parseBigDecimal(bytes, 0, size);
    }

    public static BigDecimal toBigDecimal(double d) {
        byte[] bytes = new byte[24];
        int size = DoubleToDecimal.toString(d, bytes, 0, true);
        return parseBigDecimal(bytes, 0, size);
    }

    public static BigDecimal toBigDecimal(String str) {
        if (str == null || str.isEmpty() || "null".equals(str)) {
            return null;
        }

        char[] chars = JDKUtils.getCharArray(str);
        return parseBigDecimal(chars, 0, chars.length);
    }

    public static BigDecimal toBigDecimal(char[] chars) {
        if (chars == null) {
            return null;
        }
        return parseBigDecimal(chars, 0, chars.length);
    }

    public static BigDecimal toBigDecimal(byte[] strBytes) {
        if (strBytes == null) {
            return null;
        }
        return parseBigDecimal(strBytes, 0, strBytes.length);
    }

    public static boolean isInt32(BigInteger value) {
        return value.compareTo(BIGINT_INT32_MIN) >= 0 && value.compareTo(BIGINT_INT32_MAX) <= 0;
    }

    public static boolean isInt64(BigInteger value) {
        return value.compareTo(BIGINT_INT64_MIN) >= 0 && value.compareTo(BIGINT_INT64_MAX) <= 0;
    }

    /**
     * decimal is integer, check has non-zero small
     *
     * @param decimal
     * @return
     */
    public static boolean isInteger(BigDecimal decimal) {
        int scale = decimal.scale();
        if (scale == 0) {
            return true;
        }

        int precision = decimal.precision();
        if (precision < 20) {
            if (FIELD_DECIMAL_INT_COMPACT_OFFSET != -1) {
                long intCompact = JDKUtils.UNSAFE.getLong(decimal, FIELD_DECIMAL_INT_COMPACT_OFFSET);
                switch (scale) {
                    case 1:
                        return intCompact % 10 == 0;
                    case 2:
                        return intCompact % 100 == 0;
                    case 3:
                        return intCompact % 1000 == 0;
                    case 4:
                        return intCompact % 10000 == 0;
                    case 5:
                        return intCompact % 100000 == 0;
                    case 6:
                        return intCompact % 1000000 == 0;
                    case 7:
                        return intCompact % 10000000 == 0;
                    case 8:
                        return intCompact % 100000000 == 0;
                    case 9:
                        return intCompact % 1000000000 == 0;
                    default:
                        break;
                }
            }
        }

        return decimal.stripTrailingZeros().scale() == 0;
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

    public static Boolean parseBoolean(char[] bytes, int off, int len) {
        switch (len) {
            case 0:
                return null;
            case 1: {
                char b0 = bytes[off];
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

    public static int parseInt(byte[] bytes, int off, int len) {
        switch (len) {
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

    public static int parseInt(char[] bytes, int off, int len) {
        switch (len) {
            case 1: {
                char b0 = bytes[off];
                if (b0 >= '0' && b0 <= '9') {
                    return b0 - '0';
                }
                break;
            }
            case 2: {
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9') {
                    return (b0 - '0') * 10
                            + (b1 - '0');
                }
                break;
            }
            case 3: {
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
                char b4 = bytes[off + 4];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
                char b4 = bytes[off + 4];
                char b5 = bytes[off + 5];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
                char b4 = bytes[off + 4];
                char b5 = bytes[off + 5];
                char b6 = bytes[off + 6];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
                char b4 = bytes[off + 4];
                char b5 = bytes[off + 5];
                char b6 = bytes[off + 6];
                char b7 = bytes[off + 7];
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

    public static long parseLong(byte[] bytes, int off, int len) {
        switch (len) {
            case 1: {
                byte b0 = bytes[off];
                if (b0 >= '0' && b0 <= '9') {
                    return (b0 - '0');
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

    public static long parseLong(char[] bytes, int off, int len) {
        switch (len) {
            case 1: {
                char b0 = bytes[off];
                if (b0 >= '0' && b0 <= '9') {
                    return (b0 - '0');
                }
                break;
            }
            case 2: {
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                if (b0 >= '0' && b0 <= '9'
                        && b1 >= '0' && b1 <= '9') {
                    return (long) (b0 - '0') * 10
                            + (b1 - '0');
                }
                break;
            }
            case 3: {
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
                char b4 = bytes[off + 4];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
                char b4 = bytes[off + 4];
                char b5 = bytes[off + 5];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
                char b4 = bytes[off + 4];
                char b5 = bytes[off + 5];
                char b6 = bytes[off + 6];
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
                char b0 = bytes[off];
                char b1 = bytes[off + 1];
                char b2 = bytes[off + 2];
                char b3 = bytes[off + 3];
                char b4 = bytes[off + 4];
                char b5 = bytes[off + 5];
                char b6 = bytes[off + 6];
                char b7 = bytes[off + 7];
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

    public static BigDecimal parseBigDecimal(char[] bytes, int off, int len) {
        if (bytes == null || len == 0) {
            return null;
        }

        boolean negative = false;
        int j = off;
        if (bytes[off] == '-') {
            negative = true;
            j++;
        }

        if (len <= 20 || (negative && len == 21)) {
            int end = off + len;
            int dot = 0;
            int dotIndex = -1;
            long unscaleValue = 0;
            for (; j < end; j++) {
                char b = bytes[j];
                if (b == '.') {
                    dot++;
                    if (dot > 1) {
                        break;
                    }
                    dotIndex = j;
                } else if (b >= '0' && b <= '9') {
                    long r = unscaleValue * 10;
                    if ((unscaleValue | 10) >>> 31 == 0L || (r / 10 == unscaleValue)) {
                        unscaleValue = r + (b - '0');
                    } else {
                        unscaleValue = -1;
                        break;
                    }
                } else {
                    unscaleValue = -1;
                    break;
                }
            }
            int scale = 0;
            if (unscaleValue >= 0 && dot <= 1) {
                if (negative) {
                    unscaleValue = -unscaleValue;
                }
                if (dotIndex != -1) {
                    scale = len - (dotIndex - off) - 1;
                }
                return BigDecimal.valueOf(unscaleValue, scale);
            }
        }

        return new BigDecimal(bytes, off, len);
    }

    public static BigDecimal parseBigDecimal(byte[] bytes, int off, int len) {
        if (bytes == null || len == 0) {
            return null;
        }

        boolean negative = false;
        int j = off;
        if (bytes[off] == '-') {
            negative = true;
            j++;
        }

        if (len <= 20 || (negative && len == 21)) {
            int end = off + len;
            int dot = 0;
            int dotIndex = -1;
            long unscaleValue = 0;
            for (; j < end; j++) {
                byte b = bytes[j];
                if (b == '.') {
                    dot++;
                    if (dot > 1) {
                        break;
                    }
                    dotIndex = j;
                } else if (b >= '0' && b <= '9') {
                    long r = unscaleValue * 10;
                    if ((unscaleValue | 10) >>> 31 == 0L || (r / 10 == unscaleValue)) {
                        unscaleValue = r + (b - '0');
                    } else {
                        unscaleValue = -1;
                        break;
                    }
                } else {
                    unscaleValue = -1;
                    break;
                }
            }
            int scale = 0;
            if (unscaleValue >= 0 && dot <= 1) {
                if (negative) {
                    unscaleValue = -unscaleValue;
                }
                if (dotIndex != -1) {
                    scale = len - (dotIndex - off) - 1;
                }
                return BigDecimal.valueOf(unscaleValue, scale);
            }
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
                b = BigDecimal.valueOf((Float) b);
            } else if (typeB == Double.class) {
                b = BigDecimal.valueOf((Double) b);
            } else if (typeB == BigInteger.class) {
                b = new BigDecimal((BigInteger) b);
            }
        } else if (typeA == BigInteger.class) {
            if (typeB == Integer.class) {
                b = BigInteger.valueOf((Integer) b);
            } else if (typeB == Long.class) {
                b = BigInteger.valueOf((Long) b);
            } else if (typeB == Float.class) {
                b = BigDecimal.valueOf((Float) b);
                a = new BigDecimal((BigInteger) a);
            } else if (typeB == Double.class) {
                b = BigDecimal.valueOf((Double) b);
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
                return CLASS_SINGLE_LIST;
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

    public static Class nonePrimitive(Class type) {
        if (type.isPrimitive()) {
            String name = type.getName();
            switch (name) {
                case "byte":
                    return Byte.class;
                case "short":
                    return Short.class;
                case "int":
                    return Integer.class;
                case "long":
                    return Long.class;
                case "float":
                    return Float.class;
                case "double":
                    return Double.class;
                case "char":
                    return Character.class;
                case "boolean":
                    return Boolean.class;
                default:
                    break;
            }
        }
        return type;
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
        if (CLASS_JSON_OBJECT_1x == null || !CLASS_JSON_OBJECT_1x.isInstance(object) || FIELD_JSON_OBJECT_1x_map == null) {
            return object;
        }

        try {
            object = (Map) FIELD_JSON_OBJECT_1x_map.get(object);
        } catch (IllegalAccessException ignore) {
            // ignore
        }

        return object;
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        char ch = str.charAt(0);
        boolean sign = ch == '-' || ch == '+';
        if (sign) {
            if (str.length() == 1) {
                return false;
            }
        } else if (ch < '0' || ch > '9') {
            return false;
        }

        for (int i = 1; i < str.length(); ++i) {
            ch = str.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isInteger(byte[] str, int off, int len) {
        if (str == null || len == 0) {
            return false;
        }

        char ch = (char) str[off];
        boolean sign = ch == '-' || ch == '+';
        if (sign) {
            if (len == 1) {
                return false;
            }
        } else if (ch < '0' || ch > '9') {
            return false;
        }

        final int end = off + len;
        for (int i = off + 1; i < end; ++i) {
            ch = (char) str[i];
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        char ch = str.charAt(0);
        int offset;
        boolean sign = ch == '-' || ch == '+';
        if (sign) {
            if (str.length() == 1) {
                return false;
            }
            ch = str.charAt(1);
            offset = 1;
        } else {
            if (ch == '.') {
                if (str.length() == 1) {
                    return false;
                }

                offset = 1;
            } else {
                offset = 0;
            }
        }

        int end = str.length();
        boolean dot = ch == '.';
        boolean space = false;
        boolean num = false;
        if (!dot && (ch >= '0' && ch <= '9')) {
            num = true;
            for (; ; ) {
                if (offset < end) {
                    ch = str.charAt(offset++);
                } else {
                    return true;
                }

                if (ch < '0' || ch > '9') {
                    break;
                }
            }
        }

        boolean small = false;
        if (ch == '.') {
            small = true;
            if (offset < end) {
                ch = str.charAt(offset++);
            } else {
                return true;
            }

            if (ch >= '0' && ch <= '9') {
                for (; ; ) {
                    if (offset < end) {
                        ch = str.charAt(offset++);
                    } else {
                        return true;
                    }

                    if (ch < '0' || ch > '9') {
                        break;
                    }
                }
            }
        }

        if (!num && !small) {
            return false;
        }

        if (ch == 'e' || ch == 'E') {
            if (offset == end) {
                return true;
            }

            ch = str.charAt(offset++);

            boolean eSign = false;
            if (ch == '+' || ch == '-') {
                eSign = true;
                if (offset < end) {
                    ch = str.charAt(offset++);
                } else {
                    return false;
                }
            }

            if (ch >= '0' && ch <= '9') {
                for (; ; ) {
                    if (offset < end) {
                        ch = str.charAt(offset++);
                    } else {
                        return true;
                    }

                    if (ch < '0' || ch > '9') {
                        break;
                    }
                }
            } else if (eSign) {
                return false;
            }
        }

        return false;
    }

    public static boolean isNumber(byte[] str, int off, int len) {
        if (str == null || len == 0) {
            return false;
        }

        char ch = (char) str[off];
        int offset;
        boolean sign = ch == '-' || ch == '+';
        if (sign) {
            if (len == 1) {
                return false;
            }
            ch = (char) str[off + 1];
            offset = off + 1;
        } else {
            if (ch == '.') {
                if (len == 1) {
                    return false;
                }

                offset = off + 1;
            } else {
                offset = off;
            }
        }

        int end = off + len;
        boolean dot = ch == '.';
        boolean space = false;
        boolean num = false;
        if (!dot && (ch >= '0' && ch <= '9')) {
            num = true;
            for (; ; ) {
                if (offset < end) {
                    ch = (char) str[offset++];
                } else {
                    return true;
                }

                if (ch < '0' || ch > '9') {
                    break;
                }
            }
        }

        boolean small = false;
        if (ch == '.') {
            small = true;
            if (offset < end) {
                ch = (char) str[offset++];
            } else {
                return true;
            }

            if (ch >= '0' && ch <= '9') {
                for (; ; ) {
                    if (offset < end) {
                        ch = (char) str[offset++];
                    } else {
                        return true;
                    }

                    if (ch < '0' || ch > '9') {
                        break;
                    }
                }
            }
        }

        if (!num && !small) {
            return false;
        }

        if (ch == 'e' || ch == 'E') {
            if (offset == end) {
                return true;
            }

            ch = (char) str[offset++];

            boolean eSign = false;
            if (ch == '+' || ch == '-') {
                eSign = true;
                if (offset < end) {
                    ch = (char) str[offset++];
                } else {
                    return false;
                }
            }

            if (ch >= '0' && ch <= '9') {
                for (; ; ) {
                    if (offset < end) {
                        ch = (char) str[offset++];
                    } else {
                        return true;
                    }

                    if (ch < '0' || ch > '9') {
                        break;
                    }
                }
            } else if (eSign) {
                return false;
            }
        }

        return false;
    }

    public static boolean isNumber(char[] str, int off, int len) {
        if (str == null || len == 0) {
            return false;
        }

        char ch = str[off];
        int offset;
        boolean sign = ch == '-' || ch == '+';
        if (sign) {
            if (len == 1) {
                return false;
            }
            ch = str[off + 1];
            offset = off + 1;
        } else {
            if (ch == '.') {
                if (len == 1) {
                    return false;
                }

                offset = off + 1;
            } else {
                offset = off;
            }
        }

        int end = off + len;
        boolean dot = ch == '.';
        boolean space = false;
        boolean num = false;
        if (!dot && (ch >= '0' && ch <= '9')) {
            num = true;
            for (; ; ) {
                if (offset < end) {
                    ch = str[offset++];
                } else {
                    return true;
                }

                if (ch < '0' || ch > '9') {
                    break;
                }
            }
        }

        boolean small = false;
        if (ch == '.') {
            small = true;
            if (offset < end) {
                ch = str[offset++];
            } else {
                return true;
            }

            if (ch >= '0' && ch <= '9') {
                for (; ; ) {
                    if (offset < end) {
                        ch = str[offset++];
                    } else {
                        return true;
                    }

                    if (ch < '0' || ch > '9') {
                        break;
                    }
                }
            }
        }

        if (!num && !small) {
            return false;
        }

        if (ch == 'e' || ch == 'E') {
            if (offset == end) {
                return true;
            }

            ch = str[offset++];

            boolean eSign = false;
            if (ch == '+' || ch == '-') {
                eSign = true;
                if (offset < end) {
                    ch = str[offset++];
                } else {
                    return false;
                }
            }

            if (ch >= '0' && ch <= '9') {
                for (; ; ) {
                    if (offset < end) {
                        ch = str[offset++];
                    } else {
                        return true;
                    }

                    if (ch < '0' || ch > '9') {
                        break;
                    }
                }
            } else if (eSign) {
                return false;
            }
        }

        return false;
    }

    public static boolean isUUID(String str) {
        if (str == null) {
            return false;
        }

        if (str.length() == 32) {
            for (int i = 0; i < 32; i++) {
                char ch = str.charAt(i);
                boolean valid = (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
                if (!valid) {
                    return false;
                }
            }
            return true;
        }

        if (str.length() == 36) {
            for (int i = 0; i < 36; i++) {
                char ch = str.charAt(i);

                if (i == 8 || i == 13 || i == 18 || i == 23) {
                    if (ch != '-') {
                        return false;
                    }
                    continue;
                }

                boolean valid = (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
                if (!valid) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean validateIPv4(String str) {
        return validateIPv4(str, 0);
    }

    static boolean validateIPv4(String str, int off) {
        if (str == null) {
            return false;
        }

        int strlen = str.length();
        {
            final int len = strlen - off;
            if (len < 7 || len > 25) {
                return false;
            }
        }

        int start = off;
        int dotCount = 0;
        for (int i = off; i < strlen; i++) {
            char ch = str.charAt(i);
            if (ch == '.' || i == strlen - 1) {
                int end = ch == '.' ? i : i + 1;
                int n = end - start;

                char c0, c1, c2;
                switch (n) {
                    case 1:
                        c0 = str.charAt(end - 1);
                        if (c0 < '0' || c0 > '9') {
                            return false;
                        }
                        break;
                    case 2:
                        c0 = str.charAt(end - 2);
                        c1 = str.charAt(end - 1);

                        if (c0 < '0' || c0 > '9') {
                            return false;
                        }
                        if (c1 < '0' || c1 > '9') {
                            return false;
                        }
                        break;
                    case 3:
                        c0 = str.charAt(end - 3);
                        c1 = str.charAt(end - 2);
                        c2 = str.charAt(end - 1);

                        if (c0 < '0' || c0 > '2') {
                            return false;
                        }
                        if (c1 < '0' || c1 > '9') {
                            return false;
                        }
                        if (c2 < '0' || c2 > '9') {
                            return false;
                        }
                        int value = (c0 - '0') * 100 + (c1 - '0') * 10 + (c2 - '0');
                        if (value > 255) {
                            return false;
                        }
                        break;
                    default:
                        return false;
                }

                if (ch == '.') {
                    dotCount++;
                    start = i + 1;
                }
            }
        }

        return dotCount == 3;
    }

    public static boolean validateIPv6(String str) {
        if (str == null) {
            return false;
        }

        final int len = str.length();
        if (len < 2 || len > 39) {
            return false;
        }

        int start = 0;
        int colonCount = 0;
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch == '.') {
                boolean ipV4 = validateIPv4(str, start);
                if (!ipV4) {
                    return false;
                }
                break;
            }
            if (ch == ':' || i == len - 1) {
                int end = ch == ':' ? i : i + 1;
                int n = end - start;

                char c0, c1, c2, c3;
                switch (n) {
                    case 0:
                        break;
                    case 1:
                        c0 = str.charAt(end - 1);
                        if (!(c0 >= '0' && c0 <= '9' || (c0 >= 'A' && c0 <= 'F') || (c0 >= 'a' && c0 <= 'f'))) {
                            return false;
                        }
                        break;
                    case 2:
                        c0 = str.charAt(end - 2);
                        c1 = str.charAt(end - 1);

                        if (!(c0 >= '0' && c0 <= '9' || (c0 >= 'A' && c0 <= 'F') || (c0 >= 'a' && c0 <= 'f'))) {
                            return false;
                        }
                        if (!(c1 >= '0' && c1 <= '9' || (c1 >= 'A' && c1 <= 'F') || (c1 >= 'a' && c1 <= 'f'))) {
                            return false;
                        }
                        break;
                    case 3:
                        c0 = str.charAt(end - 3);
                        c1 = str.charAt(end - 2);
                        c2 = str.charAt(end - 1);

                        if (!(c0 >= '0' && c0 <= '9' || (c0 >= 'A' && c0 <= 'F') || (c0 >= 'a' && c0 <= 'f'))) {
                            return false;
                        }
                        if (!(c1 >= '0' && c1 <= '9' || (c1 >= 'A' && c1 <= 'F') || (c1 >= 'a' && c1 <= 'f'))) {
                            return false;
                        }
                        if (!(c2 >= '0' && c2 <= '9' || (c2 >= 'A' && c2 <= 'F') || (c2 >= 'a' && c2 <= 'f'))) {
                            return false;
                        }
                        break;
                    case 4:
                        c0 = str.charAt(end - 4);
                        c1 = str.charAt(end - 3);
                        c2 = str.charAt(end - 2);
                        c3 = str.charAt(end - 1);

                        if (!(c0 >= '0' && c0 <= '9' || (c0 >= 'A' && c0 <= 'F') || (c0 >= 'a' && c0 <= 'f'))) {
                            return false;
                        }
                        if (!(c1 >= '0' && c1 <= '9' || (c1 >= 'A' && c1 <= 'F') || (c1 >= 'a' && c1 <= 'f'))) {
                            return false;
                        }
                        if (!(c2 >= '0' && c2 <= '9' || (c2 >= 'A' && c2 <= 'F') || (c2 >= 'a' && c2 <= 'f'))) {
                            return false;
                        }
                        if (!(c3 >= '0' && c3 <= '9' || (c3 >= 'A' && c3 <= 'F') || (c3 >= 'a' && c3 <= 'f'))) {
                            return false;
                        }
                        break;
                    default:
                        return false;
                }

                if (ch == ':') {
                    colonCount++;
                    start = i + 1;
                }
            }
        }

        return colonCount > 0 && colonCount < 8;
    }

    private static final BigInteger[] BIG_TEN_POWERS_TABLE;

    static {
        BigInteger[] bigInts = new BigInteger[128];
        bigInts[0] = BigInteger.ONE;
        bigInts[1] = BigInteger.TEN;
        long longValue = 10;
        for (int i = 2; i < 19; ++i) {
            longValue *= 10;
            bigInts[i] = BigInteger.valueOf(longValue);
        }
        BigInteger bigInt = bigInts[18];
        for (int i = 19; i < 128; ++i) {
            bigInt = bigInt.multiply(BigInteger.TEN);
            bigInts[i] = bigInt;
        }
        BIG_TEN_POWERS_TABLE = bigInts;
    }

    public static double doubleValue(int signNum, long intCompact, int scale) {
        final int P_D = 53; // Double.PRECISION
        final int Q_MIN_D = -1074; //(Double.MIN_EXPONENT - (P_D - 1));
        final int Q_MAX_D = 971; // (Double.MAX_EXPONENT - (P_D - 1));
        final double L = 3.321928094887362;

        int bitLength = 64 - Long.numberOfLeadingZeros(intCompact);
        long qb = bitLength - (long) Math.ceil(scale * L);
        if (qb < Q_MIN_D - 2) {  // qb < -1_076
            return signNum * 0.0;
        }
        if (qb > Q_MAX_D + P_D + 1) {  // qb > 1_025
            /* If s <= -309 then qb >= 1_027, so these cases all end up here. */
            return signNum * Double.POSITIVE_INFINITY;
        }

        if (scale < 0) {
            BigInteger pow10 = BIG_TEN_POWERS_TABLE[-scale];
            BigInteger w = BigInteger.valueOf(intCompact);
            return signNum * w.multiply(pow10).doubleValue();
        }
        if (scale == 0) {
            return signNum * (double) intCompact;
        }

        BigInteger w = BigInteger.valueOf(intCompact);
        int ql = (int) qb - (P_D + 3);  // narrowing qb to an int is safe
        BigInteger pow10 = BIG_TEN_POWERS_TABLE[scale];
        BigInteger m, n;
        if (ql <= 0) {
            m = w.shiftLeft(-ql);
            n = pow10;
        } else {
            m = w;
            n = pow10.shiftLeft(ql);
        }

        BigInteger[] qr = m.divideAndRemainder(n);
        long i = qr[0].longValue();
        int sb = qr[1].signum();
        int dq = (Long.SIZE - (P_D + 2)) - Long.numberOfLeadingZeros(i);
        int eq = (Q_MIN_D - 2) - ql;
        if (dq >= eq) {
            return signNum * Math.scalb((double) (i | sb), ql);
        }

        /* Subnormal */
        long mask = (1L << eq) - 1;
        long j = i >> eq | Long.signum(i & mask) | sb;
        return signNum * Math.scalb((double) j, Q_MIN_D - 2);
    }

    public static float floatValue(int signNum, long intCompact, int scale) {
        final int P_F = 24;
        final int Q_MIN_F = -149;
        final int Q_MAX_F = 104;
        final double L = 3.321928094887362;

        int bitLength = 64 - Long.numberOfLeadingZeros(intCompact);
        long qb = bitLength - (long) Math.ceil(scale * L);
        if (qb < Q_MIN_F - 2) {  // qb < -151
            return signNum * 0.0f;
        }
        if (qb > Q_MAX_F + P_F + 1) {  // qb > 129
            return signNum * Float.POSITIVE_INFINITY;
        }
        if (scale < 0) {
            BigInteger w = BigInteger.valueOf(intCompact);
            return signNum * w.multiply(BIG_TEN_POWERS_TABLE[-scale]).floatValue();
        }

        BigInteger w = BigInteger.valueOf(intCompact);
        int ql = (int) qb - (P_F + 3);
        BigInteger pow10 = BIG_TEN_POWERS_TABLE[scale];
        BigInteger m, n;
        if (ql <= 0) {
            m = w.shiftLeft(-ql);
            n = pow10;
        } else {
            m = w;
            n = pow10.shiftLeft(ql);
        }
        BigInteger[] qr = m.divideAndRemainder(n);
        int i = qr[0].intValue();
        int sb = qr[1].signum();
        int dq = (Integer.SIZE - (P_F + 2)) - Integer.numberOfLeadingZeros(i);
        int eq = (Q_MIN_F - 2) - ql;
        if (dq >= eq) {
            return signNum * Math.scalb((float) (i | sb), ql);
        }
        int mask = (1 << eq) - 1;
        int j = i >> eq | (Integer.signum(i & mask)) | sb;
        return signNum * Math.scalb((float) j, Q_MIN_F - 2);
    }
}
