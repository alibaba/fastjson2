package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2640 {
    @Test
    public void test() {
        Object val = "#";
        assertEquals(val, JSONPath.eval(val, "$"));
    }
}
