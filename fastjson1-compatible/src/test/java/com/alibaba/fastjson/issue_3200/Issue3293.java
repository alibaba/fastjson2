package com.alibaba.fastjson.issue_3200;

import com.alibaba.fastjson.JSONValidator;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @Author ：Nanqi
 * @Date ：Created in 09:59 2020/6/24
 */
public class Issue3293 {
    @Test
    public void test_for_issue() throws Exception {
        JSONValidator jv = JSONValidator.from("{\"a\"}");
        Assert.assertFalse(jv.validate());

        jv = JSONValidator.from("113{}[]");
        jv.setSupportMultiValue(false);
        Assert.assertFalse(jv.validate());
        Assert.assertEquals(JSONValidator.Type.Value, jv.getType());

        jv = JSONValidator.from("{\"a\":\"12333\"}");
        Assert.assertTrue(jv.validate());

        jv = JSONValidator.from("{}");
        Assert.assertTrue(jv.validate());
    }
}
