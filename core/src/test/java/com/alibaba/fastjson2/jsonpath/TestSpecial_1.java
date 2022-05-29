package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSpecial_1 {
    @Test
    public void test_special() {
        String x = "{\"10.0.0.1\":{\"region\":\"xxx\"}}";
        Object o = JSON.parse(x);
        assertTrue(JSONPath.contains(o, "$.10\\.0\\.0\\.1"));
        assertEquals("{\"region\":\"xxx\"}", JSONPath.eval(o, "$.10\\.0\\.0\\.1").toString());
        assertTrue(JSONPath.contains(o, "$.10\\.0\\.0\\.1.region"));
        assertEquals("xxx", JSONPath.eval(o, "$.10\\.0\\.0\\.1.region"));
    }
}
