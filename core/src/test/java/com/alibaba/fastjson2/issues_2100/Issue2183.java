package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2183 {
    @Test
    public void test() {
        Map<LocalDateTime, String> map = new HashMap<>();
        map.put(LocalDateTime.of(2014, 1, 2, 12, 13, 14), "2014");
        String str = JSON.toJSONString(map, JSONWriter.Feature.BrowserCompatible);
        assertEquals("{\"2014-01-02 12:13:14\":\"2014\"}", str);
    }

    @Test
    public void test1() {
        Map<Date, String> map = new HashMap<>();
        LocalDateTime ldt = LocalDateTime.of(2014, 1, 2, 12, 13, 14);
        long epochMilli = ldt.toInstant(DateUtils.SHANGHAI_ZONE_RULES.getOffset(ldt)).toEpochMilli();
        map.put(new Date(epochMilli), "2014");
        String str = JSON.toJSONString(map, JSONWriter.Feature.BrowserCompatible);
        assertEquals("{\"2014-01-02 12:13:14\":\"2014\"}", str);
    }
}
