package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerMinValueTypeTest {
    @Test
    public void testDefaultScalarTypes() {
        assertScalarType("-2147483649", Long.class);
        assertScalarType("-2147483648", Integer.class);
        assertScalarType("2147483647", Integer.class);
        assertScalarType("2147483648", Long.class);

        assertScalarType("-9223372036854775809", BigInteger.class);
        assertScalarType("-9223372036854775808", Long.class);
        assertScalarType("9223372036854775807", Long.class);
        assertScalarType("9223372036854775808", BigInteger.class);
    }

    @Test
    public void testNestedTypes() {
        JSONArray array = JSON.parseArray("[-2147483648,-9223372036854775808]");
        assertEquals(Integer.class, array.get(0).getClass());
        assertEquals(Long.class, array.get(1).getClass());

        JSONObject object = JSON.parseObject(
                "{\"intMin\":-2147483648,\"longMin\":-9223372036854775808}"
        );
        assertEquals(Integer.class, object.get("intMin").getClass());
        assertEquals(Long.class, object.get("longMin").getClass());
    }

    @Test
    public void testFeaturesAndSuffix() {
        Object intAsLong = JSON.parse("-2147483648", JSONReader.Feature.UseLongForInts);
        assertEquals(Long.class, intAsLong.getClass());
        assertEquals(Long.valueOf(Integer.MIN_VALUE), intAsLong);

        Object intAsBigInteger = JSON.parse("-2147483648", JSONReader.Feature.UseBigIntegerForInts);
        assertEquals(BigInteger.class, intAsBigInteger.getClass());
        assertEquals(BigInteger.valueOf(Integer.MIN_VALUE), intAsBigInteger);

        Object longAsBigInteger = JSON.parse("-9223372036854775808", JSONReader.Feature.UseBigIntegerForInts);
        assertEquals(BigInteger.class, longAsBigInteger.getClass());
        assertEquals(BigInteger.valueOf(Long.MIN_VALUE), longAsBigInteger);

        Object intWithLongSuffix = JSON.parse("-2147483648L");
        assertEquals(Long.class, intWithLongSuffix.getClass());
        assertEquals(Long.valueOf(Integer.MIN_VALUE), intWithLongSuffix);
    }

    private static void assertScalarType(String json, Class<?> expectedClass) {
        Object[] values = {
                JSON.parse(json),
                JSON.parse(json.getBytes(StandardCharsets.UTF_8)),
                JSON.parse(json.toCharArray()),
                JSON.parseObject(json, Object.class)
        };
        for (Object value : values) {
            assertEquals(expectedClass, value.getClass(), json);
        }
    }
}
