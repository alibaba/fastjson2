package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue495 {
    @Test
    public void testX() {
        LocalTime time = LocalTime.of(9, 10);
        assertEquals("\"09:10:00\"", JSON.toJSONString(time));
        assertEquals("4200000", JSON.toJSONString(time, "millis"));
        assertEquals("4200", JSON.toJSONString(time, "unixtime"));

        assertEquals(time, JSON.parseObject("4200000", LocalTime.class));
    }

    @Test
    public void test() {
        String str = "{\"time\": \"09:00\"}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals("{\"time\":\"09:00\"}", JSON.toJSONString(bean));

        assertEquals("\"09:00\"", JSON.toJSONString(bean.time, "HH:mm"));
    }

    public static class Bean {
        @JSONField(format = "HH:mm")
        public LocalTime time;
    }

    @Test
    public void test1() {
        String str = "{\"time\": 33000000}";
        Bean1 bean = JSON.parseObject(str, Bean1.class);
        assertEquals("{\"time\":33000000}", JSON.toJSONString(bean));

        Bean1 bean1 = JSON.parseObject("{\"time\":\"33000000\"}", Bean1.class);
        assertEquals(bean.time, bean1.time);
    }

    public static class Bean1 {
        @JSONField(format = "millis")
        public LocalTime time;
    }

    @Test
    public void test2() {
        String str = "{\"time\": 33000}";
        Bean2 bean = JSON.parseObject(str, Bean2.class);
        assertEquals("{\"time\":33000}", JSON.toJSONString(bean));

        Bean2 bean1 = JSON.parseObject("{\"time\":\"33000\"}", Bean2.class);
        assertEquals(bean.time, bean1.time);
    }

    public static class Bean2 {
        @JSONField(format = "unixtime")
        public LocalTime time;
    }

    @Test
    public void test3() {
        String str = "{\"time\":\"2017-07-14 09:10:0\"}";
        Bean3 bean = JSON.parseObject(str, Bean3.class);
        assertEquals(LocalTime.of(9, 10), bean.time);
    }

    public static class Bean3 {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public LocalTime time;
    }

    @Test
    public void test4() {
        String str = "{\"time\":\"2017-07-14T09:10:00\"}";
        Bean4 bean = JSON.parseObject(str, Bean4.class);
        assertEquals(LocalTime.of(9, 10), bean.time);
    }

    public static class Bean4 {
        @JSONField(format = "yyyy-MM-dd'T'HH:mm:ss")
        public LocalTime time;
    }

    @Test
    public void test5() {
        String str = "{\"time\":\"2017/07/14 09:10:00\"}";
        Bean5 bean = JSON.parseObject(str, Bean5.class);
        assertEquals(LocalTime.of(9, 10), bean.time);
    }

    public static class Bean5 {
        @JSONField(format = "yyyy/MM/dd HH:mm:ss")
        public LocalTime time;
    }
}
