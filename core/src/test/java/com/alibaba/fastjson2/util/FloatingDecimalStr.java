package com.alibaba.fastjson2.util;

public class FloatingDecimalStr {
    public static double parseDouble(String in, int off, int len) throws NumberFormatException {
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
            switch (in.charAt(i)) {
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
                c = in.charAt(i);
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
                c = in.charAt(i);
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

            if ((i < end) && (((c = in.charAt(i)) == 'e') || (c == 'E'))) {
                int expSign = 1;
                int expVal = 0;
                int reallyBig = Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch (in.charAt(++i)) {
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
                    c = in.charAt(i++);
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
            return TypeUtils.doubleValue(isNegative, decExp, digits, nDigits);
        } catch (StringIndexOutOfBoundsException e) {
        }

        throw new NumberFormatException("For input string: \"" + in.substring(off, end) + "\"");
    }

    public static float parseFloat(String in, int off, int len) throws NumberFormatException {
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
            switch (in.charAt(i)) {
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
                c = in.charAt(i);
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
                c = in.charAt(i);
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

            if ((i < end) && (((c = in.charAt(i)) == 'e') || (c == 'E'))) {
                int expSign = 1;
                int expVal = 0;
                int reallyBig = Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch (in.charAt(++i)) {
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
                    c = in.charAt(i++);
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
            return TypeUtils.floatValue(isNegative, decExp, digits, nDigits);
        } catch (StringIndexOutOfBoundsException e) {
        }

        throw new NumberFormatException("For input string: \"" + in.substring(off, end) + "\"");
    }
}
