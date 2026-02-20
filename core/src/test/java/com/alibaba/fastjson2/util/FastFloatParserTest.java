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
 * Tests for FastFloatParser.
 */
public class FastFloatParserTest {
    @Test
    public void testParseFloatBasic() {
        String[] testCases = {
                "0",
                "0.0",
                "1",
                "1.0",
                "1.5",
                "-1",
                "-1.5",
                "123",
                "-123",
                "1e5",
                "1.5e5",
                "1e-3",
                "1.5e-3",
                "-1.5e5",
                "-1.5e-3",
                "3.4028235e38",  // Float.MAX_VALUE
                "0.1",
                "0.01",
                "0.001",
                "0.0001",
                "0.00001",
                "3.141592",
                "2.718281",
                "1.414213",
        };

        for (String testCase : testCases) {
            byte[] bytes = testCase.getBytes(StandardCharsets.ISO_8859_1);
            char[] chars = testCase.toCharArray();

            float expected = Float.parseFloat(testCase);
            float actualBytes = FastFloatParser.parseFloat(bytes, 0, bytes.length);
            float actualChars = FastFloatParser.parseFloat(chars, 0, chars.length);

            // Use relative tolerance for floating point comparison
            float tolerance = Math.abs(expected) * 1e-6f;
            if (tolerance == 0) {
                tolerance = Float.MIN_VALUE;
            }

            assertEquals(expected, actualBytes, tolerance, "Failed for: " + testCase + " (bytes)");
            assertEquals(expected, actualChars, tolerance, "Failed for: " + testCase + " (chars)");
        }
    }

    @Test
    public void testParseFloatScientificNotation() {
        String[] testCases = {
                "1e0",
                "1e1",
                "1e2",
                "1e10",
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
        };

        for (String testCase : testCases) {
            byte[] bytes = testCase.getBytes(StandardCharsets.ISO_8859_1);
            char[] chars = testCase.toCharArray();

            float expected = Float.parseFloat(testCase);
            float actualBytes = FastFloatParser.parseFloat(bytes, 0, bytes.length);
            float actualChars = FastFloatParser.parseFloat(chars, 0, chars.length);

            // Use relative tolerance for floating point comparison
            float tolerance = Math.abs(expected) * 1e-6f;
            if (tolerance == 0) {
                tolerance = Float.MIN_VALUE;
            }

            assertEquals(expected, actualBytes, tolerance, "Failed for: " + testCase + " (bytes)");
            assertEquals(expected, actualChars, tolerance, "Failed for: " + testCase + " (chars)");
        }
    }

    @Test
    public void testParseFloatSpecialValues() {
        // Test positive infinity
        float posInf = FastFloatParser.parseFloat("Infinity".getBytes(StandardCharsets.ISO_8859_1), 0, 8);
        assertEquals(Float.POSITIVE_INFINITY, posInf);

        // Test negative infinity
        float negInf = FastFloatParser.parseFloat("-Infinity".getBytes(StandardCharsets.ISO_8859_1), 0, 9);
        assertEquals(Float.NEGATIVE_INFINITY, negInf);

        // Test NaN
        float nan = FastFloatParser.parseFloat("NaN".getBytes(StandardCharsets.ISO_8859_1), 0, 3);
        assertTrue(Float.isNaN(nan));

        // Test case insensitive Infinity
        posInf = FastFloatParser.parseFloat("infinity".getBytes(StandardCharsets.ISO_8859_1), 0, 8);
        assertEquals(Float.POSITIVE_INFINITY, posInf);
    }

    @Test
    public void testParseFloatEdgeCases() {
        // Very large exponent (infinity)
        float largeExp = FastFloatParser.parseFloat("1e100".getBytes(StandardCharsets.ISO_8859_1), 0, 6);
        assertEquals(Float.POSITIVE_INFINITY, largeExp);

        // Very small exponent (zero)
        float smallExp = FastFloatParser.parseFloat("1e-100".getBytes(StandardCharsets.ISO_8859_1), 0, 7);
        assertEquals(0.0f, smallExp);

        // Leading zeros
        assertEquals(1.0f, FastFloatParser.parseFloat("00001".getBytes(StandardCharsets.ISO_8859_1), 0, 5), 0.0f);
        assertEquals(0.1f, FastFloatParser.parseFloat("0.1".getBytes(StandardCharsets.ISO_8859_1), 0, 3), 0.0f);
        assertEquals(0.01f, FastFloatParser.parseFloat("0.01".getBytes(StandardCharsets.ISO_8859_1), 0, 4), 0.0f);

        // Trailing zeros
        assertEquals(1.0f, FastFloatParser.parseFloat("1.000".getBytes(StandardCharsets.ISO_8859_1), 0, 5), 0.0f);
        assertEquals(10.0f, FastFloatParser.parseFloat("10.000".getBytes(StandardCharsets.ISO_8859_1), 0, 6), 0.0f);

        // Zero variations
        assertEquals(0.0f, FastFloatParser.parseFloat("0".getBytes(StandardCharsets.ISO_8859_1), 0, 1), 0.0f);
        assertEquals(0.0f, FastFloatParser.parseFloat("0.0".getBytes(StandardCharsets.ISO_8859_1), 0, 3), 0.0f);
        assertEquals(0.0f, FastFloatParser.parseFloat("0.000".getBytes(StandardCharsets.ISO_8859_1), 0, 5), 0.0f);
        assertEquals(-0.0f, FastFloatParser.parseFloat("-0".getBytes(StandardCharsets.ISO_8859_1), 0, 2), 0.0f);
        assertEquals(-0.0f, FastFloatParser.parseFloat("-0.0".getBytes(StandardCharsets.ISO_8859_1), 0, 4), 0.0f);
    }

    @Test
    public void testParseFloatInvalidInput() {
        // Empty string
        assertThrows(NumberFormatException.class, () ->
                FastFloatParser.parseFloat(new byte[0], 0, 0));

        // Just sign
        assertThrows(NumberFormatException.class, () ->
                FastFloatParser.parseFloat("-".getBytes(StandardCharsets.ISO_8859_1), 0, 1));
        assertThrows(NumberFormatException.class, () ->
                FastFloatParser.parseFloat("+".getBytes(StandardCharsets.ISO_8859_1), 0, 1));

        // Just decimal point
        assertThrows(NumberFormatException.class, () ->
                FastFloatParser.parseFloat(".".getBytes(StandardCharsets.ISO_8859_1), 0, 1));

        // Multiple decimal points
        assertThrows(NumberFormatException.class, () ->
                FastFloatParser.parseFloat("1.2.3".getBytes(StandardCharsets.ISO_8859_1), 0, 5));

        // Invalid characters
        assertThrows(NumberFormatException.class, () ->
                FastFloatParser.parseFloat("abc".getBytes(StandardCharsets.ISO_8859_1), 0, 3));
    }

    @Test
    public void testParseFloatChars() {
        String[] testCases = {
                "0", "1", "1.5", "-1.5",
                "1e5", "1e-3", "1.5e5", "-1.5e-3",
                "3.141592", "2.718281"
        };

        for (String testCase : testCases) {
            char[] chars = testCase.toCharArray();
            float expected = Float.parseFloat(testCase);
            float actual = FastFloatParser.parseFloat(chars, 0, chars.length);
            // Use relative tolerance for floating point comparison
            float tolerance = Math.abs(expected) * 1e-6f;
            if (tolerance == 0) {
                tolerance = Float.MIN_VALUE;
            }
            assertEquals(expected, actual, tolerance, "Failed for: " + testCase);
        }
    }

    @Test
    public void testParseFloatWithOffset() {
        String input = "abc123def";
        byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
        char[] chars = input.toCharArray();

        float expected = 123.0f;
        float actualBytes = FastFloatParser.parseFloat(bytes, 3, 3);
        float actualChars = FastFloatParser.parseFloat(chars, 3, 3);

        assertEquals(expected, actualBytes, 0.0f);
        assertEquals(expected, actualChars, 0.0f);
    }
}
