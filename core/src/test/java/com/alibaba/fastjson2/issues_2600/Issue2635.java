package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2635 {
    @Test
    public void test() {
        String str1 = "\"May 28, 2024 12:10:10 PM\"";
        Date date = JSON.parseObject(str1, Date.class);
        String expected = "\"2024-05-28 12:10:10\"";
        assertEquals(expected, JSON.toJSONString(date));

        Date date1 = JSON.parseObject(str1.toCharArray(), Date.class);
        assertEquals(expected, JSON.toJSONString(date1));

        Date date2 = JSON.parseObject(str1.getBytes(StandardCharsets.UTF_8), Date.class);
        assertEquals(expected, JSON.toJSONString(date2));
    }

    @Test
    public void test1() {
        String str1 = "\"Apr 3, 2024, 2:13:04 PM\"";
        String expected = "\"2024-04-03 14:13:04\"";

        Date date = JSON.parseObject(str1, Date.class);
        assertEquals(expected, JSON.toJSONString(date));

        Date date1 = JSON.parseObject(str1.toCharArray(), Date.class);
        assertEquals(expected, JSON.toJSONString(date1));

        Date date2 = JSON.parseObject(str1.getBytes(StandardCharsets.UTF_8), Date.class);
        assertEquals(expected, JSON.toJSONString(date2));
    }

    @Test
    public void test2() {
        String str1 = "\"Apr 13, 2024, 2:13:04 PM\"";
        String expected = "\"2024-04-13 14:13:04\"";

        Date date = JSON.parseObject(str1, Date.class);
        assertEquals(expected, JSON.toJSONString(date));

        Date date1 = JSON.parseObject(str1.toCharArray(), Date.class);
        assertEquals(expected, JSON.toJSONString(date1));

        Date date2 = JSON.parseObject(str1.getBytes(StandardCharsets.UTF_8), Date.class);
        assertEquals(expected, JSON.toJSONString(date2));
    }

    @Test
    public void test3() {
        String str1 = "\"Apr 3, 2024, 11:13:04 PM\"";
        String expected = "\"2024-04-03 23:13:04\"";

        Date date = JSON.parseObject(str1, Date.class);
        assertEquals(expected, JSON.toJSONString(date));

        Date date1 = JSON.parseObject(str1.toCharArray(), Date.class);
        assertEquals(expected, JSON.toJSONString(date1));

        Date date2 = JSON.parseObject(str1.getBytes(StandardCharsets.UTF_8), Date.class);
        assertEquals(expected, JSON.toJSONString(date2));
    }

    @Test
    public void test4() {
        String str1 = "\"Apr 12, 2024, 11:13:04 PM\"";
        String expected = "\"2024-04-12 23:13:04\"";

        Date date = JSON.parseObject(str1, Date.class);
        assertEquals(expected, JSON.toJSONString(date));

        Date date1 = JSON.parseObject(str1.toCharArray(), Date.class);
        assertEquals(expected, JSON.toJSONString(date1));

        Date date2 = JSON.parseObject(str1.getBytes(StandardCharsets.UTF_8), Date.class);
        assertEquals(expected, JSON.toJSONString(date2));
    }
}
