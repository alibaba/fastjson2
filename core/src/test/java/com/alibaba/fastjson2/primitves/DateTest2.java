package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest2 {
    @Test
    public void test_jsonb_0() {
        long millis = 2524492800000L;
        Date date = new Date(millis);

        byte[] jsonbBytes = JSONB.toBytes(date);
        Date date2 = JSONB.parseObject(jsonbBytes, Date.class);
        assertEquals(date, date2);
    }

    @Test
    public void test_jsonb_1() {
        long millis = 1000000L;
        Date date = new Date(millis);

        byte[] jsonbBytes = JSONB.toBytes(date);
        Date date2 = JSONB.parseObject(jsonbBytes, Date.class);
        assertEquals(date, date2);
    }

    @Test
    public void test_jsonb_1_map() {
        long millis = 1000000L;
        Date date = new Date(millis);

        Map map = new HashMap();
        map.put("date", date);

        byte[] jsonbBytes = JSONB.toBytes(map);
        Map map2 = JSONB.parseObject(jsonbBytes, Map.class);
        assertEquals(date, map2.get("date"));
    }

    @Test
    public void test_jsonb_2() {
        long millis = 1543377121000L;
        Date date = new Date(millis);

        byte[] jsonbBytes = JSONB.toBytes(date);
        Date date2 = JSONB.parseObject(jsonbBytes, Date.class);
        assertEquals(date, date2);
    }

    @Test
    public void test_jsonb_2_map() {
        long millis = 1543377121000L;
        Date date = new Date(millis);

        Map map = new HashMap();
        map.put("date", date);

        byte[] jsonbBytes = JSONB.toBytes(map);
        Map map2 = JSONB.parseObject(jsonbBytes, Map.class);
        assertEquals(date, map2.get("date"));
    }

    @Test
    public void test_1() {
        Date date = new Date(253397865600000L);
        byte[] bytes = JSONB.toBytes(date, JSONWriter.Feature.WriteClassName);
        Date date2 = (Date) JSONB.parse(bytes);
        assertEquals(date, date2);
    }
}
