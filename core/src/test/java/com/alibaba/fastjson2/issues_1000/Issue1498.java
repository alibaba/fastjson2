package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1498 {
    @Test
    public void test() {
        String str = "{\n\"name\": \"张三\",\n\"age\": 18\n}";
        assertTrue(JSON.isValid(str));
        assertTrue(JSON.isValid(str.getBytes()));
        assertTrue(JSON.isValid(str.toCharArray()));
    }
}
