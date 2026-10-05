package com.alibaba.fastjson2.internal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CastReviewRegressionTest {
    @Test
    public void boxedNumbersPreserveLowByte() {
        Number[] values = {
                Long.MAX_VALUE, Long.MIN_VALUE, 2147483648L, 9007199254740993L,
                new BigInteger("18446744073709551617"),
                new BigDecimal("18446744073709551617.5"),
                123.5F, 123.5D
        };
        for (Number value : values) {
            assertEquals(value.byteValue(), Cast.toByteValue((Object) value));
            assertEquals(Byte.valueOf(value.byteValue()), Cast.toByte(value));
        }
    }

    @Test
    public void boxedCharacterConversionsMatchPrimitiveOverloads() {
        for (boolean value : new boolean[]{false, true}) {
            assertEquals(Cast.toCharValue(value), Cast.toCharValue((Object) value));
            assertEquals(Character.valueOf(Cast.toCharValue(value)), Cast.toCharacter(value));
        }
        for (char value : new char[]{'0', '1', 'f', 't', 'F', 'T', '\0'}) {
            assertEquals(Cast.toBooleanValue(value), Cast.toBooleanValue((Object) value));
            assertEquals(Boolean.valueOf(Cast.toBooleanValue(value)), Cast.toBoolean(value));
        }
        assertEquals('A', Cast.toCharValue((Object) (byte) 65));
    }
}
