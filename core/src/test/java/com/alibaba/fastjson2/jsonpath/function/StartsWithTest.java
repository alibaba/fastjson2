package com.alibaba.fastjson2.jsonpath.function;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StartsWithTest {
    @Test
    public void test() {
        JSONArray array = JSONArray.of(
                JSONObject.of("name", "abc"),
                JSONObject.of("name", "abd"),
                JSONObject.of("name", "xbc")
        );
        assertEquals("[{\"name\":\"abc\"},{\"name\":\"abd\"}]", JSONPath.eval(array, "$[?(@.name startsWith 'ab')]").toString());
        assertEquals("[{\"name\":\"abc\"},{\"name\":\"abd\"}]", JSONPath.eval(array, "$[?(@.name starts with 'ab')]").toString());
    }

    @Test
    public void test1() {
        JSONArray array = JSONArray.of("abc", "abc", "xbc");
        assertEquals("[\"abc\",\"abc\"]", JSONPath.eval(array, "$[?(@ starts with 'ab')]").toString());
    }
}
