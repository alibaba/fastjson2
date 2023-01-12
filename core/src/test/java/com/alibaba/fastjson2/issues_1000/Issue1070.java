package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1070 {
    @Test
    public void test() {
        String raw = "[[{\"a\": 1}]]";
        JSONArray arr = (JSONArray) JSONPath.extract(raw, "$[*][*]");

// Expect "[{"a": 1}]" but "[1]"
        assertEquals(
                "[1]",
                ((JSONArray) JSONPath.extract(raw, "$[0][*]")).toJSONString()
        );
    }
}
