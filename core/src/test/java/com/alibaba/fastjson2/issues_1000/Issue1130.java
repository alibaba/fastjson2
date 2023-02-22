package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1130 {
    @Test
    public void test() {
        String raw = "{\"arr1\":[\"a\"],\"numeric\":1,\"arr2\":[\"b\"]}";
        Object result = JSONPath.eval(raw, "$.arr2[0]");
        assertEquals("b", result);
    }
}
