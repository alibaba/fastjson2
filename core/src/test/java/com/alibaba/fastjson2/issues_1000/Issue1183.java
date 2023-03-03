package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1183 {
    @Test
    public void test() {
        String json = "{\"a\":1,\"c\":1}";
        assertTrue(JSON.isValid(json));
    }
}
