package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathLikeReviewTest {
    @Test
    public void wildcardOnly() {
        JSONArray values = JSONArray.of(JSONObject.of("name", ""), JSONObject.of("name", "abc"));
        for (String pattern : new String[]{"%", "%%", "%%%"}) {
            assertEquals(values, JSONPath.eval(values, "$[?(@.name like '" + pattern + "')]"));
            assertEquals(new JSONArray(), JSONPath.eval(values, "$[?(@.name not like '" + pattern + "')]"));
        }
    }

    @Test
    public void anchoredPrefixWithMultipleWildcards() {
        JSONObject match = JSONObject.of("name", "abc");
        JSONObject mismatch = JSONObject.of("name", "xabc");
        JSONArray values = JSONArray.of(match, mismatch);
        assertEquals(JSONArray.of(match), JSONPath.eval(values, "$[?(@.name like 'a%b%')]"));
        assertEquals(JSONArray.of(mismatch), JSONPath.eval(values, "$[?(@.name not like 'a%b%')]"));
    }
}
