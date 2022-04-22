package com.alibaba.fastjson.issue_1400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue1422 {
    @Test
    public void test_for_issue() throws Exception {
        String strOk = "{\"v\": 111}";

        Foo ok = JSON.parseObject(strOk, Foo.class);
        assertFalse(ok.v);
    }

    @Test
    public void test_for_issue_reader() throws Exception {
        String strBad = "{\"v\": 111}";
        Foo bad = new JSONReader(new StringReader(strBad)).readObject(Foo.class);
        assertFalse(bad.v);
    }

    @Test
    public void test_for_issue_1() throws Exception {
        String strBad = "{\"v\":111}";
        Foo bad = JSON.parseObject(strBad, Foo.class);
        assertFalse(bad.v);
    }

    @Test
    public void test_for_issue_1_reader() throws Exception {
        String strBad = "{\"v\":111}";
        Foo bad = new JSONReader(new StringReader(strBad)).readObject(Foo.class);
        assertFalse(bad.v);
    }

    public static class Foo {
        public boolean v;
    }
}
