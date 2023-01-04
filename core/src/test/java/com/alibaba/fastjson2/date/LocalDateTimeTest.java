package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCompiler;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.alibaba.fastjson2.annotation.JSONCompiler.CompilerOption.LAMBDA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class LocalDateTimeTest {
    @Test
    public void test() {
        String str = "{\"birthday\":\"2022-05-03 15:26:05\"}";
        Student student = JSON.parseObject(str, Student.class);
        assertEquals(str, JSON.toJSONString(student));
    }

    public static class Student {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public LocalDateTime birthday;
    }

    @Test
    public void test1() {
        String str = "{\"birthday\":\"2022-05-03T15:26:05\"}";
        Student1 student = JSON.parseObject(str, Student1.class);
        assertEquals("{\"birthday\":\"2022-05-03T15:26:05+08:00\"}", JSON.toJSONString(student));

        Student1 student1 = new Student1();
        JSONPath.of("$.birthday")
                .set(student1, "2022-05-03T15:26:05");
        assertEquals(student.birthday, student1.birthday);
    }

    @Test
    public void test1_date() {
        String str = "{\"birthday\":\"2022-05-03\"}";
        Student1 student = JSON.parseObject(str, Student1.class);
        assertEquals("{\"birthday\":\"2022-05-03T00:00:00+08:00\"}", JSON.toJSONString(student));
    }

    @Test
    public void test1_null() {
        String str = "{\"birthday\":null}";
        Student1 student = JSON.parseObject(str, Student1.class);
        assertEquals(str, JSON.toJSONString(student, JSONWriter.Feature.WriteNulls));
    }

    public static class Student1 {
        @JSONField(format = "iso8601")
        public LocalDateTime birthday;
    }

    @Test
    public void test2() {
        String str = "{\"birthday\":\"2022-05-03 15:26:05\"}";
        Student2 student = JSON.parseObject(str, Student2.class);
        assertEquals(str, JSON.toJSONString(student, "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void test2_1() {
        String str = "{\"birthday\":\"2022-05-03 15:26\"}";
        Student2 student = JSON.parseObject(str, Student2.class);
        assertEquals("{\"birthday\":\"2022-05-03 15:26:00\"}", JSON.toJSONString(student));
    }

    public static class Student2 {
        public LocalDateTime birthday;
    }

    @Test
    public void test3_1() {
        String str = "{\"birthday\":\"2022-05-03 15:26\"}";
        Student3 student = JSON.parseObject(str, Student3.class);
        assertEquals("{\"birthday\":\"2022-05-03 15:26:00\"}", JSON.toJSONString(student));

        Student3 student1 = JSON.parseObject(str).toJavaObject(Student3.class);
        assertEquals("{\"birthday\":\"2022-05-03 15:26:00\"}", JSON.toJSONString(student1));
    }

    public static class Student3 {
        private LocalDateTime birthday;

        public LocalDateTime getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDateTime birthday) {
            this.birthday = birthday;
        }
    }

    @Test
    public void test4() {
        String str = "{\"birthday\":\"2022-05-03 15:26\"}";
        Student4 student = JSON.parseObject(str, Student4.class);
        assertEquals("{\"birthday\":\"2022-05-03 15:26:00\"}", JSON.toJSONString(student));

        Student4 student1 = JSON.parseObject(str).toJavaObject(Student4.class);
        assertEquals(student.birthday, student1.birthday);

        JSONPath jsonPath = JSONPath.of("$.birthday");
        assertSame(student.birthday, jsonPath.eval(student));
    }

    @JSONCompiler(LAMBDA)
    public static class Student4 {
        private LocalDateTime birthday;

        public Student4() {
        }

        public LocalDateTime getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDateTime birthday) {
            this.birthday = birthday;
        }
    }

    @Test
    public void test5() {
        String str = "{\"birthday\":\"2022-05-03 15:26\"}";
        Student5 student = JSON.parseObject(str, Student5.class);
        assertEquals("{\"birthday\":\"2022-05-03 15:26:00\"}", JSON.toJSONString(student));

        Student5 student1 = JSON.parseObject(str).toJavaObject(Student5.class);
        assertEquals(student.birthday, student1.birthday);

        JSONPath jsonPath = JSONPath.of("$.birthday");
        assertSame(student.birthday, jsonPath.eval(student));
    }

    public static class Student5 {
        private LocalDateTime birthday;

        @JSONCompiler(LAMBDA)
        public Student5() {
        }

        @JSONCompiler(LAMBDA)
        public LocalDateTime getBirthday() {
            return birthday;
        }

        @JSONCompiler(LAMBDA)
        public void setBirthday(LocalDateTime birthday) {
            this.birthday = birthday;
        }
    }

    @Test
    public void test6() {
        String str = "\"2022-05-03 15:26\"";
        Student6 student = JSON.parseObject(str, Student6.class);

        JSONPath jsonPath = JSONPath.of("$.birthday");
        assertSame(student.birthday, jsonPath.eval(student));
    }

    @JSONCompiler(LAMBDA)
    public static class Student6 {
        private final LocalDateTime birthday;

        @JSONCreator
        public Student6(@JSONField(value = true) LocalDateTime birthday) {
            this.birthday = birthday;
        }

        public LocalDateTime getBirthday() {
            return birthday;
        }
    }
}
