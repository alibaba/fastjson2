package com.alibaba.fastjson2.v1issues.issue_3500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3516 {
    @Test
    public void test_for_issue() throws Exception {
        assertTrue(JSON.isValid("{}"));
    }
}
