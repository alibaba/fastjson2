package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1789 {
    @Test
    public void test() {
        Person person = new Person();
        person.setZoneOffset(ZoneOffset.ofHours(8));
        String str = JSON.toJSONString(person); // {"zoneOffset":"+08:00"}
        Person person1 = JSON.parseObject(str, Person.class);
        assertNotNull(person1);
        assertEquals(person.zoneOffset, person1.zoneOffset);
    }

    @Data
    public static class Person {
        private String name;
        private Integer age;
        private Object object;
        private Date date;
        private Duration duration;
        private LocalTime localTime;
        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private ZoneOffset zoneOffset = ZoneOffset.ofHours(8);
    }
}
