package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathTest9 {
    @Test
    public void test0() {
        String str = "[1,2,3,4,5]";
        assertEquals(1, JSONPath.extract(str, "$[*][0]"));
    }
}
