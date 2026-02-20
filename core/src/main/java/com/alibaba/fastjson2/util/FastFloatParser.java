/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson2.util;

/**
 * Fast float parser implementation inspired by FastDoubleParser.
 * This implementation provides optimized parsing for float values from byte arrays
 * and character sequences without creating intermediate String objects.
 *
 * <p>Key optimizations:
 * <ul>
 *   <li>Direct parsing without creating intermediate String objects</li>
 *   <li>Lookup tables for powers of 10</li>
 *   <li>Optimized fast path for common cases (≤8 significant digits)</li>
 * </ul>
 *
 * @author wenshao
 * @since 2.0.62
 */
public final class FastFloatParser {
    // Maximum number of significant digits we can handle in fast path for float
    static final int MAX_SIGNIFICANT_DIGITS = 8;

    // Maximum exponent value we need to handle
    static final int MAX_EXPONENT = 45;

    // Powers of 10 that can be represented exactly as float
    static final float[] POWERS_OF_10_FLOAT = {
            1e0f, 1e1f, 1e2f, 1e3f, 1e4f, 1e5f, 1e6f, 1e7f, 1e8f, 1e9f,
            1e10f, 1e11f, 1e12f, 1e13f, 1e14f, 1e15f, 1e16f, 1e17f, 1e18f, 1e19f,
            1e20f, 1e21f, 1e22f, 1e23f, 1e24f, 1e25f, 1e26f, 1e27f, 1e28f, 1e29f,
            1e30f, 1e31f, 1e32f, 1e33f, 1e34f, 1e35f, 1e36f, 1e37f, 1e38f
    };

    // Negative powers of 10 for float
    static final float[] NEGATIVE_POWERS_OF_10_FLOAT = {
            1e0f, 1e-1f, 1e-2f, 1e-3f, 1e-4f, 1e-5f, 1e-6f, 1e-7f, 1e-8f, 1e-9f,
            1e-10f, 1e-11f, 1e-12f, 1e-13f, 1e-14f, 1e-15f, 1e-16f, 1e-17f, 1e-18f, 1e-19f,
            1e-20f, 1e-21f, 1e-22f, 1e-23f, 1e-24f, 1e-25f, 1e-26f, 1e-27f, 1e-28f, 1e-29f,
            1e-30f, 1e-31f, 1e-32f, 1e-33f, 1e-34f, 1e-35f, 1e-36f, 1e-37f, 1e-38f, 1e-39f,
            1e-40f, 1e-41f, 1e-42f, 1e-43f, 1e-44f, 1e-45f
    };

    private FastFloatParser() {
        // utility class
    }

    /**
     * Parses a float value from a byte array using optimized fast path.
     *
     * @param bytes the byte array containing the number
     * @param off the start offset
     * @param len the length
     * @return the parsed float value
     * @throws NumberFormatException if the input is not a valid float
     */
    public static float parseFloat(byte[] bytes, int off, int len) throws NumberFormatException {
        if (bytes == null) {
            throw new NumberFormatException("null");
        }
        if (len == 0) {
            throw new NumberFormatException("empty String");
        }

        int end = off + len;
        int index = off;
        boolean negative = false;

        // Parse sign
        byte firstByte = bytes[index];
        if (firstByte == '-') {
            negative = true;
            index++;
        } else if (firstByte == '+') {
            index++;
        }

        if (index >= end) {
            throw new NumberFormatException("For input string: \"" + new String(bytes, off, len) + "\"");
        }

        // Check for special values
        int remaining = end - index;
        if (remaining >= 3) {
            byte b0 = bytes[index];
            byte b1 = bytes[index + 1];
            byte b2 = bytes[index + 2];

            // Check for NaN
            if ((b0 == 'N' || b0 == 'n') && (b1 == 'a' || b1 == 'A') && (b2 == 'N' || b2 == 'n')) {
                if (remaining == 3) {
                    return Float.NaN;
                }
            }

            // Check for Infinity
            if ((b0 == 'I' || b0 == 'i') && (b1 == 'n' || b1 == 'N') && (b2 == 'f' || b2 == 'F')) {
                if (remaining == 3 || (remaining >= 8 && isInfinity(bytes, index))) {
                    return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
                }
            }
        }

        // Parse digits
        long significand = 0;
        int significantDigits = 0;
        int exponent = 0;
        boolean seenDigit = false;
        boolean seenDecimalPoint = false;
        int digitsBeforeDecimal = 0;

        // Parse integer part and decimal point
        while (index < end) {
            byte b = bytes[index];
            if (b >= '0' && b <= '9') {
                seenDigit = true;
                if (!seenDecimalPoint) {
                    digitsBeforeDecimal++;
                }
                if (significantDigits < MAX_SIGNIFICANT_DIGITS) {
                    significand = significand * 10 + (b - '0');
                    significantDigits++;
                } else if (!seenDecimalPoint) {
                    exponent++;  // Extra digits before decimal point
                }
                index++;
            } else if (b == '.') {
                if (seenDecimalPoint) {
                    throw new NumberFormatException("multiple points");
                }
                seenDecimalPoint = true;
                index++;
            } else if (b == 'e' || b == 'E') {
                break;
            } else {
                break;
            }
        }

        // Count digits after decimal point for exponent adjustment
        if (seenDecimalPoint && significantDigits > 0) {
            int digitsAfterDecimal = significantDigits - digitsBeforeDecimal;
            if (digitsAfterDecimal < 0) {
                digitsAfterDecimal = 0;
            }
            exponent -= digitsAfterDecimal;
        }

        // Parse exponent
        if (index < end) {
            byte b = bytes[index];
            if (b == 'e' || b == 'E') {
                index++;
                if (index >= end) {
                    throw new NumberFormatException("For input string: \"" + new String(bytes, off, len) + "\"");
                }

                boolean negativeExp = false;
                if (bytes[index] == '-') {
                    negativeExp = true;
                    index++;
                } else if (bytes[index] == '+') {
                    index++;
                }

                if (index >= end || !isDigit(bytes[index])) {
                    throw new NumberFormatException("For input string: \"" + new String(bytes, off, len) + "\"");
                }

                int expValue = 0;
                while (index < end) {
                    byte expByte = bytes[index];
                    if (expByte >= '0' && expByte <= '9') {
                        expValue = expValue * 10 + (expByte - '0');
                        if (expValue > MAX_EXPONENT) {
                            // Exponent overflow: return infinity or zero based on sign
                            if (negativeExp) {
                                return negative ? -0.0f : 0.0f;
                            } else {
                                return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
                            }
                        }
                        index++;
                    } else {
                        break;
                    }
                }
                exponent += negativeExp ? -expValue : expValue;
            }
        }

        if (!seenDigit) {
            throw new NumberFormatException("For input string: \"" + new String(bytes, off, len) + "\"");
        }

        // Handle zero
        if (significand == 0) {
            return negative ? -0.0f : 0.0f;
        }

        // Compute result using lookup tables
        float result;
        if (exponent >= 0) {
            if (exponent < POWERS_OF_10_FLOAT.length) {
                result = significand * POWERS_OF_10_FLOAT[exponent];
            } else {
                return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            }
        } else {
            int negExp = -exponent;
            if (negExp < NEGATIVE_POWERS_OF_10_FLOAT.length) {
                result = significand * NEGATIVE_POWERS_OF_10_FLOAT[negExp];
            } else {
                return negative ? -0.0f : 0.0f;
            }
        }

        return negative ? -result : result;
    }

    /**
     * Parses a float value from a char array using optimized fast path.
     *
     * @param chars the char array containing the number
     * @param off the start offset
     * @param len the length
     * @return the parsed float value
     * @throws NumberFormatException if the input is not a valid float
     */
    public static float parseFloat(char[] chars, int off, int len) throws NumberFormatException {
        if (chars == null) {
            throw new NumberFormatException("null");
        }
        if (len == 0) {
            throw new NumberFormatException("empty String");
        }

        int end = off + len;
        int index = off;
        boolean negative = false;

        // Parse sign
        char firstChar = chars[index];
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        if (index >= end) {
            throw new NumberFormatException("For input string: \"" + new String(chars, off, len) + "\"");
        }

        // Check for special values
        int remaining = end - index;
        if (remaining >= 3) {
            char c0 = chars[index];
            char c1 = chars[index + 1];
            char c2 = chars[index + 2];

            // Check for NaN
            if ((c0 == 'N' || c0 == 'n') && (c1 == 'a' || c1 == 'A') && (c2 == 'N' || c2 == 'n')) {
                if (remaining == 3) {
                    return Float.NaN;
                }
            }

            // Check for Infinity
            if ((c0 == 'I' || c0 == 'i') && (c1 == 'n' || c1 == 'N') && (c2 == 'f' || c2 == 'F')) {
                if (remaining == 3 || (remaining >= 8 && isInfinity(chars, index))) {
                    return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
                }
            }
        }

        // Parse digits
        long significand = 0;
        int significantDigits = 0;
        int exponent = 0;
        boolean seenDigit = false;
        boolean seenDecimalPoint = false;
        int digitsBeforeDecimal = 0;

        // Parse integer part and decimal point
        while (index < end) {
            char c = chars[index];
            if (c >= '0' && c <= '9') {
                seenDigit = true;
                if (!seenDecimalPoint) {
                    digitsBeforeDecimal++;
                }
                if (significantDigits < MAX_SIGNIFICANT_DIGITS) {
                    significand = significand * 10 + (c - '0');
                    significantDigits++;
                } else if (!seenDecimalPoint) {
                    exponent++;  // Extra digits before decimal point
                }
                index++;
            } else if (c == '.') {
                if (seenDecimalPoint) {
                    throw new NumberFormatException("multiple points");
                }
                seenDecimalPoint = true;
                index++;
            } else if (c == 'e' || c == 'E') {
                break;
            } else {
                break;
            }
        }

        // Count digits after decimal point for exponent adjustment
        if (seenDecimalPoint && significantDigits > 0) {
            int digitsAfterDecimal = significantDigits - digitsBeforeDecimal;
            if (digitsAfterDecimal < 0) {
                digitsAfterDecimal = 0;
            }
            exponent -= digitsAfterDecimal;
        }

        // Parse exponent
        if (index < end) {
            char c = chars[index];
            if (c == 'e' || c == 'E') {
                index++;
                if (index >= end) {
                    throw new NumberFormatException("For input string: \"" + new String(chars, off, len) + "\"");
                }

                boolean negativeExp = false;
                if (chars[index] == '-') {
                    negativeExp = true;
                    index++;
                } else if (chars[index] == '+') {
                    index++;
                }

                if (index >= end || !isDigit(chars[index])) {
                    throw new NumberFormatException("For input string: \"" + new String(chars, off, len) + "\"");
                }

                int expValue = 0;
                while (index < end) {
                    char expChar = chars[index];
                    if (expChar >= '0' && expChar <= '9') {
                        expValue = expValue * 10 + (expChar - '0');
                        if (expValue > MAX_EXPONENT) {
                            // Exponent overflow: return infinity or zero based on sign
                            if (negativeExp) {
                                return negative ? -0.0f : 0.0f;
                            } else {
                                return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
                            }
                        }
                        index++;
                    } else {
                        break;
                    }
                }
                exponent += negativeExp ? -expValue : expValue;
            }
        }

        if (!seenDigit) {
            throw new NumberFormatException("For input string: \"" + new String(chars, off, len) + "\"");
        }

        // Handle zero
        if (significand == 0) {
            return negative ? -0.0f : 0.0f;
        }

        // Compute result using lookup tables
        float result;
        if (exponent >= 0) {
            if (exponent < POWERS_OF_10_FLOAT.length) {
                result = significand * POWERS_OF_10_FLOAT[exponent];
            } else {
                return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            }
        } else {
            int negExp = -exponent;
            if (negExp < NEGATIVE_POWERS_OF_10_FLOAT.length) {
                result = significand * NEGATIVE_POWERS_OF_10_FLOAT[negExp];
            } else {
                return negative ? -0.0f : 0.0f;
            }
        }

        return negative ? -result : result;
    }

    private static boolean isDigit(byte b) {
        return b >= '0' && b <= '9';
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isInfinity(byte[] bytes, int index) {
        int len = bytes.length - index;
        if (len >= 8) {
            return (bytes[index] == 'I' || bytes[index] == 'i')
                    && (bytes[index + 1] == 'n' || bytes[index + 1] == 'N')
                    && (bytes[index + 2] == 'f' || bytes[index + 2] == 'F')
                    && (bytes[index + 3] == 'i' || bytes[index + 3] == 'I')
                    && (bytes[index + 4] == 'n' || bytes[index + 4] == 'N')
                    && (bytes[index + 5] == 'i' || bytes[index + 5] == 'I')
                    && (bytes[index + 6] == 't' || bytes[index + 6] == 'T')
                    && (bytes[index + 7] == 'y' || bytes[index + 7] == 'Y');
        }
        return false;
    }

    private static boolean isInfinity(char[] chars, int index) {
        int len = chars.length - index;
        if (len >= 8) {
            return (chars[index] == 'I' || chars[index] == 'i')
                    && (chars[index + 1] == 'n' || chars[index + 1] == 'N')
                    && (chars[index + 2] == 'f' || chars[index + 2] == 'F')
                    && (chars[index + 3] == 'i' || chars[index + 3] == 'I')
                    && (chars[index + 4] == 'n' || chars[index + 4] == 'N')
                    && (chars[index + 5] == 'i' || chars[index + 5] == 'I')
                    && (chars[index + 6] == 't' || chars[index + 6] == 'T')
                    && (chars[index + 7] == 'y' || chars[index + 7] == 'Y');
        }
        return false;
    }
}
