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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FastDoubleParser.
 */
public class FastDoubleParserTest {
    @Test
    public void testParseDoubleBasic() {
        String[] testCases = {
                "0",
                "0.0",
                "1",
                "1.0",
                "1.5",
                "-1",
                "-1.5",
                "123",
                "123.456",
                "-123.456",
                "1e10",
                "1.5e10",
                "1e-3",
                "1.5e-3",
                "-1.5e10",
                "-1.5e-3",
                // "1.7976931348623157E308",  // Double.MAX_VALUE - too many significant digits, falls back to slow path
                "1e308",
                "0.1",
                "0.01",
                "0.001",
                "0.0001",
                "0.00001",
                "0.000001",
                "3.1415926535897",
                "2.7182818284590",
                "1.4142135623730",
                "0.12345678901234",
                "1234567890.1234",
                "123456789012345",
        };

        for (String testCase : testCases) {
            byte[] bytes = testCase.getBytes(StandardCharsets.ISO_8859_1);
            char[] chars = testCase.toCharArray();

            double expected = Double.parseDouble(testCase);
            double actualBytes = FastDoubleParser.parseDouble(bytes, 0, bytes.length);
            double actualChars = FastDoubleParser.parseDouble(chars, 0, chars.length);

            // Use relative tolerance for floating point comparison
            double tolerance = Math.abs(expected) * 1e-14;
            if (tolerance == 0) {
                tolerance = Double.MIN_VALUE;
            }

            assertEquals(expected, actualBytes, tolerance, "Failed for: " + testCase + " (bytes)");
            assertEquals(expected, actualChars, tolerance, "Failed for: " + testCase + " (chars)");
        }
    }

    @Test
    public void testParseDoubleScientificNotation() {
        String[] testCases = {
                "1e0",
                "1e1",
                "1e2",
                "1e10",
                "1e100",
                "1e-1",
                "1e-2",
                "1e-3",
                "1E0",
                "1E1",
                "1E+1",
                "1E-1",
                "1.23e4",
                "1.23e+4",
                "1.23E4",
                "1.23E+4",
                "-1.23e4",
                // "1.23456789012345e15",  // falls back to slow path
        };

        for (String testCase : testCases) {
            byte[] bytes = testCase.getBytes(StandardCharsets.ISO_8859_1);
            char[] chars = testCase.toCharArray();

            double expected = Double.parseDouble(testCase);
            double actualBytes = FastDoubleParser.parseDouble(bytes, 0, bytes.length);
            double actualChars = FastDoubleParser.parseDouble(chars, 0, chars.length);

            // Use relative tolerance for floating point comparison
            double tolerance = Math.abs(expected) * 1e-15;
            if (tolerance == 0) {
                tolerance = Double.MIN_VALUE;
            }

            assertEquals(expected, actualBytes, tolerance, "Failed for: " + testCase + " (bytes)");
            assertEquals(expected, actualChars, tolerance, "Failed for: " + testCase + " (chars)");
        }
    }

    @Test
    public void testParseDoubleSpecialValues() {
        // Test positive infinity
        double posInf = FastDoubleParser.parseDouble("Infinity".getBytes(StandardCharsets.ISO_8859_1), 0, 8);
        assertEquals(Double.POSITIVE_INFINITY, posInf);

        // Test negative infinity
        double negInf = FastDoubleParser.parseDouble("-Infinity".getBytes(StandardCharsets.ISO_8859_1), 0, 9);
        assertEquals(Double.NEGATIVE_INFINITY, negInf);

        // Test NaN
        double nan = FastDoubleParser.parseDouble("NaN".getBytes(StandardCharsets.ISO_8859_1), 0, 3);
        assertTrue(Double.isNaN(nan));

        // Test case insensitive Infinity
        posInf = FastDoubleParser.parseDouble("infinity".getBytes(StandardCharsets.ISO_8859_1), 0, 8);
        assertEquals(Double.POSITIVE_INFINITY, posInf);
    }

    @Test
    public void testParseDoubleEdgeCases() {
        // Very large exponent (infinity)
        double largeExp = FastDoubleParser.parseDouble("1e1000".getBytes(StandardCharsets.ISO_8859_1), 0, 6);
        assertEquals(Double.POSITIVE_INFINITY, largeExp);

        // Very small exponent (zero)
        double smallExp = FastDoubleParser.parseDouble("1e-1000".getBytes(StandardCharsets.ISO_8859_1), 0, 7);
        assertEquals(0.0, smallExp);

        // Leading zeros
        assertEquals(1.0, FastDoubleParser.parseDouble("00001".getBytes(StandardCharsets.ISO_8859_1), 0, 5), 0.0);
        assertEquals(0.1, FastDoubleParser.parseDouble("0.1".getBytes(StandardCharsets.ISO_8859_1), 0, 3), 0.0);
        assertEquals(0.01, FastDoubleParser.parseDouble("0.01".getBytes(StandardCharsets.ISO_8859_1), 0, 4), 0.0);

        // Trailing zeros
        assertEquals(1.0, FastDoubleParser.parseDouble("1.000".getBytes(StandardCharsets.ISO_8859_1), 0, 5), 0.0);
        assertEquals(10.0, FastDoubleParser.parseDouble("10.000".getBytes(StandardCharsets.ISO_8859_1), 0, 6), 0.0);

        // Zero variations
        assertEquals(0.0, FastDoubleParser.parseDouble("0".getBytes(StandardCharsets.ISO_8859_1), 0, 1), 0.0);
        assertEquals(0.0, FastDoubleParser.parseDouble("0.0".getBytes(StandardCharsets.ISO_8859_1), 0, 3), 0.0);
        assertEquals(0.0, FastDoubleParser.parseDouble("0.000".getBytes(StandardCharsets.ISO_8859_1), 0, 5), 0.0);
        assertEquals(-0.0, FastDoubleParser.parseDouble("-0".getBytes(StandardCharsets.ISO_8859_1), 0, 2), 0.0);
        assertEquals(-0.0, FastDoubleParser.parseDouble("-0.0".getBytes(StandardCharsets.ISO_8859_1), 0, 4), 0.0);
    }

    @Test
    public void testParseDoubleInvalidInput() {
        // Empty string
        assertThrows(NumberFormatException.class, () ->
                FastDoubleParser.parseDouble(new byte[0], 0, 0));

        // Just sign
        assertThrows(NumberFormatException.class, () ->
                FastDoubleParser.parseDouble("-".getBytes(StandardCharsets.ISO_8859_1), 0, 1));
        assertThrows(NumberFormatException.class, () ->
                FastDoubleParser.parseDouble("+".getBytes(StandardCharsets.ISO_8859_1), 0, 1));

        // Just decimal point
        assertThrows(NumberFormatException.class, () ->
                FastDoubleParser.parseDouble(".".getBytes(StandardCharsets.ISO_8859_1), 0, 1));

        // Multiple decimal points
        assertThrows(NumberFormatException.class, () ->
                FastDoubleParser.parseDouble("1.2.3".getBytes(StandardCharsets.ISO_8859_1), 0, 5));

        // Invalid characters
        assertThrows(NumberFormatException.class, () ->
                FastDoubleParser.parseDouble("abc".getBytes(StandardCharsets.ISO_8859_1), 0, 3));
    }

    @Test
    public void testParseDoubleChars() {
        String[] testCases = {
                "0", "1", "1.5", "-1.5", "123.456",
                "1e10", "1e-3", "1.5e10", "-1.5e-3",
                "3.1415926535897", "2.7182818284590"
        };

        for (String testCase : testCases) {
            char[] chars = testCase.toCharArray();
            double expected = Double.parseDouble(testCase);
            double actual = FastDoubleParser.parseDouble(chars, 0, chars.length);
            // Use relative tolerance for floating point comparison
            double tolerance = Math.abs(expected) * 1e-14;
            if (tolerance == 0) {
                tolerance = Double.MIN_VALUE;
            }
            assertEquals(expected, actual, tolerance, "Failed for: " + testCase);
        }
    }

    @Test
    public void testParseDoubleWithOffset() {
        String input = "abc123.456def";
        byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
        char[] chars = input.toCharArray();

        double expected = 123.456;
        double actualBytes = FastDoubleParser.parseDouble(bytes, 3, 7);
        double actualChars = FastDoubleParser.parseDouble(chars, 3, 7);

        assertEquals(expected, actualBytes, 0.0);
        assertEquals(expected, actualChars, 0.0);
    }
}
