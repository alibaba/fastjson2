package com.alibaba.fastjson.issue_2400;

import com.alibaba.fastjson.JSON;

import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class Issue2477 {

    @Test
    public void test_for_issue() {
        Map<String, Object> map = new HashMap<>();
        map.put("localDate", LocalDate.now());
        map.put("date", Date.from(Instant.now()));
        map.put("instant", Instant.now());
        map.put("localDateTime", LocalDateTime.now());
        map.put("localTime", LocalTime.now());
        map.put("offsetDateTime", OffsetDateTime.now());
        map.put("zonedDateTime", ZonedDateTime.now());
        map.put("calendar", Calendar.getInstance());
        System.out.println(JSON.toJSONString(map));
    }
}
