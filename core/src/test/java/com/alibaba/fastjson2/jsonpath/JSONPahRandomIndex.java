package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONPahRandomIndex {
    @Test
    public void testRandomIndex() {
        JSONArray array = JSONArray.of("1", "2", "3");
        JSONPath jsonPath = JSONPath.of("$[randomIndex()]");
        for (int i = 0; i < 100; ++i) {
            assertNotNull(
                    jsonPath.eval(array)
            );
        }
    }
}
