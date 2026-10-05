package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeUtilsReviewRegressionTest {
    @Test
    public void ipValidatorsRequireCompleteAddresses() {
        assertFalse(TypeUtils.validateIPv4("1.1.11."));
        assertTrue(TypeUtils.validateIPv4("192.168.0.1"));
        for (String value : new String[]{"1:2", ":::1", "1::2::3", "1:2:3:4:5:6:7:",
                ":1:2:3:4:5:6:7", "1:2:3:4:5:6:7:8::", "::ffff:1.1.11."}) {
            assertFalse(TypeUtils.validateIPv6(value), value);
        }
        for (String value : new String[]{"::", "::1", "1::", "1:2:3:4:5:6:7:8", "::ffff:192.168.0.1",
                "ffff:ffff:ffff:ffff:ffff:ffff:255.255.255.255"}) {
            assertTrue(TypeUtils.validateIPv6(value), value);
        }
    }

    @Test
    public void numberPredicatesRequireMantissaAndExponentDigits() {
        for (String value : new String[]{"1e", "1e+", "1e-", ".e1", "-.", "+.", ".", "1x"}) {
            assertFalse(TypeUtils.isNumber(value), value);
            assertFalse(TypeUtils.isNumber(value.toCharArray(), 0, value.length()), value);
            assertFalse(TypeUtils.isNumber(value.getBytes(StandardCharsets.US_ASCII), 0, value.length()), value);
        }
        for (String value : new String[]{"-.5", "+.5", "1.", ".5", "-1.2e+3", "0", "1E-9"}) {
            assertTrue(TypeUtils.isNumber(value), value);
            String padded = "!" + value + "!";
            assertTrue(TypeUtils.isNumber(padded.toCharArray(), 1, value.length()), value);
            assertTrue(TypeUtils.isNumber(padded.getBytes(StandardCharsets.US_ASCII), 1, value.length()), value);
        }
    }

    @Test
    public void shortConversionRetainsAllSixteenBits() {
        for (Number value : new Number[]{256, 32767L, -32768L, new BigDecimal("1234")}) {
            assertEquals(value.shortValue(), TypeUtils.toShortValue(value));
        }
    }

    @Test
    public void decimalParserRequiresAtLeastOneDigit() {
        for (String value : new String[]{"-", ".", "-."}) {
            byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
            char[] chars = value.toCharArray();
            assertThrows(NumberFormatException.class, () -> TypeUtils.parseBigDecimal(bytes, 0, bytes.length));
            assertThrows(NumberFormatException.class, () -> TypeUtils.parseBigDecimal(chars, 0, chars.length));
        }
    }

    @Test
    public void integerDecimalIncludesNegativeScaleAndInflatedValues() {
        assertTrue(TypeUtils.isInteger(new BigDecimal("1E+3")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("10.0000000000")));
        assertTrue(TypeUtils.isInteger(new BigDecimal("999999999999999999.0")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("999999999999999999.1")));
        assertFalse(TypeUtils.isInteger(new BigDecimal("1.0000000001")));
    }

    @Test
    public void floatingParsersRejectMalformedNumbers() {
        for (String value : new String[]{"1x", "0x", "1e", "1e+", "1e-", "1e2x", "1!"}) {
            byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
            char[] chars = value.toCharArray();
            assertThrows(NumberFormatException.class, () -> TypeUtils.parseDouble(bytes, 0, bytes.length), value);
            assertThrows(NumberFormatException.class, () -> TypeUtils.parseDouble(chars, 0, chars.length), value);
            assertThrows(NumberFormatException.class, () -> TypeUtils.parseFloat(bytes, 0, bytes.length), value);
            assertThrows(NumberFormatException.class, () -> TypeUtils.parseFloat(chars, 0, chars.length), value);
        }
    }

    @Test
    public void floatingParsersRespectSliceAndSupportedSuffixes() {
        byte[] bytes = "!1e+9".getBytes(StandardCharsets.US_ASCII);
        char[] chars = "!1e+9".toCharArray();
        assertThrows(NumberFormatException.class, () -> TypeUtils.parseDouble(bytes, 1, 2));
        assertThrows(NumberFormatException.class, () -> TypeUtils.parseDouble(chars, 1, 2));
        assertThrows(NumberFormatException.class, () -> TypeUtils.parseFloat(bytes, 1, 2));
        assertThrows(NumberFormatException.class, () -> TypeUtils.parseFloat(chars, 1, 2));
        for (String value : new String[]{"1.25", "1.25f", "1.25F", "1.25d", "1.25D"}) {
            byte[] input = value.getBytes(StandardCharsets.US_ASCII);
            char[] inputChars = value.toCharArray();
            assertEquals(1.25, TypeUtils.parseDouble(input, 0, input.length));
            assertEquals(1.25, TypeUtils.parseDouble(inputChars, 0, inputChars.length));
            assertEquals(1.25F, TypeUtils.parseFloat(input, 0, input.length));
            assertEquals(1.25F, TypeUtils.parseFloat(inputChars, 0, inputChars.length));
        }
    }
}
