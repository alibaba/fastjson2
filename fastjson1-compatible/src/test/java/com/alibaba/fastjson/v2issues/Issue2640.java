package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2640 {
    @Test
    public void test() {
        Object val = "#";
        assertEquals(val, JSONPath.eval(val, "$"));
    }
}
