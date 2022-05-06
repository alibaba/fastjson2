package com.alibaba.fastjson.issue_3200;

import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3267 {
    @Test
    public void test_for_issue() throws Exception {
        JSONValidator v = JSONValidator.from("113");
        assertTrue(
                v.validate());

        assertEquals(JSONValidator.Type.Value, v.getType());
    }
}
