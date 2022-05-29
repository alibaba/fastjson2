package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1679 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\"create\":\"2018-01-10 08:30:00\"}";
        User user = JSON.parseObject(json, User.class);

        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.getContext().setDateFormat("iso8601");
        jsonWriter.writeAny(user.create);

        assertEquals("\"2018-01-10T08:30:00+08:00\"", jsonWriter.toString());
    }

    public static class User {
        public Date create;
    }
}
