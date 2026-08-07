package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2542 {
    @Test
    public void test() {
        String temp = "{\n" +
                "  \"code\": \"1003\", \n" +
                "  \"data\": [1, 2], \n" +
                "}\n";
        assertEquals(
                JSONPath.eval(temp, "$.data"),
                com.alibaba.fastjson.JSONPath.eval(temp, "$.data")
        );
    }
}
