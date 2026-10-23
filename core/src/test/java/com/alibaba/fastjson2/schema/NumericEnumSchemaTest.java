package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Numeric enum equality: members and instances must be compared by mathematical
 * numeric value, matching the existing BigDecimal/Long normalization contract of
 * {@link EnumSchema}.
 * <p>
 * 数值 enum 相等性：成员与实例必须按数学数值比较，沿用 EnumSchema 已有的
 * BigDecimal/Long 归一化约定。
 */
@Tag("schema")
public class NumericEnumSchemaTest {
    static JSONSchema schemaOf(String text) {
        return JSONSchema.of(JSON.parseObject(text));
    }

    static boolean isValid(JSONSchema schema, Object value) {
        return schema.validate(value).isSuccess();
    }

    @Test
    public void testIntegralMembersAcceptEqualNumbers() {
        JSONSchema schema = schemaOf("{\"enum\":[1,2]}");
        assertTrue(schema.isValid(2));
        assertTrue(schema.isValid(2L));
        assertTrue(schema.isValid(2.0d));
        assertTrue(schema.isValid(2.0f));
        assertTrue(schema.isValid((Object) Double.valueOf(2.0)));
        assertTrue(schema.isValid((Object) Float.valueOf(2.0f)));
        assertTrue(isValid(schema, new BigDecimal("2.0")));
        assertTrue(isValid(schema, new BigDecimal("2.000")));
        assertTrue(isValid(schema, new BigDecimal("2E+0")));
        assertTrue(isValid(schema, BigInteger.valueOf(2)));

        assertFalse(schema.isValid(3));
        assertFalse(schema.isValid(3.0d));
        assertFalse(isValid(schema, new BigDecimal("2.5")));
    }

    @Test
    public void testNonIntegralMembersAcceptEqualNumbers() {
        JSONSchema schema = schemaOf("{\"enum\":[1.5]}");
        assertTrue(schema.isValid(1.5d));
        assertTrue(schema.isValid(1.5f));
        assertTrue(schema.isValid((Object) Double.valueOf(1.5)));
        assertTrue(isValid(schema, new BigDecimal("1.5")));
        assertTrue(isValid(schema, new BigDecimal("1.50")));
        assertTrue(isValid(schema, new BigDecimal("15E-1")));

        assertFalse(schema.isValid(1.6d));
        assertFalse(schema.isValid(1));
        assertFalse(isValid(schema, new BigDecimal("1.5000000001")));
    }

    @Test
    public void testFractionalPrecisionPreserved() {
        JSONSchema schema = schemaOf("{\"enum\":[0.1]}");
        assertTrue(schema.isValid(0.1d));
        // 0.1f widens to 0.10000000149011612, so it must not be reported as 0.1
        assertFalse(schema.isValid(0.1f));
        assertFalse(isValid(schema, Float.valueOf(0.1f)));
        assertFalse(isValid(schema, Float.valueOf(1.5f)));
        assertTrue(isValid(schema, new BigDecimal("0.1")));
        assertTrue(isValid(schema, new BigDecimal("0.10")));
        assertFalse(schema.isValid(0.2d));
        assertFalse(isValid(schema, new BigDecimal("0.10000000000000001")));
    }

    @Test
    public void testNegativeScaleMembers() {
        JSONSchema decimalStyle = schemaOf("{\"enum\":[100.0]}");
        assertTrue(decimalStyle.isValid(100));
        assertTrue(decimalStyle.isValid(100L));
        assertTrue(decimalStyle.isValid(100.0d));
        assertTrue(decimalStyle.isValid(100.0f));
        assertTrue(isValid(decimalStyle, new BigDecimal("1E2")));
        assertTrue(isValid(decimalStyle, new BigDecimal("100.00")));
        assertTrue(isValid(decimalStyle, BigInteger.valueOf(100)));
        assertFalse(decimalStyle.isValid(101));
        assertFalse(decimalStyle.isValid(100.5d));

        JSONSchema exponentStyle = schemaOf("{\"enum\":[1E2]}");
        assertTrue(exponentStyle.isValid(100));
        assertTrue(exponentStyle.isValid(100L));
        assertTrue(exponentStyle.isValid(100.0d));
        assertTrue(isValid(exponentStyle, new BigDecimal("100.0")));
        assertFalse(exponentStyle.isValid(101));
    }

    @Test
    public void testConstructorAndValidateShareCanonicalForm() {
        EnumSchema fromDouble = new EnumSchema(2.0d);
        assertTrue(fromDouble.isValid(2));
        assertTrue(fromDouble.isValid(2L));
        assertTrue(fromDouble.isValid(2.0f));
        assertTrue(fromDouble.isValid((Object) BigInteger.valueOf(2)));

        EnumSchema fromFloat = new EnumSchema(1.5f);
        assertTrue(fromFloat.isValid(1.5d));
        assertTrue(fromFloat.isValid((Object) new BigDecimal("1.5")));

        EnumSchema fromDecimal = new EnumSchema(new BigDecimal("1E2"));
        assertTrue(fromDecimal.isValid(100));
        assertTrue(fromDecimal.isValid(100.0d));

        EnumSchema fromLong = new EnumSchema(100L);
        assertTrue(fromLong.isValid(100));
        assertTrue(fromLong.isValid(100.0d));
        assertTrue(fromLong.isValid((Object) new BigDecimal("1E2")));
    }

    @Test
    public void testNegativeZero() {
        JSONSchema zero = schemaOf("{\"enum\":[0]}");
        assertTrue(zero.isValid(0));
        assertTrue(zero.isValid(0.0d));
        assertTrue(zero.isValid(-0.0d));
        assertTrue(zero.isValid(-0.0f));
        assertTrue(isValid(zero, new BigDecimal("-0.00")));
        assertTrue(isValid(zero, new BigDecimal("0E-10")));

        JSONSchema negativeZero = schemaOf("{\"enum\":[-0.0]}");
        assertTrue(negativeZero.isValid(0));
        assertTrue(negativeZero.isValid(-0.0d));
        assertTrue(negativeZero.isValid(0.0d));
    }

    @Test
    public void testLargeMagnitudes() {
        JSONSchema bigIntegral = schemaOf("{\"enum\":[10000000000000000000]}");
        assertTrue(isValid(bigIntegral, new BigInteger("10000000000000000000")));
        assertTrue(isValid(bigIntegral, new BigDecimal("10000000000000000000")));
        assertTrue(bigIntegral.isValid(1.0E19d));
        assertFalse(isValid(bigIntegral, new BigInteger("10000000000000000001")));

        JSONSchema longIntegral = schemaOf("{\"enum\":[9007199254740993]}");
        assertTrue(longIntegral.isValid(9007199254740993L));
        assertTrue(isValid(longIntegral, new BigInteger("9007199254740993")));
        assertTrue(isValid(longIntegral, new BigDecimal("9007199254740993")));
        assertFalse(longIntegral.isValid(9007199254740992L));

        JSONSchema bigDecimal = schemaOf("{\"enum\":[12345678901234567890.5]}");
        assertTrue(isValid(bigDecimal, new BigDecimal("12345678901234567890.5")));
        assertTrue(isValid(bigDecimal, new BigDecimal("12345678901234567890.50")));
        assertFalse(isValid(bigDecimal, new BigDecimal("12345678901234567890.6")));

        JSONSchema hugeInteger = schemaOf("{\"enum\":[214748364921474836492147483649]}");
        assertTrue(isValid(hugeInteger, new BigInteger("214748364921474836492147483649")));
        assertTrue(isValid(hugeInteger, new BigDecimal("214748364921474836492147483649")));
        assertFalse(isValid(hugeInteger, new BigInteger("214748364921474836492147483650")));
    }

    @Test
    public void testBigIntegerBeyondLongMatchesCompactDecimal() {
        BigInteger tenPow30 = BigInteger.TEN.pow(30);
        EnumSchema fromBigInteger = new EnumSchema(tenPow30);
        assertTrue(fromBigInteger.isValid(tenPow30));
        assertTrue(fromBigInteger.isValid(new BigDecimal("1E+30")));
        assertTrue(fromBigInteger.isValid(new BigDecimal("10E+29")));
        assertTrue(fromBigInteger.isValid(new BigDecimal("1000000000000000000000000000000")));
        assertFalse(fromBigInteger.isValid(BigInteger.TEN.pow(31)));
        assertFalse(fromBigInteger.isValid(new BigDecimal("1E+31")));

        EnumSchema fromDecimal = new EnumSchema(new BigDecimal("1E+30"));
        assertTrue(fromDecimal.isValid(tenPow30));
        assertTrue(fromDecimal.isValid(tenPow30.add(BigInteger.ZERO)));
        assertFalse(fromDecimal.isValid(tenPow30.add(BigInteger.ONE)));

        BigInteger trailingZeros = new BigInteger("123000000000000000000000000000000");
        EnumSchema fromTrailingZeros = new EnumSchema(trailingZeros);
        assertTrue(fromTrailingZeros.isValid(new BigDecimal("1.23E+32")));
        assertFalse(fromTrailingZeros.isValid(new BigDecimal("1.24E+32")));
        assertTrue(new EnumSchema(new BigDecimal("1.23E+32")).isValid(trailingZeros));
    }

    @Test
    public void testDoubleExactBoundary() {
        EnumSchema aboveExact = new EnumSchema(9007199254740993L); // 2^53 + 1
        assertTrue(aboveExact.isValid(9007199254740993L));
        assertTrue(aboveExact.isValid(new BigDecimal("9007199254740993")));
        assertFalse(aboveExact.isValid(9007199254740992L));
        assertFalse(aboveExact.isValid((double) 9007199254740993L));

        EnumSchema exact = new EnumSchema(9007199254740992L); // 2^53
        assertTrue(exact.isValid(9007199254740992.0d));
        assertTrue(exact.isValid(new BigDecimal("9.007199254740992E15")));
        assertFalse(exact.isValid(9007199254740993L));
    }

    @Test
    public void testHugeExponentStaysCompact() {
        // 1E+1000000000 is unscaled=1, scale=-1e9: canonical form must stay that compact on both
        // the member and the instance side, so no 10^|scale| expansion happens at all.
        EnumSchema schema = new EnumSchema(new BigDecimal("1E+1000000000"));
        assertDoesNotThrow(() -> assertTrue(schema.isValid(new BigDecimal("1E+1000000000"))));
        assertTrue(schema.isValid(new BigDecimal("10E+999999999")));
        assertFalse(schema.isValid(new BigDecimal("2E+1000000000")));
        assertFalse(schema.isValid(new BigDecimal("1E+999999999")));
        assertFalse(schema.isValid(1));
        assertFalse(schema.isValid(1.0d));
    }

    @Test
    public void testHugeInstanceRejectedAgainstFiniteEnum() {
        JSONSchema schema = schemaOf("{\"enum\":[1,2,3]}");
        assertDoesNotThrow(() -> assertFalse(schema.isValid(new BigDecimal("1E+1000000000"))));
        assertDoesNotThrow(() -> assertFalse(schema.isValid(new BigDecimal("-1E+1000000000"))));
        assertFalse(schema.isValid(new BigDecimal("1E+2147483647")));
        assertTrue(schema.isValid(new BigDecimal("2.000")));
    }

    @Test
    public void testNonFiniteIsRejectedWithoutException() {
        JSONSchema schema = schemaOf("{\"enum\":[1,2]}");
        assertDoesNotThrow(() -> assertFalse(schema.isValid(Double.NaN)));
        assertFalse(schema.isValid(Double.POSITIVE_INFINITY));
        assertFalse(schema.isValid(Double.NEGATIVE_INFINITY));
        assertFalse(schema.isValid(Float.NaN));
        assertFalse(schema.isValid(Float.POSITIVE_INFINITY));
        assertTrue(isValid(schema, new BigDecimal("1.0")));
        assertFalse(isValid(schema, new BigDecimal("1.0000000000000001")));
    }

    @Test
    public void testNonFiniteMembersDoNotMatchFiniteNumbers() {
        EnumSchema schema = new EnumSchema(Double.NaN, Double.POSITIVE_INFINITY);
        assertDoesNotThrow(() -> assertFalse(schema.isValid(1)));
        assertFalse(schema.isValid(1.0d));
        assertFalse(schema.isValid(0.0d));
    }

    @Test
    public void testNumericStringIsNotANumber() {
        JSONSchema numbers = schemaOf("{\"enum\":[1,2]}");
        assertFalse(numbers.isValid("1"));
        assertFalse(numbers.isValid("2.0"));
        assertFalse(numbers.isValid("2"));

        JSONSchema mixed = schemaOf("{\"enum\":[\"1\",1]}");
        assertTrue(mixed.isValid("1"));
        assertTrue(mixed.isValid(1));
        assertTrue(mixed.isValid(1.0d));
        assertFalse(mixed.isValid("1.0"));
        assertFalse(mixed.isValid(2));

        JSONSchema strings = schemaOf("{\"enum\":[\"1\",\"2\"]}");
        assertEquals(JSONSchema.Type.String, strings.getType());
        assertTrue(strings.isValid("1"));
        assertTrue(strings.isValid("2"));
    }

    @Test
    public void testNonNumericMembersUnchanged() {
        JSONSchema schema = schemaOf("{\"enum\":[true,\"a\"]}");
        assertTrue(schema.isValid(true));
        assertTrue(schema.isValid("a"));
        assertFalse(schema.isValid(false));
        assertFalse(schema.isValid(1));
        assertFalse(schema.isValid((Object) JSONObject.of("a", 1)));
    }

    @Test
    public void testShortMembersAndValues() {
        JSONSchema schema = schemaOf("{\"enum\":[1,2]}");
        assertTrue(schema.isValid((Object) Short.valueOf((short) 2)));
        assertFalse(schema.isValid((Object) Short.valueOf((short) 3)));
        assertTrue(new EnumSchema((short) 2).isValid(2));
    }

    @Test
    public void testByteMembersAndValues() {
        JSONSchema schema = schemaOf("{\"enum\":[1,2]}");
        assertTrue(schema.isValid((Object) Byte.valueOf((byte) 2)));
        assertFalse(schema.isValid((Object) Byte.valueOf((byte) 3)));
        assertTrue(new EnumSchema((byte) 2).isValid(2));
    }

    @Test
    public void testAtomicIntegerMembersAndValues() {
        JSONSchema schema = schemaOf("{\"enum\":[1,2]}");
        assertTrue(schema.isValid((Object) new AtomicInteger(2)));
        assertFalse(schema.isValid((Object) new AtomicInteger(3)));
        assertTrue(new EnumSchema(new AtomicInteger(2)).isValid(2));
    }

    @Test
    public void testAtomicLongMembersAndValues() {
        JSONSchema schema = schemaOf("{\"enum\":[1,2]}");
        assertTrue(schema.isValid((Object) new AtomicLong(2)));
        assertFalse(schema.isValid((Object) new AtomicLong(3)));
        assertTrue(new EnumSchema(new AtomicLong(2)).isValid(2));
        long aboveInt = (long) Integer.MAX_VALUE + 1;
        assertTrue(new EnumSchema(aboveInt).isValid(new AtomicLong(aboveInt)));
        assertTrue(new EnumSchema(new AtomicLong(aboveInt)).isValid(aboveInt));
        assertFalse(new EnumSchema(aboveInt).isValid(new AtomicLong(aboveInt + 1)));
    }

    @Test
    public void testLongMaximumMatchesDecimal() {
        assertTrue(new EnumSchema(Long.MAX_VALUE).isValid(new BigDecimal("9223372036854775807")));
    }

    @Test
    public void testLongMinimumMatchesDecimal() {
        assertTrue(new EnumSchema(Long.MIN_VALUE).isValid(new BigDecimal("-9223372036854775808")));
    }

    @Test
    public void testBeyondLongDoesNotMatchBoundary() {
        assertFalse(new EnumSchema(Long.MAX_VALUE).isValid(new BigInteger("9223372036854775808")));
        assertFalse(new EnumSchema(Long.MIN_VALUE).isValid(new BigInteger("-9223372036854775809")));
    }

    @Test
    public void testIntegerMaximumMatchesDecimal() {
        assertTrue(new EnumSchema(Integer.MAX_VALUE).isValid(new BigDecimal("2147483647")));
        assertEquals(Integer.MAX_VALUE, EnumSchema.canonicalize(new BigDecimal("2147483647")));
    }

    @Test
    public void testIntegerMinimumMatchesDecimal() {
        assertTrue(new EnumSchema(Integer.MIN_VALUE).isValid(new BigDecimal("-2147483648")));
        assertEquals(Integer.MIN_VALUE, EnumSchema.canonicalize(new BigDecimal("-2147483648")));
    }

    @Test
    public void testDoubleAtLongLimitDoesNotClamp() {
        assertFalse(new EnumSchema(Long.MAX_VALUE).isValid(0x1p63));
        assertFalse(new EnumSchema(Long.MIN_VALUE).isValid(-0x1p63));
    }

    @Test
    public void testLargeIntegralDoubleKeepsDecimalRoundTrip() {
        double positive = 1.00000000000000013E18;
        double negative = -positive;
        assertTrue(new EnumSchema(BigDecimal.valueOf(positive)).isValid(positive));
        assertTrue(new EnumSchema(BigDecimal.valueOf(negative)).isValid(negative));
        assertFalse(new EnumSchema((long) positive).isValid(positive));
        assertFalse(new EnumSchema((long) negative).isValid(negative));
    }
}
