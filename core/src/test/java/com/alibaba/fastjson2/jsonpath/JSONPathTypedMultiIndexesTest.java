package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTypedMultiIndexesTest {
    @Test
    public void test() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[0]", "$[1]", "$[2]", "$[3]"},
                new Type[]{Long.class, BigDecimal.class, String[].class, BigInteger.class}
        );
        Object[] result = (Object[]) jsonPath.eval(
                JSONArray.of(
                        1,
                        2,
                        JSONArray.of(3),
                        4
                )
        );

        assertEquals(1L, result[0]);
        assertEquals(new BigDecimal(2L), result[1]);
        assertArrayEquals(new String[]{"3"}, (String[]) result[2]);
        assertEquals(BigInteger.valueOf(4), result[3]);

        Object[] result1 = (Object[]) jsonPath.eval(new Object[]{1, 2, new int[]{3}, 4});
        assertEquals(1L, result1[0]);
        assertEquals(new BigDecimal(2L), result1[1]);
        assertArrayEquals(new String[]{"3"}, (String[]) result1[2]);
        assertEquals(BigInteger.valueOf(4), result1[3]);
        assertArrayEquals(new Object[4], (Object[]) jsonPath.eval(null));
        assertThrows(JSONException.class, () -> jsonPath.eval(JSONArray.of("xx")));
        assertThrows(JSONException.class, () -> jsonPath.extract("[\"xx\"]"));
    }

    @Test
    public void test1() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[0]", "$[1]", "$[2]", "$[3]"},
                new Type[]{Long.class, BigDecimal.class, String[].class, BigInteger.class},
                null,
                new long[]{JSONPath.Feature.NullOnError.mask},
                ZoneId.systemDefault()
        );

        assertNull(((Object[]) jsonPath.eval(JSONArray.of("xx")))[0]);
        assertNull(((Object[]) jsonPath.extract("[\"xx\"]"))[0]);
    }

    @Test
    public void test2() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.k0[0]", "$.k0[1]", "$.k0[2]", "$.k0[3]"},
                new Type[]{Long.class, BigDecimal.class, String[].class, BigInteger.class}
        );

        assertThrows(JSONException.class, () -> jsonPath.extract("[\"xx\"]"));
        assertThrows(JSONException.class, () -> jsonPath.extract("1"));
        assertThrows(JSONException.class, () -> jsonPath.extract("\"1\""));
        assertNull(((Object[]) jsonPath.extract("{\"k0\":null}"))[0]);
    }

    @Test
    public void test3() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.k0.k1[0]", "$.k0.k1[1]", "$.k0.k1[2]", "$.k0.k1[3]"},
                new Type[]{Long.class, BigDecimal.class, String[].class, BigInteger.class}
        );

//        assertThrows(JSONException.class, () -> jsonPath.extract("[\"xx\"]"));
//        assertThrows(JSONException.class, () -> jsonPath.extract("1"));
//        assertThrows(JSONException.class, () -> jsonPath.extract("\"1\""));
        assertNull(((Object[]) jsonPath.extract("{\"k0\":{\"k1\":null}}"))[0]);
        assertNull(((Object[]) jsonPath.extract("{\"k0\":null}"))[0]);
    }
}
