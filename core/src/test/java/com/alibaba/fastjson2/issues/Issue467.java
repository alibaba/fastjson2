package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue467 {
    @Test
    public void test() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean.class).date);
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd")
        public Date date;
    }

    @Test
    public void test1() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean1.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean1.class).date);
    }

    public static class Bean1 {
        @JSONField(format = "yyyy-MM-dd")
        public Calendar date;
    }

    @Test
    public void test2() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean2.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean2.class).date);
    }

    public static class Bean2 {
        @JSONField(format = "yyyy-MM-dd")
        public Instant date;
    }

    @Test
    public void test3() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean3.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean3.class).date);
    }

    public static class Bean3 {
        @JSONField(format = "yyyy-MM-dd")
        public LocalDateTime date;
    }

    @Test
    public void test4() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean4.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean4.class).date);
    }

    public static class Bean4 {
        @JSONField(format = "yyyy-MM-dd")
        public LocalDate date;
    }

    @Test
    public void test5() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean5.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean5.class).date);
    }

    public static class Bean5 {
        @JSONField(format = "HH:mm:ss")
        public LocalTime date;
    }

    @Test
    public void test6() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean6.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean6.class).date);
    }

    public static class Bean6 {
        @JSONField(format = "yyyy-MM-dd")
        public java.sql.Date date;
    }

    @Test
    public void test7() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean7.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean7.class).date);
    }

    public static class Bean7 {
        @JSONField(format = "yyyy-MM-dd")
        public java.sql.Timestamp date;
    }

    @Test
    public void test8() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean8.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean8.class).date);
    }

    public static class Bean8 {
        @JSONField(format = "yyyy-MM-dd")
        public java.sql.Time date;
    }

    @Test
    public void test9() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean9.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean9.class).date);
    }

    public static class Bean9 {
        @JSONField(format = "yyyy-MM-dd")
        public org.joda.time.LocalDate date;
    }

    @Test
    public void test10() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean10.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean10.class).date);
    }

    public static class Bean10 {
        @JSONField(format = "yyyy-MM-dd")
        public org.joda.time.LocalDateTime date;
    }

    @Test
    public void test11() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean11.class).date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean11.class).date);
    }

    public static class Bean11 {
        @JSONField(format = "yyyy-MM-dd")
        public org.joda.time.Instant date;
    }
}
