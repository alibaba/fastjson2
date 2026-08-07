package com.alibaba.fastjson.issue_3200;

import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3245 {
    @Test
    public void test_for_issue() throws Exception {
        JSONValidator v = JSONValidator.from("[]");
        v.validate();
        assertEquals(JSONValidator.Type.Array, v.getType());
    }

    @Test
    public void test_for_issue_1() throws Exception {
        assertEquals(JSONValidator.Type.Array, JSONValidator.from("[]").getType());
    }
}
