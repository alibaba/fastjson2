package com.alibaba.fastjson2.v1issues.issue_4000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("regression")
@Tag("compat-fastjson1")
public class Issue4050 {
    @Test
    public void test_validate() {
        String str = "{\"file\":\"d:\\abc.txt\"}";
        assertFalse(
                JSON.isValid(str));
    }
}
