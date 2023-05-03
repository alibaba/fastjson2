package com.alibaba.fastjson2.jsonpath.function;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexTest {
    @Test
    public void indexInt() {
        assertEquals(1, JSONPath.extract("[1,2,3,4,5]", "$.index(2)"));
        assertEquals(1, JSONPath.extract("[1.0,2.0,3.0,4.0,5.0]", "$.index(2)"));
        assertEquals(1, JSONPath.eval(JSONArray.of(1F, 2F, 3F, 4F, 5F), "$.index(2)"));
        assertEquals(1, JSONPath.eval(JSONArray.of(1D, 2D, 3D, 4D, 5D), "$.index(2)"));
        assertEquals(1, JSONPath.eval(new int[]{1, 2, 3, 4, 5}, "$.index(2)"));
        assertEquals(1, JSONPath.eval(new long[]{1, 2, 3, 4, 5}, "$.index(2)"));
        assertEquals(1, JSONPath.eval(new float[]{1, 2, 3, 4, 5}, "$.index(2)"));
        assertEquals(1, JSONPath.eval(new double[]{1, 2, 3, 4, 5}, "$.index(2)"));
        assertEquals(1, JSONPath.eval(new Object[]{1, 2, 3, 4, 5}, "$.index(2)"));
        assertEquals(1, JSONPath.eval(new Object[]{1L, 2L, 3L, 4L, 5L}, "$.index(2)"));
        assertEquals(1, JSONPath.eval(new Object[]{1F, 2F, 3F, 4F, 5F}, "$.index(2)"));
    }

    @Test
    public void indexIntMiss() {
        assertEquals(-1, JSONPath.extract("[1,2,3,4,5]", "$.index(6)"));
        assertEquals(-1, JSONPath.extract("[1.0,2.0,3.0,4.0,5.0]", "$.index(6)"));
        assertEquals(-1, JSONPath.eval(JSONArray.of(1F, 2F, 3F, 4F, 5F), "$.index(6)"));
        assertEquals(-1, JSONPath.eval(JSONArray.of(1D, 2D, 3D, 4D, 5D), "$.index(6)"));
        assertEquals(-1, JSONPath.eval(new int[]{1, 2, 3, 4, 5}, "$.index(6)"));
        assertEquals(-1, JSONPath.eval(new long[]{1, 2, 3, 4, 5}, "$.index(6)"));
        assertEquals(-1, JSONPath.eval(new float[]{1, 2, 3, 4, 5}, "$.index(6)"));
        assertEquals(-1, JSONPath.eval(new double[]{1, 2, 3, 4, 5}, "$.index(6)"));
        assertEquals(-1, JSONPath.eval(new Object[]{1, 2, 3, 4, 5}, "$.index(6)"));
        assertEquals(-1, JSONPath.eval(new Object[]{1L, 2L, 3L, 4L, 5L}, "$.index(6)"));
        assertEquals(-1, JSONPath.eval(new Object[]{1F, 2F, 3F, 4F, 5F}, "$.index(6)"));
    }

    @Test
    public void indexDecimal() {
        assertEquals(1, JSONPath.extract("[12.3,23.4,45.6]", "$.index(23.4)"));
        assertEquals(1, JSONPath.extract("['12.3','23.4','45.6']", "$.index(23.4)"));
    }

    @Test
    public void indexString() {
        assertEquals(1, JSONPath.extract("['A','B','C','D']", "$.index('B')"));
        assertEquals(2, JSONPath.extract("['Big', 'Medium', 'Small']", "$.index('Small')"));
        assertEquals(2, JSONPath.eval(JSONArray.of("Big", "Medium", "Small"), "$.index('Small')"));
    }
}
