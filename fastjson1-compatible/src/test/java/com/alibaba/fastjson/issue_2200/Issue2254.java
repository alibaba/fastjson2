package com.alibaba.fastjson.issue_2200;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2254 {
    @Test
    public void test_for_issue() throws Exception {
        String jsonString = "{\"a\":[1.0,2.0]}"; //{"a":[1.0,2.0]}
        Exception error = null;
        try {
            JSON.parseObject(jsonString, TestClass.class);
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    public static class TestClass {
        public float[][] a;
    }
}
