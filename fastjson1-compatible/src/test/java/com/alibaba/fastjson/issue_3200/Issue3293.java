package com.alibaba.fastjson.issue_3200;

import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @Author ：Nanqi
 * @Date ：Created in 09:59 2020/6/24
 */
public class Issue3293 {
    @Test
    public void test_for_issue() throws Exception {
        JSONValidator jv = JSONValidator.from("{\"a\"}");
        Assertions.assertFalse(jv.validate());

        jv = JSONValidator.from("113");
        Assertions.assertTrue(jv.validate());
        Assertions.assertEquals(JSONValidator.Type.Value, jv.getType());

        jv = JSONValidator.from("{\"a\":\"12333\"}");
        Assertions.assertTrue(jv.validate());

        jv = JSONValidator.from("{}");
        Assertions.assertTrue(jv.validate());
    }
}
