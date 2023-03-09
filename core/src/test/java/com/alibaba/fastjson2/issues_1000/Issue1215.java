package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1215 {
    @Test
    public void test() {
        String str = "{\"x\":{},\"message\":\"1\"}";
        assertNull(JSONPath.extract(str, "$.x.y"));
    }
}
