package com.alibaba.fastjson2.v1issues.issue_3500;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;

public class Issue3516 extends TestCase {
    public void test_for_issue() throws Exception {
        assertTrue(JSON.isValid("{}"));
    }
}
