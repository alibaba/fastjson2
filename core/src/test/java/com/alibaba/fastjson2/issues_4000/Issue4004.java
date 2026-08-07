package com.alibaba.fastjson2.issues_4000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

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

    public static class Event {
        private OffsetDateTime timestamp;

        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
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

    @Test
    public void testOffsetDateTime() {
        String json1 = "{\n  \"timestamp\": \"2025-01-02T10:30:00+08:00\"\n}";
        Event e1 = JSON.parseObject(json1, Event.class);
        assertNotNull(e1);
        assertNotNull(e1.getTimestamp());

        String json2 = "{\n  \"timestamp\": \"2025-01-02T10:30:00+08:00\",\n}";
        Event e2 = JSON.parseObject(json2, Event.class);
        assertNotNull(e2);
        assertNotNull(e2.getTimestamp());
    }
}
