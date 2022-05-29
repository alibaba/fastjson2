package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZonedDateTimeTest {
    @Test
    public void test() {
        String str = "{\"birthday\":\"2022-05-03 15:26:05\"}";
        Student student = JSON.parseObject(str, Student.class);
        assertEquals(str, JSON.toJSONString(student));
    }

    public static class Student {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public ZonedDateTime birthday;
    }

    @Test
    public void test1() {
        String str = "{\"birthday\":\"2022-05-03T15:26:05\"}";
        Student1 student = JSON.parseObject(str, Student1.class);
        String str2 = JSON.toJSONString(student);
        Student1 student1 = JSON.parseObject(str2, Student1.class);
        assertEquals(student.birthday.getYear(), student1.birthday.getYear());
        assertEquals(student.birthday.getMonthValue(), student1.birthday.getMonthValue());
        assertEquals(student.birthday.getDayOfMonth(), student1.birthday.getDayOfMonth());
        assertEquals(student.birthday.getHour(), student1.birthday.getHour());
        assertEquals(student.birthday.getMinute(), student1.birthday.getMinute());
        assertEquals(student.birthday.getSecond(), student1.birthday.getSecond());
        assertEquals(student.birthday.getNano(), student1.birthday.getNano());
        assertEquals(student.birthday.toInstant().toEpochMilli(), student1.birthday.toInstant().toEpochMilli());
    }

    @Test
    public void test1_null() {
        String str = "{\"birthday\":null}";
        Student1 student = JSON.parseObject(str, Student1.class);
        assertEquals(str, JSON.toJSONString(student, JSONWriter.Feature.WriteNulls));
    }

    public static class Student1 {
        @JSONField(format = "iso8601")
        public ZonedDateTime birthday;
    }

    @Test
    public void test2() {
        String str = "{\"birthday\":\"2022-05-03 15:26:05\"}";
        Student2 student = JSON.parseObject(str, Student2.class);
        assertEquals(str, JSON.toJSONString(student, "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void test3() {
//        ZonedDateTime zonedDateTime = ZonedDateTime.now();
//        String zonedDateTimeStr = JSON.toJSONString(zonedDateTime);
//        System.out.println("=========>zonedDateTimeStr:" + zonedDateTimeStr);
//
//        ZonedDateTime zonedDateTime1 = JSON.parseObject(zonedDateTimeStr,ZonedDateTime.class);
//        System.out.println("=========>zonedDateTime1:" + zonedDateTime1);

        String s = "\"2022-04-24T12:29:11.729+03:00[Europe/Moscow]\"";
        ZonedDateTime zonedDateTime2 = JSON.parseObject(s, ZonedDateTime.class);
        assertEquals(s, '"' + zonedDateTime2.toString() + '"');
    }

    public static class Student2 {
        public ZonedDateTime birthday;
    }
}
