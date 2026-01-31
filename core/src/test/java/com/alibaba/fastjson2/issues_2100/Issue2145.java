package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class Issue2145 {
    @Test
    public void test() {
        String testJson = "{\"key2\": [null, null, null, null, null, null]}";
        Object jsonObject = JSON.parse(testJson);
        Object eval = JSONPath.eval(jsonObject, "$");
        assertDoesNotThrow(() -> {
            JSONPath.set(eval, "$..key3", "value2");
        });
    }
}
