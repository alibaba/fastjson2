package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JodaLocalDateTest {
    @Test
    public void test() {
        String str = "{\"birthday\":\"2022-05-03\"}";
        Student student = JSON.parseObject(str, Student.class);
        assertEquals(str, JSON.toJSONString(student));
    }

    public static class Student {
        @JSONField(format = "yyyy-MM-dd")
        public LocalDate birthday;
    }

    @Test
    public void test1() {
        String str = "{\"birthday\":\"2022-05-03\"}";
        Student1 student = JSON.parseObject(str, Student1.class);
        assertEquals(str, JSON.toJSONString(student, "yyyy-MM-dd"));
    }

    public static class Student1 {
        public LocalDate birthday;
    }

    @Test
    public void test2() {
        String str = "{\"birthday\":\"20220503\"}";
        Student2 student = JSON.parseObject(str, Student2.class);
        assertEquals(str, JSON.toJSONString(student));
    }

    public static class Student2 {
        @JSONField(format = "yyyyMMdd")
        public LocalDate birthday;
    }
}
