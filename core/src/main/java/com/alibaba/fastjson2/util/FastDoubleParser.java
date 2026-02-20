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
 * Fast double parser implementation inspired by FastDoubleParser.
 * This implementation provides optimized parsing for double values from byte arrays
 * and character sequences without creating intermediate String objects.
 *
 * <p>Key optimizations:
 * <ul>
 *   <li>Direct parsing without creating intermediate String objects</li>
 *   <li>Lookup tables for powers of 10</li>
 *   <li>Optimized fast path for common cases (≤17 significant digits)</li>
 * </ul>
 *
 * @author wenshao
 * @since 2.0.62
 */
public final class FastDoubleParser {
    // Maximum number of significant digits we can handle in fast path
    // Using 15 to ensure accuracy for most common cases
    static final int MAX_SIGNIFICANT_DIGITS = 15;

    // Maximum exponent value we need to handle
    static final int MAX_EXPONENT = 310;

    // Powers of 10 that can be represented exactly as double
    static final double[] POWERS_OF_10 = {
            1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9,
            1e10, 1e11, 1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18, 1e19,
            1e20, 1e21, 1e22, 1e23, 1e24, 1e25, 1e26, 1e27, 1e28, 1e29,
            1e30, 1e31, 1e32, 1e33, 1e34, 1e35, 1e36, 1e37, 1e38, 1e39,
            1e40, 1e41, 1e42, 1e43, 1e44, 1e45, 1e46, 1e47, 1e48, 1e49,
            1e50, 1e51, 1e52, 1e53, 1e54, 1e55, 1e56, 1e57, 1e58, 1e59,
            1e60, 1e61, 1e62, 1e63, 1e64, 1e65, 1e66, 1e67, 1e68, 1e69,
            1e70, 1e71, 1e72, 1e73, 1e74, 1e75, 1e76, 1e77, 1e78, 1e79,
            1e80, 1e81, 1e82, 1e83, 1e84, 1e85, 1e86, 1e87, 1e88, 1e89,
            1e90, 1e91, 1e92, 1e93, 1e94, 1e95, 1e96, 1e97, 1e98, 1e99,
            1e100, 1e101, 1e102, 1e103, 1e104, 1e105, 1e106, 1e107, 1e108, 1e109,
            1e110, 1e111, 1e112, 1e113, 1e114, 1e115, 1e116, 1e117, 1e118, 1e119,
            1e120, 1e121, 1e122, 1e123, 1e124, 1e125, 1e126, 1e127, 1e128, 1e129,
            1e130, 1e131, 1e132, 1e133, 1e134, 1e135, 1e136, 1e137, 1e138, 1e139,
            1e140, 1e141, 1e142, 1e143, 1e144, 1e145, 1e146, 1e147, 1e148, 1e149,
            1e150, 1e151, 1e152, 1e153, 1e154, 1e155, 1e156, 1e157, 1e158, 1e159,
            1e160, 1e161, 1e162, 1e163, 1e164, 1e165, 1e166, 1e167, 1e168, 1e169,
            1e170, 1e171, 1e172, 1e173, 1e174, 1e175, 1e176, 1e177, 1e178, 1e179,
            1e180, 1e181, 1e182, 1e183, 1e184, 1e185, 1e186, 1e187, 1e188, 1e189,
            1e190, 1e191, 1e192, 1e193, 1e194, 1e195, 1e196, 1e197, 1e198, 1e199,
            1e200, 1e201, 1e202, 1e203, 1e204, 1e205, 1e206, 1e207, 1e208, 1e209,
            1e210, 1e211, 1e212, 1e213, 1e214, 1e215, 1e216, 1e217, 1e218, 1e219,
            1e220, 1e221, 1e222, 1e223, 1e224, 1e225, 1e226, 1e227, 1e228, 1e229,
            1e230, 1e231, 1e232, 1e233, 1e234, 1e235, 1e236, 1e237, 1e238, 1e239,
            1e240, 1e241, 1e242, 1e243, 1e244, 1e245, 1e246, 1e247, 1e248, 1e249,
            1e250, 1e251, 1e252, 1e253, 1e254, 1e255, 1e256, 1e257, 1e258, 1e259,
            1e260, 1e261, 1e262, 1e263, 1e264, 1e265, 1e266, 1e267, 1e268, 1e269,
            1e270, 1e271, 1e272, 1e273, 1e274, 1e275, 1e276, 1e277, 1e278, 1e279,
            1e280, 1e281, 1e282, 1e283, 1e284, 1e285, 1e286, 1e287, 1e288, 1e289,
            1e290, 1e291, 1e292, 1e293, 1e294, 1e295, 1e296, 1e297, 1e298, 1e299,
            1e300, 1e301, 1e302, 1e303, 1e304, 1e305, 1e306, 1e307, 1e308
    };

    // Negative powers of 10
    static final double[] NEGATIVE_POWERS_OF_10 = {
            1e0, 1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7, 1e-8, 1e-9,
            1e-10, 1e-11, 1e-12, 1e-13, 1e-14, 1e-15, 1e-16, 1e-17, 1e-18, 1e-19,
            1e-20, 1e-21, 1e-22, 1e-23, 1e-24, 1e-25, 1e-26, 1e-27, 1e-28, 1e-29,
            1e-30, 1e-31, 1e-32, 1e-33, 1e-34, 1e-35, 1e-36, 1e-37, 1e-38, 1e-39,
            1e-40, 1e-41, 1e-42, 1e-43, 1e-44, 1e-45, 1e-46, 1e-47, 1e-48, 1e-49,
            1e-50, 1e-51, 1e-52, 1e-53, 1e-54, 1e-55, 1e-56, 1e-57, 1e-58, 1e-59,
            1e-60, 1e-61, 1e-62, 1e-63, 1e-64, 1e-65, 1e-66, 1e-67, 1e-68, 1e-69,
            1e-70, 1e-71, 1e-72, 1e-73, 1e-74, 1e-75, 1e-76, 1e-77, 1e-78, 1e-79,
            1e-80, 1e-81, 1e-82, 1e-83, 1e-84, 1e-85, 1e-86, 1e-87, 1e-88, 1e-89,
            1e-90, 1e-91, 1e-92, 1e-93, 1e-94, 1e-95, 1e-96, 1e-97, 1e-98, 1e-99,
            1e-100, 1e-101, 1e-102, 1e-103, 1e-104, 1e-105, 1e-106, 1e-107, 1e-108, 1e-109,
            1e-110, 1e-111, 1e-112, 1e-113, 1e-114, 1e-115, 1e-116, 1e-117, 1e-118, 1e-119,
            1e-120, 1e-121, 1e-122, 1e-123, 1e-124, 1e-125, 1e-126, 1e-127, 1e-128, 1e-129,
            1e-130, 1e-131, 1e-132, 1e-133, 1e-134, 1e-135, 1e-136, 1e-137, 1e-138, 1e-139,
            1e-140, 1e-141, 1e-142, 1e-143, 1e-144, 1e-145, 1e-146, 1e-147, 1e-148, 1e-149,
            1e-150, 1e-151, 1e-152, 1e-153, 1e-154, 1e-155, 1e-156, 1e-157, 1e-158, 1e-159,
            1e-160, 1e-161, 1e-162, 1e-163, 1e-164, 1e-165, 1e-166, 1e-167, 1e-168, 1e-169,
            1e-170, 1e-171, 1e-172, 1e-173, 1e-174, 1e-175, 1e-176, 1e-177, 1e-178, 1e-179,
            1e-180, 1e-181, 1e-182, 1e-183, 1e-184, 1e-185, 1e-186, 1e-187, 1e-188, 1e-189,
            1e-190, 1e-191, 1e-192, 1e-193, 1e-194, 1e-195, 1e-196, 1e-197, 1e-198, 1e-199,
            1e-200, 1e-201, 1e-202, 1e-203, 1e-204, 1e-205, 1e-206, 1e-207, 1e-208, 1e-209,
            1e-210, 1e-211, 1e-212, 1e-213, 1e-214, 1e-215, 1e-216, 1e-217, 1e-218, 1e-219,
            1e-220, 1e-221, 1e-222, 1e-223, 1e-224, 1e-225, 1e-226, 1e-227, 1e-228, 1e-229,
            1e-230, 1e-231, 1e-232, 1e-233, 1e-234, 1e-235, 1e-236, 1e-237, 1e-238, 1e-239,
            1e-240, 1e-241, 1e-242, 1e-243, 1e-244, 1e-245, 1e-246, 1e-247, 1e-248, 1e-249,
            1e-250, 1e-251, 1e-252, 1e-253, 1e-254, 1e-255, 1e-256, 1e-257, 1e-258, 1e-259,
            1e-260, 1e-261, 1e-262, 1e-263, 1e-264, 1e-265, 1e-266, 1e-267, 1e-268, 1e-269,
            1e-270, 1e-271, 1e-272, 1e-273, 1e-274, 1e-275, 1e-276, 1e-277, 1e-278, 1e-279,
            1e-280, 1e-281, 1e-282, 1e-283, 1e-284, 1e-285, 1e-286, 1e-287, 1e-288, 1e-289,
            1e-290, 1e-291, 1e-292, 1e-293, 1e-294, 1e-295, 1e-296, 1e-297, 1e-298, 1e-299,
            1e-300, 1e-301, 1e-302, 1e-303, 1e-304, 1e-305, 1e-306, 1e-307, 1e-308,
            // Note: 1e-309 to 1e-323 are smaller than Double.MIN_VALUE (4.9E-324)
            // but still representable as non-zero doubles with reduced precision
            Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE,
            Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE,
            Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE
    };

    private FastDoubleParser() {
        // utility class
    }

    /**
     * Parses a double value from a byte array using optimized fast path.
     * This method handles common cases without creating intermediate String objects.
     * For complex cases that cannot be handled by the fast path, it throws a ParseException
     * and the caller should fall back to a more robust (but slower) implementation.
     *
     * @param bytes the byte array containing the number
     * @param off the start offset
     * @param len the length
     * @return the parsed double value
     * @throws NumberFormatException if the input is not a valid double or cannot be handled by fast path
     */
    public static double parseDouble(byte[] bytes, int off, int len) throws NumberFormatException {
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
                    return Double.NaN;
                }
            }

            // Check for Infinity
            if ((b0 == 'I' || b0 == 'i') && (b1 == 'n' || b1 == 'N') && (b2 == 'f' || b2 == 'F')) {
                if (remaining == 3 || (remaining >= 8 && isInfinity(bytes, index))) {
                    return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }
            }
        }

        // Parse digits
        long significand = 0;
        int significantDigits = 0;
        int totalDigits = 0;  // Count all digits including trailing zeros
        int exponent = 0;
        boolean seenDigit = false;
        boolean seenDecimalPoint = false;
        int digitsBeforeDecimal = 0;
        int digitsAfterDecimal = 0;

        // Parse integer part and decimal point
        while (index < end) {
            byte b = bytes[index];
            if (b >= '0' && b <= '9') {
                seenDigit = true;
                totalDigits++;
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
            digitsAfterDecimal = significantDigits - digitsBeforeDecimal;
            if (digitsAfterDecimal < 0) {
                digitsAfterDecimal = 0;
            }
            exponent -= digitsAfterDecimal;
        }

        // If we have too many significant digits, the fast path may not be accurate.
        // Fall back to slow path for these cases.
        if (totalDigits > MAX_SIGNIFICANT_DIGITS) {
            throw new NumberFormatException("For input string: \"" + new String(bytes, off, len) + "\" - too many significant digits");
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
                                return negative ? -0.0 : 0.0;
                            } else {
                                return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
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
            return negative ? -0.0 : 0.0;
        }

        // Compute result using lookup tables
        double result;
        if (exponent >= 0) {
            if (exponent < POWERS_OF_10.length) {
                result = significand * POWERS_OF_10[exponent];
            } else {
                // Exponent too large for fast path
                return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
        } else {
            int negExp = -exponent;
            if (negExp < NEGATIVE_POWERS_OF_10.length) {
                result = significand * NEGATIVE_POWERS_OF_10[negExp];
            } else {
                // Exponent too small for fast path, return zero
                return negative ? -0.0 : 0.0;
            }
        }

        return negative ? -result : result;
    }

    /**
     * Parses a double value from a char array using optimized fast path.
     *
     * @param chars the char array containing the number
     * @param off the start offset
     * @param len the length
     * @return the parsed double value
     * @throws NumberFormatException if the input is not a valid double
     */
    public static double parseDouble(char[] chars, int off, int len) throws NumberFormatException {
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
                    return Double.NaN;
                }
            }

            // Check for Infinity
            if ((c0 == 'I' || c0 == 'i') && (c1 == 'n' || c1 == 'N') && (c2 == 'f' || c2 == 'F')) {
                if (remaining == 3 || (remaining >= 8 && isInfinity(chars, index))) {
                    return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }
            }
        }

        // Parse digits
        long significand = 0;
        int significantDigits = 0;
        int totalDigits = 0;  // Count all digits including trailing zeros
        int exponent = 0;
        boolean seenDigit = false;
        boolean seenDecimalPoint = false;
        int digitsBeforeDecimal = 0;

        // Parse integer part and decimal point
        while (index < end) {
            char c = chars[index];
            if (c >= '0' && c <= '9') {
                seenDigit = true;
                totalDigits++;
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

        // If we have too many significant digits, the fast path may not be accurate.
        // Fall back to slow path for these cases.
        if (totalDigits > MAX_SIGNIFICANT_DIGITS) {
            throw new NumberFormatException("For input string: \"" + new String(chars, off, len) + "\" - too many significant digits");
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
                                return negative ? -0.0 : 0.0;
                            } else {
                                return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
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
            return negative ? -0.0 : 0.0;
        }

        // Compute result using lookup tables
        double result;
        if (exponent >= 0) {
            if (exponent < POWERS_OF_10.length) {
                result = significand * POWERS_OF_10[exponent];
            } else {
                return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
        } else {
            int negExp = -exponent;
            if (negExp < NEGATIVE_POWERS_OF_10.length) {
                result = significand * NEGATIVE_POWERS_OF_10[negExp];
            } else {
                return negative ? -0.0 : 0.0;
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
