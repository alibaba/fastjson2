package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue997 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"date\":\"Dec 7, 2022 10:55:19 AM\"}", Bean.class);
        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
        assertEquals(2022, zdt.getYear());
        assertEquals(12, zdt.getMonthValue());
        assertEquals(7, zdt.getDayOfMonth());
        assertEquals(10, zdt.getHour());
        assertEquals(55, zdt.getMinute());
        assertEquals(19, zdt.getSecond());
    }

    @Test
    public void testMonth() {
        String[] months = {
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec"
        };

        for (int i = 0; i < months.length; i++) {
            String month = months[i];

            for (int day = 1; day < 28; day++) {
                for (int hour = 1; hour <= 9; hour++) {
                    String str = "{\"date\":\"" + month + " " + day + ", 2022 0" + hour + ":55:19 AM\"}";

                    {
                        Bean bean = JSONReader.of(str.getBytes()).read(Bean.class);
                        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.getYear());
                        assertEquals(i + 1, zdt.getMonthValue());
                        assertEquals(day, zdt.getDayOfMonth());
                        assertEquals(hour, zdt.getHour());
                        assertEquals(55, zdt.getMinute());
                        assertEquals(19, zdt.getSecond());
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.getYear());
                        assertEquals(i + 1, zdt.getMonthValue());
                        assertEquals(day, zdt.getDayOfMonth());
                        assertEquals(hour, zdt.getHour());
                        assertEquals(55, zdt.getMinute());
                        assertEquals(19, zdt.getSecond());
                    }
                }

                for (int hour = 0; hour <= 1; hour++) {
                    String str = "{\"date\":\"" + month + " " + day + ", 2022 1" + hour + ":55:19 AM\"}";
                    {
                        Bean bean = JSONReader.of(str.getBytes()).read(Bean.class);
                        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.getYear());
                        assertEquals(i + 1, zdt.getMonthValue());
                        assertEquals(day, zdt.getDayOfMonth());
                        assertEquals(hour + 10, zdt.getHour());
                        assertEquals(55, zdt.getMinute());
                        assertEquals(19, zdt.getSecond());
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.getYear());
                        assertEquals(i + 1, zdt.getMonthValue());
                        assertEquals(day, zdt.getDayOfMonth());
                        assertEquals(hour + 10, zdt.getHour());
                        assertEquals(55, zdt.getMinute());
                        assertEquals(19, zdt.getSecond());
                    }
                }

                for (int hour = 0; hour <= 9; hour++) {
                    String str = "{\"date\":\"" + month + " " + day + ", 2022 0" + hour + ":55:19 PM\"}";
                    {
                        Bean bean = JSONReader.of(str.getBytes()).read(Bean.class);
                        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.getYear());
                        assertEquals(i + 1, zdt.getMonthValue());
                        assertEquals(day, zdt.getDayOfMonth());
                        assertEquals(hour + 12, zdt.getHour());
                        assertEquals(55, zdt.getMinute());
                        assertEquals(19, zdt.getSecond());
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.getYear());
                        assertEquals(i + 1, zdt.getMonthValue());
                        assertEquals(day, zdt.getDayOfMonth());
                        assertEquals(hour + 12, zdt.getHour());
                        assertEquals(55, zdt.getMinute());
                        assertEquals(19, zdt.getSecond());
                    }
                }

                for (int hour = 0; hour <= 1; hour++) {
                    String str = "{\"date\":\"" + month + " " + day + ", 2022 1" + hour + ":55:19 PM\"}";
                    {
                        Bean bean = JSONReader.of(str.getBytes()).read(Bean.class);
                        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.getYear());
                        assertEquals(i + 1, zdt.getMonthValue());
                        assertEquals(day, zdt.getDayOfMonth());
                        assertEquals(hour + 22, zdt.getHour());
                        assertEquals(55, zdt.getMinute());
                        assertEquals(19, zdt.getSecond());
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.getYear());
                        assertEquals(i + 1, zdt.getMonthValue());
                        assertEquals(day, zdt.getDayOfMonth());
                        assertEquals(hour + 22, zdt.getHour());
                        assertEquals(55, zdt.getMinute());
                        assertEquals(19, zdt.getSecond());
                    }
                }
            }
        }
    }

    public static class Bean {
        public Date date;
    }
}
