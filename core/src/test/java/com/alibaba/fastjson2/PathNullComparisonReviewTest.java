package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathNullComparisonReviewTest {
    @Test
    public void mapNullEqualityAndInequality() {
        JSONObject present = JSONObject.of("value", 1);
        JSONObject explicitNull = JSONObject.of("value", null);
        JSONObject missing = new JSONObject();
        JSONArray input = JSONArray.of(present, explicitNull, missing);
        assertEquals(JSONArray.of(explicitNull, missing), JSONPath.eval(input, "$[?(@.value == null)]"));
        assertEquals(JSONArray.of(present), JSONPath.eval(input, "$[?(@.value != null)]"));
        assertEquals(JSONArray.of(present), JSONPath.extract(input.toString(), "$[?(@.value != null)]"));
    }

    @Test
    public void nestedNullEqualityAndInequality() {
        JSONObject present = JSONObject.of("nested", JSONObject.of("value", 1));
        JSONObject explicitNull = JSONObject.of("nested", JSONObject.of("value", null));
        JSONArray input = JSONArray.of(present, explicitNull);
        assertEquals(JSONArray.of(explicitNull), JSONPath.eval(input, "$[?(@.nested.value == null)]"));
        assertEquals(JSONArray.of(present), JSONPath.eval(input, "$[?(@.nested.value != null)]"));
    }

    @Test
    public void beanNullEqualityAndInequality() {
        Bean present = new Bean();
        present.value = 1;
        Bean absent = new Bean();
        JSONArray input = JSONArray.of(present, absent);
        assertEquals(JSONArray.of(absent), JSONPath.eval(input, "$[?(@.value == null)]"));
        assertEquals(JSONArray.of(present), JSONPath.eval(input, "$[?(@.value != null)]"));
    }

    public static class Bean {
        public Integer value;
    }
}
