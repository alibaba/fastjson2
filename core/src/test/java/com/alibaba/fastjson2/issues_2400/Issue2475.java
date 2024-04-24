package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author hnyyghk
 * @since 2024-04-24
 */
public class Issue2475 {
    @Test
    void testList() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        List<Date> data = new ArrayList<>();
        data.add(date);
        String str = JSON.toJSONString(data, JSONWriter.Feature.WriteClassName);

        List<Date> data1 = JSON.parseObject(str, new TypeReference<List<Date>>() {
        }.getType());
        assertEquals(date.getTime(), data1.iterator().next().getTime());
    }

    @Test
    void testSet() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        Set<Date> data = new HashSet<>();
        data.add(date);
        String str = JSON.toJSONString(data, JSONWriter.Feature.WriteClassName);

        Set<Date> data1 = JSON.parseObject(str, new TypeReference<Set<Date>>() {
        }.getType());
        assertEquals(date.getTime(), data1.iterator().next().getTime());
    }

    @Test
    void testMapKey() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        Map<Date, String> data = new HashMap<>();
        data.put(date, "date");
        String str = JSON.toJSONString(data, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);

        Map<Date, String> data1 = JSON.parseObject(str, new TypeReference<Map<Date, String>>() {
        }.getType());
        assertEquals(date.getTime(), data1.keySet().iterator().next().getTime());
    }

    @Test
    void testMapValue() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        Map<String, Date> data = new HashMap<>();
        data.put("date", date);
        String str = JSON.toJSONString(data, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);

        Map<String, Date> data1 = JSON.parseObject(str, new TypeReference<Map<String, Date>>() {
        }.getType());
        assertEquals(date.getTime(), data1.values().iterator().next().getTime());
    }

    @Test
    public void testBean() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        Map<String, Date> data = new HashMap<>();
        data.put("date", date);
        String str = JSON.toJSONString(data, JSONWriter.Feature.WriteClassName);

        Bean data1 = JSON.parseObject(str, Bean.class);
        assertEquals(date.getTime(), data1.date.getTime());
    }

    public static class Bean {
        public Date date;
    }
}
