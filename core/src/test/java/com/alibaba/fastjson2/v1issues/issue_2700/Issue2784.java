package com.alibaba.fastjson2.v1issues.issue_2700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2784 {
    ZoneId zoneId = ZoneId.systemDefault();

    @Test
    public void test_for_issue() {
        Model m = new Model();
        m.time = LocalDateTime.now();
        String str = JSON.toJSONString(m);
        assertEquals("{\"time\":"
                + m.time.atZone(zoneId).toInstant().toEpochMilli()
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.time.atZone(zoneId).toInstant().toEpochMilli(), m1.time.atZone(zoneId).toInstant().toEpochMilli());
    }

    @Test
    public void f_test_for_issue_1() {
        Model m = new Model();
        m.ztime = ZonedDateTime.now();
        String str = JSON.toJSONString(m);
        assertEquals("{\"ztime\":"
                + m.ztime.toInstant().toEpochMilli()
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.ztime.toInstant().toEpochMilli(), m1.ztime.toInstant().toEpochMilli());
    }

    @Test
    public void test_for_issue_2() {
        Model m = new Model();
        m.time1 = LocalDateTime.now();
        String str = JSON.toJSONString(m);
        assertEquals("{\"time1\":"
                + m.time1.atZone(zoneId).toEpochSecond()
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.time1.atZone(zoneId).toEpochSecond(),
                m1.time1.atZone(zoneId).toEpochSecond());
    }

    @Test
    public void test_for_issue_3() {
        Model m = new Model();
        m.ztime1 = ZonedDateTime.now();
        String str = JSON.toJSONString(m);
        assertEquals("{\"ztime1\":"
                + m.ztime1.toEpochSecond()
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.ztime1.toEpochSecond(),
                m1.ztime1.toEpochSecond());
    }

    @Test
    public void test_for_issue_4() {
        Model m = new Model();
        m.date = new Date();
        String str = JSON.toJSONString(m);
        assertEquals("{\"date\":"
                + m.date.getTime()
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.date.getTime(),
                m1.date.getTime());
    }

    @Test
    public void test_for_issue_5() {
        Model m = new Model();
        m.date1 = new Date();
        String str = JSON.toJSONString(m);
        assertEquals("{\"date1\":"
                + (m.date1.getTime() / 1000)
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.date1.getTime() / 1000,
                m1.date1.getTime() / 1000);
    }

    @Test
    public void test_for_issue_6() {
        Model m = new Model();
        m.date1 = new Date();
        String str = JSON.toJSONString(m);
        assertEquals("{\"date1\":"
                + (m.date1.getTime() / 1000)
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.date1.getTime() / 1000,
                m1.date1.getTime() / 1000);
    }

    @Test
    public void test_for_issue_7() {
        Model m = JSON.parseObject("{\"time2\":20190714121314}", Model.class);
        assertEquals(LocalDateTime.of(2019, 7, 14, 12, 13, 14), m.time2);
    }

    public static class Model {
        @JSONField(format = "millis")
        public LocalDateTime time;

        @JSONField(format = "millis")
        public ZonedDateTime ztime;

        @JSONField(format = "unixtime")
        public LocalDateTime time1;

        @JSONField(format = "unixtime")
        public ZonedDateTime ztime1;

        @JSONField(format = "millis")
        public Date date;

        @JSONField(format = "unixtime")
        public Date date1;

        @JSONField(format = "yyyyMMddHHmmss")
        public LocalDateTime time2;
    }
}
