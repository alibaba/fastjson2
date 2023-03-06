package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;

import static com.alibaba.fastjson2.JSONPath.Feature.NullOnError;
import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTypedMultiNamesTest {
    @Test
    public void test() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.f0", "$.f1", "$.f2", "$.f3"},
                new Type[]{Long.class, BigDecimal.class, String[].class, BigInteger.class}
        );
        Object[] result = (Object[]) jsonPath.eval(
                JSONObject.of(
                        "f0", 1,
                        "f1", 2,
                        "f2", JSONArray.of(3),
                        "f3", 4
                )
        );

        assertEquals(1L, result[0]);
        assertEquals(new BigDecimal(2L), result[1]);
        assertArrayEquals(new String[]{"3"}, (String[]) result[2]);
        assertEquals(BigInteger.valueOf(4), result[3]);

        Object[] result1 = (Object[]) jsonPath.eval(new Bean(1, 2, new int[]{3}, 4));
        assertEquals(1L, result1[0]);
        assertEquals(new BigDecimal(2L), result1[1]);
        assertArrayEquals(new String[]{"3"}, (String[]) result1[2]);
        assertEquals(BigInteger.valueOf(4), result1[3]);

        assertNull(((Object[]) jsonPath.eval(null))[0]);
    }

    @Test
    public void test1() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.x0", "$.x1", "$.x2", "$.x3"},
                new Type[]{Long.class, BigDecimal.class, String[].class, BigInteger.class},
                null,
                new long[]{
                        NullOnError.mask,
                        NullOnError.mask,
                        NullOnError.mask,
                        NullOnError.mask
                },
                ZoneId.systemDefault()
        );

        Bean bean = new Bean(1, 2, new int[]{3}, 4);

        String jsonStr = JSON.toJSONString(bean);
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertNull(result[0]);
        assertNull(result[1]);
        assertNull(result[2]);
        assertNull(result[3]);

        Object[] result1 = (Object[]) jsonPath.eval(bean);
        assertNull(result1[0]);
        assertNull(result1[1]);
        assertNull(result1[2]);
        assertNull(result1[3]);

        Object[] result2 = (Object[]) jsonPath.extract("{\"x0\":\"xx\",\"x1\":\"xx\",\"x2\":\"xx\",\"x3\":\"xx\"}");
        assertNull(result2[0]);
        assertNull(result2[1]);
        assertNull(result2[2]);
        assertNull(result2[3]);

        Object[] result3 = (Object[]) jsonPath.extract("null");
        assertNull(result3[0]);
        assertNull(result3[1]);
        assertNull(result3[2]);
        assertNull(result3[3]);

        assertThrows(JSONException.class, () -> jsonPath.extract("["));
    }

    static class Bean {
        int f0;
        int f1;
        int[] f2;
        int f3;

        public Bean(int f0, int f1, int[] f2, int f3) {
            this.f0 = f0;
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
        }

        public int getF0() {
            return f0;
        }

        public int getF1() {
            return f1;
        }

        public int[] getF2() {
            return f2;
        }

        public int getF3() {
            return f3;
        }
    }
}
