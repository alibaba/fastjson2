package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue553 {
    @Test
    public void test() {
        String json = "{\"status\":0}";
        String path = "$.status";
        Object rt1 = JSONPath.extract(json, path);
        Object rt2 = JSONPath.eval(json, path);
        assertEquals(rt1, rt2);
    }
}
