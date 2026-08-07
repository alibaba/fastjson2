package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @Author ：Nanqi
 * @Date ：Created in 00:14 2020/7/18
 */
public class Issue3351 {
    @Test
    public void test_for_issue() throws Exception {
        String cString = "c110";
        boolean cValid = JSONValidator.from(cString).validate();
        assertFalse(cValid);

        String jsonString = "{\"forecast\":\"sss\"}";
        boolean jsonValid = JSONValidator.from(jsonString).validate();
        assertTrue(jsonValid);
    }
}
