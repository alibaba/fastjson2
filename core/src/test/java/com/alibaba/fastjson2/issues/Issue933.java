package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Issue933 {
    @Test
    public void test() throws Exception {
        Map<String, ZonedDateTime> date = new HashMap<>();
        date.put("date", ZonedDateTime.parse("2022-11-15T00:00:00.000+08:00", DateTimeFormatter.ISO_DATE_TIME));
        System.out.println(JSON.toJSONString(date));
    }
}
