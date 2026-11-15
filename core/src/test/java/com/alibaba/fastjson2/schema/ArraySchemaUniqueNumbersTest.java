package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ArraySchemaUniqueNumbersTest {
    private final JSONSchema schema = JSONSchema.of(JSONObject.of("type", "array", "uniqueItems", true));

    static Stream<Arguments> jsonValues() {
        return Stream.of(
                Arguments.of("[1,1.0]", false),
                Arguments.of("[0,-0.0]", false),
                Arguments.of("[1000,1e3]", false),
                Arguments.of("[1.23,1.230]", false),
                Arguments.of("[[1],[1.0]]", false),
                Arguments.of("[{\"x\":1},{\"x\":1.0}]", false),
                Arguments.of("[{\"x\":[{\"y\":1}]},{\"x\":[{\"y\":1.0}]}]", false),
                Arguments.of("[{\"a\":1,\"b\":[2]},{\"b\":[2.0],\"a\":1.0}]", false),
                Arguments.of("[1,1.0000000000000001]", true),
                Arguments.of("[[1,2],[2,1]]", true),
                Arguments.of("[{\"a\":1},{\"b\":1}]", true),
                Arguments.of("[1,true]", true),
                Arguments.of("[0,false]", true),
                Arguments.of("[null,null]", false),
                Arguments.of("[\"1\",1]", true),
                Arguments.of("[\"x\",\"x\"]", false)
        );
    }

    @ParameterizedTest
    @MethodSource("jsonValues")
    void comparesJsonValues(String input, boolean expected) {
        assertEquals(expected, schema.isValid(JSON.parseArray(input)), input);
    }

    static Stream<Arguments> javaValues() {
        return Stream.of(
                Arguments.of(new Object[]{1, 1L}, false),
                Arguments.of(new Object[]{(short) 1, 1}, false),
                Arguments.of(new Object[]{1, BigInteger.ONE}, false),
                Arguments.of(new Object[]{1D, 1F}, false),
                Arguments.of(new Object[]{new BigDecimal("1.00"), 1}, false),
                Arguments.of(new Object[]{new BigInteger("100000000000000000000"), new BigDecimal("100000000000000000000.0")}, false),
                Arguments.of(new Object[]{new int[]{1}, new long[]{1}}, false),
                Arguments.of(new long[]{1, 1}, false),
                Arguments.of(new Object[]{Double.NaN, 1}, true)
        );
    }

    @ParameterizedTest
    @MethodSource("javaValues")
    void comparesJavaValues(Object input, boolean expected) {
        assertEquals(expected, schema.isValid(input));
    }

    @Test
    void doesNotMutateInput() {
        BigDecimal original = new BigDecimal("1.00");
        JSONObject first = JSONObject.of("value", original);
        JSONObject second = JSONObject.of("value", 1);
        JSONArray values = JSONArray.of(first, second);
        assertFalse(schema.isValid(values));
        assertSame(original, first.get("value"));
        assertEquals(2, original.scale());
        assertSame(second, values.get(1));
    }
}
