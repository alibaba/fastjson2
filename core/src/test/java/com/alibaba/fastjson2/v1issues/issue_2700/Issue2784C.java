package com.alibaba.fastjson2.v1issues.issue_2700;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.time.Instant;
import com.alibaba.fastjson2.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2784C {
    LocalDateTime now = LocalDateTime.now();

    @Test
    public void test_for_issue() {
        Model m = new Model();
        m.time = now.toDate();
        String str = JSON.toJSONString(m);
        assertEquals("{\"time\":"
                + Instant.of(m.time).toEpochMilli()
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(Instant.of(m.time).toEpochMilli(), Instant.of(m1.time).toEpochMilli());
    }

    @Test
    public void f_test_for_issue_1() {
        Model m = new Model();
        m.ztime = new Date();
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
        m.time1 = new Date();
        String str = JSON.toJSONString(m);
        assertEquals("{\"time1\":"
                + Instant.of(m.time1).epochSecond
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(Instant.of(m.time1).epochSecond,
                Instant.of(m1.time1).epochSecond);
    }

    @Test
    public void test_for_issue_3() {
        Model m = new Model();
        m.ztime1 = new Date();
        String str = JSON.toJSONString(m);
        assertEquals("{\"ztime1\":"
                + Instant.of(m.ztime1).epochSecond
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(Instant.of(m.ztime1).epochSecond,
                Instant.of(m1.ztime1).epochSecond);
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
        assertEquals(
                LocalDateTime.of(2019, 7, 14, 12, 13, 14).toDate(),
                m.time2
        );
    }

    public static class Model {
        @JSONField(format = "millis")
        public Date time;

        @JSONField(format = "millis")
        public Date ztime;

        @JSONField(format = "unixtime")
        public Date time1;

        @JSONField(format = "unixtime")
        public Date ztime1;

        @JSONField(format = "millis")
        public Date date;

        @JSONField(format = "unixtime")
        public Date date1;

        @JSONField(format = "yyyyMMddHHmmss")
        public Date time2;
    }
}
