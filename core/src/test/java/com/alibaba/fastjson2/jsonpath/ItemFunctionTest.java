package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemFunctionTest {
    @Test
    public void type() {
        assertEquals("number", JSONPath.extract("1", "$.type()"));
        assertEquals("number", JSONPath.extract("1.0", "$.type()"));
        assertEquals("string", JSONPath.extract("'1'", "$.type()"));
        assertEquals("string", JSONPath.extract("\"abc\"", "$.type()"));
        assertEquals("boolean", JSONPath.extract("true", "$.type()"));
        assertEquals("boolean", JSONPath.extract("false", "$.type()"));
        assertEquals("object", JSONPath.extract("{}", "$.type()"));
        assertEquals("array", JSONPath.extract("[]", "$.type()"));
        assertEquals("null", JSONPath.extract("null", "$.type()"));
    }

    @Test
    public void size() {
        assertEquals(0, JSONPath.extract("[]", "$.size()"));
        assertEquals(1, JSONPath.extract("[0]", "$.size()"));
        assertEquals(0, JSONPath.extract("{}", "$.size()"));
        assertEquals(1, JSONPath.extract("{\"id\":123}", "$.size()"));
    }

    @Test
    public void abs() {
        assertEquals(1, JSONPath.extract("1", "$.abs()"));
        assertEquals(1, JSONPath.extract("-1", "$.abs()"));

        assertEquals(1L, JSONPath.extract("1L", "$.abs()"));
        assertEquals(1L, JSONPath.extract("-1L", "$.abs()"));

        assertEquals(new BigDecimal("1.0"), JSONPath.extract("1.0", "$.abs()"));
        assertEquals(new BigDecimal("1.0"), JSONPath.extract("-1.0", "$.abs()"));

        assertEquals(1.0F, JSONPath.extract("1.0F", "$.abs()"));
        assertEquals(1.0F, JSONPath.extract("-1.0F", "$.abs()"));

        assertEquals(1.0D, JSONPath.extract("1.0D", "$.abs()"));
        assertEquals(1.0D, JSONPath.extract("-1.0D", "$.abs()"));

        assertEquals(BigInteger.ONE, JSONPath.eval(BigInteger.valueOf(1), "$.abs()"));
        assertEquals(BigInteger.ONE, JSONPath.eval(BigInteger.valueOf(-1), "$.abs()"));

        assertEquals(1, JSONPath.extract("{'id':1}", "$.id.abs()"));
        assertEquals(1, JSONPath.extract("{'id':1}", "$.id.abs()"));
    }

    @Test
    public void test_double() {
        assertEquals(1D, JSONPath.extract("1", "$.double()"));
        assertEquals(JSONArray.of(1D), JSONPath.extract("[1]", "$.double()"));
        assertEquals(JSONArray.of(1D, 2D, 3D, 4D, 5D, null), JSONPath.extract("[1.0, 2, 3F, 4D, \"5\", \"\"]", "$.double()"));

        assertEquals(1D, JSONPath.extract("{'id':1}", "$.id.double()"));
        assertEquals(1D, JSONPath.extract("{'id':1}", "$.id.double()"));
    }

    @Test
    public void floor() {
        assertEquals(1, JSONPath.extract("1", "$.floor()"));
        assertEquals(new BigDecimal("1"), JSONPath.extract("1.1", "$.floor()"));
        assertEquals(1D, JSONPath.extract("1.1F", "$.floor()"));
        assertEquals(1D, JSONPath.extract("1.1D", "$.floor()"));
    }
}
