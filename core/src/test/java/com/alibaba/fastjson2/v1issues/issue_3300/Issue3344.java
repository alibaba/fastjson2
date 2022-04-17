package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSONObject;
import junit.framework.TestCase;

import java.util.Date;
import java.util.TimeZone;


/**
 * @Author ：Nanqi
 * @Date ：Created in 18:28 2020/7/19
 */
public class Issue3344 extends TestCase {
    TimeZone defaultTimeZone;
    protected void setUp() {
        defaultTimeZone = TimeZone.getDefault();
    }

    protected void tearDown() {
        TimeZone.setDefault(defaultTimeZone);
    }

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
