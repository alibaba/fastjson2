package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Author ：Nanqi
 * @Date ：Created in 18:28 2020/7/19
 */
public class Issue3344 {
    private TimeZone defaultTimeZone;

    @BeforeEach
    public void before() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @AfterEach
    public void after() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    public void test_for_issue_timeZone() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
        String jsonStr = "{\"date\":1595154768}";
        Model model = JSONObject.parseObject(jsonStr, Model.class);
        assertEquals("Mon Jan 19 12:05:54 GMT+01:00 1970", model.getDate().toString());
    }

    static class Model {
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
