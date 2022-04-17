package com.alibaba.fastjson2.v1issues.issue_3500;

import junit.framework.TestCase;

public class Issue3521 extends TestCase {
    public void test_for_issue() throws Exception {
        // assertFalse(JSON.isValid("{\"cat\":\"dog\"\"cat\":\"dog\"}")); // TODO true
    }
}
