package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue432 {
    @Test
    public void test() {
        assertEquals(123, JSONPath.read("{\"id\":123}", "$.id"));
    }
}
