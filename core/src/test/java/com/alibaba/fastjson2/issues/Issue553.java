package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

public class Issue553 {
    @Test
    public void test() {
        String json = "{\"status\":0}";
        String path = "$.status";
        Object rt1 = JSONPath.extract(json, path);
        System.out.println(rt1);// 0
        Object rt2 = JSONPath.eval(json, path);
        System.out.println(rt2);// null
    }
}
