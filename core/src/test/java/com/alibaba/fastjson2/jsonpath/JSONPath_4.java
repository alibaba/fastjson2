package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONPath_4 {
    @Test
    public void test_path() throws Exception {
        String a = "{\"key\":\"value\",\"10.0.1.1\":\"haha\"}";
        Object x = JSON.parse(a);
        JSONPath.set(x, "$.test", "abc");
        Object o = JSONPath.eval(x, "$.10\\.0\\.1\\.1");
        Assertions.assertEquals("haha", o);
    }

    @Test
    public void testChinese() {
        String a = "{\"key\":\"value\",\"你好\":\"haha\"}";
        Object x = JSON.parse(a);
        Object o = JSONPath.eval(x, "你好");
        Assertions.assertEquals("haha", o);
    }
}
