package com.alibaba.fastjson.issue_3500;

import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue3521 {
    @Test
    public void test_for_issue() throws Exception {
        JSONValidator jsv = JSONValidator.from("{\"cat\":\"dog\"\"cat\":\"dog\"}"); // 字段之间缺英文逗号，不是json
        assertFalse(jsv.validate());
    }
}
