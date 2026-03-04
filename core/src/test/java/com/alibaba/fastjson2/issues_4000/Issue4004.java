package com.alibaba.fastjson2.issues_4000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class Issue4004 {
    public static class Person {
        private LocalDate birthday;

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }
    }

    @Test
    public void test() {
        String json1 = "{\n  \"birthday\": \"2025-01-02\"\n}";
        Person p1 = JSON.parseObject(json1, Person.class);
        assertNotNull(p1);
        assertEquals(LocalDate.of(2025, 1, 2), p1.getBirthday());

        String json2 = "{\n  \"birthday\": \"2025-01-02\",\n}";
        Person p2 = JSON.parseObject(json2, Person.class);
        assertNotNull(p2);
        assertEquals(LocalDate.of(2025, 1, 2), p2.getBirthday());

        String json3 = "{\n  \"birthday\": \"2025-01-02 00:00:00\"\n}";
        Person p3 = JSON.parseObject(json3, Person.class);
        assertNotNull(p3);
        assertEquals(LocalDate.of(2025, 1, 2), p3.getBirthday());
    }
}
