package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest2 {
    private TimeZone timeZone;

    @BeforeEach
    protected void setUp() throws Exception {
        timeZone = JSON.defaultTimeZone;
    }

    @AfterEach
    public void tearDown() throws Exception {
        JSON.defaultTimeZone = timeZone;
    }

    @Test
    public void test_date() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("America/Chicago");
        Date date = new Date(1531928656055L);
        TestBean bean = new TestBean();
        bean.setDate(date);

        String iso = JSON.toJSONString(bean, SerializerFeature.UseISO8601DateFormat);
        assertEquals("{\"date\":\"2018-07-18T10:44:16.055-05:00\"}", iso);
    }

    public static class TestBean {
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
