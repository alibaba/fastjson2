package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionalLocalDateTimeTest {
    @Test
    public void test() {
        String str = "{\"birthday\":\"2022-05-03 15:26:05\"}";
        Student student = JSON.parseObject(str, Student.class);
        assertEquals(str, JSON.toJSONString(student));
    }

    public static class Student {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Optional<LocalDateTime> birthday;
    }

    @Test
    public void test1() {
        String str = "{\"birthday\":\"2022-05-03T15:26:05\"}";
        Student1 student = JSON.parseObject(str, Student1.class);
        assertEquals("{\"birthday\":\"2022-05-03T15:26:05+08:00\"}", JSON.toJSONString(student));
    }

    @Test
    public void test1_null() {
        String str = "{\"birthday\":null}";
        Student1 student = JSON.parseObject(str, Student1.class);
        assertEquals(str, JSON.toJSONString(student, JSONWriter.Feature.WriteNulls));
    }

    public static class Student1 {
        @JSONField(format = "iso8601")
        public Optional<LocalDateTime> birthday;
    }

    @Test
    public void test2() {
        String str = "{\"birthday\":\"2022-05-03 15:26:05\"}";
        Student2 student = JSON.parseObject(str, Student2.class);
        assertEquals(str, JSON.toJSONString(student, "yyyy-MM-dd HH:mm:ss"));
    }

    public static class Student2 {
        public Optional<LocalDateTime> birthday;
    }
}
