package com.alibaba.fastjson2.v1issues.issue_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue3924 {
    @Test
    public void test_for_issue3924() {
        assertFalse(JSON.isValidArray("["));
        assertFalse(JSON.isValidArray("[[]"));
        String json_string = "[3,{\"tag\": [\"tag1\", \"tag2\"]},[\"tag1\",\"tag2\"],6]";
        Object r = JSONPath.extract(json_string, " $[1].tag[*] ");
        assertEquals("[\"tag1\",\"tag2\"]",r.toString());
    }
}
