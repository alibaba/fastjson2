package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import junit.framework.TestCase;

import java.util.Date;

public class Issue1679 extends TestCase {
    protected void setUp() throws Exception {

    }

    public void test_for_issue() throws Exception {
        String json = "{\"create\":\"2018-01-10 08:30:00\"}";
        User user = JSON.parseObject(json, User.class);

        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.getContext().setDateFormat("iso8601");
        jsonWriter.writeAny(user.create);

        assertEquals("\"2018-01-10T08:30:00+08:00\"", jsonWriter.toString());
    }

    public static class User{
        public Date create;
    }
}
