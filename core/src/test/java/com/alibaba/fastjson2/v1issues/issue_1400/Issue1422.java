package com.alibaba.fastjson2.v1issues.issue_1400;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;

public class Issue1422 extends TestCase {
    public void test_for_issue() throws Exception {
        String strOk = "{\"v\": 111}";

        Foo ok = JSON.parseObject(strOk, Foo.class);
        assertFalse(ok.v);
    }


    public void test_for_issue_1() throws Exception {
        String strBad = "{\"v\":111}";
        Foo bad = JSON.parseObject(strBad, Foo.class);
        assertFalse(bad.v);
    }

    public static class Foo {
        public boolean v;
    }
}
