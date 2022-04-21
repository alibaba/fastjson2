package com.alibaba.fastjson.issue_3500;

import com.alibaba.fastjson.JSONValidator;
import junit.framework.TestCase;

public class Issue3521 extends TestCase {
    public void test_for_issue() throws Exception {
        JSONValidator jsv = JSONValidator.from("{\"cat\":\"dog\"\"cat\":\"dog\"}"); // 字段之间缺英文逗号，不是json
        System.out.println(jsv.getType()); // Object
        assertFalse(jsv.validate()); // true
    }
}
