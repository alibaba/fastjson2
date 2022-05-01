package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 30/06/2017.
 */
public class Issue1298 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject object = new JSONObject();

        object.put("date", "2017-06-29T08:06:30.000+05:30");

        Date date = object.getObject("date", Date.class);

        assertEquals("\"2017-06-29T10:36:30+08:00\"", JSON.toJSONString(date, "iso8601"));
    }

    @Test
    public void test_for_issue_1() throws Exception {
        JSONObject object = new JSONObject();

        object.put("date", "2017-08-15 20:00:00.000");

        Date date = object.getObject("date", Date.class);

        assertEquals("\"2017-08-15T20:00:00+08:00\"", JSON.toJSONString(date, "iso8601"));

        JSON.parseObject("\"2017-08-15 20:00:00.000\"", Date.class);
    }
}
