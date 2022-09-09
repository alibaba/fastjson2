package com.alibaba.fastjson2.v1issues.issue_3500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue3521 {
    @Test
    public void test_for_issue() throws Exception {
        String str = "{\"cat\":\"dog\"\"cat\":\"dog\"}";

        char[] chars = str.toCharArray();
        assertFalse(
                JSON.isValid(chars)
        );

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        assertFalse(
                JSON.isValid(bytes)
        );

        assertFalse(
                JSON.isValid(str)
        );
    }
}
