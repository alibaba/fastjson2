package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue842 {
    @Test
    public void test() {
        String str = "{\"items\": [{\"data\":{\"0\":\"abc\"}}]}";
        Object result = JSONPath.eval(str, "$.items[0].data.0");
        assertEquals("abc", result);
    }
}
