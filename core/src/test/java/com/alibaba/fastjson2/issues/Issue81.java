package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue81 {
    @Test
    public void test_issue() {
        String jsono = "{\"a\":1}";
        String jsona = "[{\"a\":1}]";
        String value = "1111";
        assertFalse(JSON.isValid(jsona));
        assertTrue(JSON.isValid(jsono));
        assertFalse(JSON.isValidArray(jsono));
        assertTrue(JSON.isValidArray(jsona));
        assertFalse(JSON.isValid(value));
        assertFalse(JSON.isValidArray(value));
    }
}
