package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;

public class Issue3336 extends TestCase {
    public void test_for_issue() throws Exception {
        String s = "{\"schema\":{\"$ref\":\"#/definitions/URLJumpConfig\"}}";
        assertEquals(s, JSON.parseObject(s)
                .toJSONString());

        String s1 = "{\"schema\":{\"ref\":\"#/definitions/URLJumpConfig\"}}";
        assertEquals(s1, JSON.parseObject(s1)
                .toJSONString());

        String s2 = "{\"schema\":{\"$ref\":\"#/definitions/URLJumpConfig\"}}";
        assertEquals(s2, JSON.parseObject(s2)
                .toJSONString());
    }
}
