package com.alibaba.fastjson.issue_3500;

import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3516 {
    @Test
    public void test_for_issue() throws Exception {
        JSONValidator validator = JSONValidator.from("{}");
        assertEquals(JSONValidator.Type.Object, validator.getType());
        assertTrue(validator.validate());
    }
}
