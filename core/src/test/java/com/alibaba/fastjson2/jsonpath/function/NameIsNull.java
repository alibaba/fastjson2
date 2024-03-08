package com.alibaba.fastjson2.jsonpath.function;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameIsNull {
    @Test
    public void test() {
        JSONArray array = JSONArray.of(
                JSONObject.of("name", "abc", "v", 1),
                JSONObject.of("name", "abd"),
                JSONObject.of("name", "xbc", "v", 1)
        );
        assertEquals("[{\"name\":\"abd\"}]", JSONPath.eval(array, "$[?(@.v is null)]").toString());
    }
}
