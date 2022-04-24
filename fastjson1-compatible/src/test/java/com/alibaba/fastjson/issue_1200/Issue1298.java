package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 30/06/2017.
 */
public class Issue1298 {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.US;
    }

    @Test
    public void test_for_issue() throws Exception {
        JSONObject object = new JSONObject();

        object.put("date", "2017-06-29T08:06:30.000+05:30");

        Date date = object.getObject("date", Date.class);

        assertEquals("\"2017-06-29T10:36:30+08:00\"", JSON.toJSONString(date, SerializerFeature.UseISO8601DateFormat));
    }

    @Test
    public void test_for_issue_1() throws Exception {
        JSONObject object = new JSONObject();

        object.put("date", "2017-08-15 20:00:00.000");

        Date date = object.getObject("date", Date.class);

        assertEquals("\"2017-08-15T20:00:00+08:00\"", JSON.toJSONString(date, SerializerFeature.UseISO8601DateFormat));

        JSON.parseObject("\"2017-08-15 20:00:00.000\"", Date.class);
    }
}
