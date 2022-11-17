package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue933 {
    @Test
    public void test() throws Exception {
        ZonedDateTime zdt = ZonedDateTime.parse("2022-11-15T00:00:00.000+08:00", DateTimeFormatter.ISO_DATE_TIME);

        Map<String, ZonedDateTime> map = new HashMap<>();
        map.put("date", zdt);
        String expected = "{\"date\":\"2022-11-15T00:00:00+08:00\"}";
        assertEquals(expected, JSON.toJSONString(map));
        assertEquals(expected, new String(JSON.toJSONBytes(map)));
    }
}
