package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue81 {
    @Test
    public void test_issue() {
        String jsonObjectStr = "{\"a\":1}";
        String jsonArrayStr = "[{\"a\":1}]";
        String value = "1111";
        assertTrue(JSON.isValid(jsonArrayStr));
        assertTrue(JSON.isValid(jsonObjectStr));
        Assertions.assertFalse(JSON.isValidArray(jsonObjectStr));
        assertTrue(JSON.isValidArray(jsonArrayStr));
        assertTrue(JSON.isValid(value));
        Assertions.assertFalse(JSON.isValidArray(value));
    }
}
