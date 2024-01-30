package com.alibaba.fastjson2.jsonpath.function;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EndsWithTest {
    @Test
    public void test() {
        JSONArray array = JSONArray.of(
                JSONObject.of("name", "abc"),
                JSONObject.of("name", "abd"),
                JSONObject.of("name", "xbc")
        );
        assertEquals("[{\"name\":\"abc\"},{\"name\":\"xbc\"}]", JSONPath.eval(array, "$[?(@.name endsWith 'bc')]").toString());
        assertEquals("[{\"name\":\"abc\"},{\"name\":\"xbc\"}]", JSONPath.eval(array, "$[?(@.name ends with 'bc')]").toString());
    }

    @Test
    public void test1() {
        JSONArray array = JSONArray.of("abc", "abc", "xbc");
        assertEquals("[\"abc\",\"abc\",\"xbc\"]", JSONPath.eval(array, "$[?(@ ends with 'bc')]").toString());
    }

    @Test
    public void testParent() {
        assertThrows(JSONException.class, () -> JSONPath.compile("#-1").getParent());
    }
}
