package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;

import java.util.Date;

public class Issue_for_zuojian extends TestCase {
    public void test_for_issue() throws Exception {
        String json = "{\"value\":\"20180131022733000-0800\"}";
        Model model = JSON.parseObject(json, Model.class, "yyyyMMddHHmmssSSSZ");
        assertNotNull(model.value);
    }

    public static class Model {
        public Date value;
    }
}
