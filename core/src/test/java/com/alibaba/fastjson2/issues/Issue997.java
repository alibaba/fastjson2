package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.time.Instant;
import com.alibaba.fastjson2.time.ZoneId;
import com.alibaba.fastjson2.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue997 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"date\":\"Dec 7, 2022 10:55:19 AM\"}", Bean.class);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
        assertEquals(2022, zdt.dateTime.date.year);
        assertEquals(12, zdt.dateTime.date.monthValue);
        assertEquals(7, zdt.dateTime.date.dayOfMonth);
        assertEquals(10, zdt.dateTime.time.hour);
        assertEquals(55, zdt.dateTime.time.minute);
        assertEquals(19, zdt.dateTime.time.second);
    }

    @Test
    public void test1() throws Exception {
        String dateStr = "Dec 17, 2022 10:55:19 AM";
        String json = "{\"date\":\"" + dateStr + "\"}";
        String fmt = "MMM dd, yyyy hh:mm:ss a";
        SimpleDateFormat format = new SimpleDateFormat(fmt);

        Date date = format.parse(dateStr);

        Bean bean = new Bean();
        bean.date = date;
        assertEquals(json, JSON.toJSONString(bean, new JSONWriter.Context(fmt)));

        Bean bean1 = JSON.parseObject(json, Bean.class, new JSONReader.Context(fmt));
        assertEquals(bean.date.getTime(), bean1.date.getTime());

        Bean bean2 = JSON.parseObject(json, Bean.class);
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    @Test
    public void test3() throws Exception {
        String dateStr = "Dec 17, 2022 2:55:19 AM";
        String json = "{\"date\":\"" + dateStr + "\"}";
        String fmt = "MMM dd, yyyy h:mm:ss a";
        SimpleDateFormat format = new SimpleDateFormat(fmt);

        Date date = format.parse(dateStr);

        Bean bean = new Bean();
        bean.date = date;
        assertEquals(json, JSON.toJSONString(bean, new JSONWriter.Context(fmt)));

        Bean bean1 = JSON.parseObject(json, Bean.class, new JSONReader.Context(fmt));
        assertEquals(bean.date.getTime(), bean1.date.getTime());

        Bean bean2 = JSON.parseObject(json, Bean.class);
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    @Test
    public void test2() throws Exception {
        String dateStr = "Dec 7, 2022 2:55:19 AM";
        String json = "{\"date\":\"" + dateStr + "\"}";
        String fmt = "MMM d, yyyy h:mm:ss a";
        SimpleDateFormat format = new SimpleDateFormat(fmt);

        Date date = format.parse(dateStr);

        Bean bean = new Bean();
        bean.date = date;
        assertEquals(json, JSON.toJSONString(bean, new JSONWriter.Context(fmt)));

        Bean bean1 = JSON.parseObject(json, Bean.class, new JSONReader.Context(fmt));
        assertEquals(bean.date.getTime(), bean1.date.getTime());

        Bean bean2 = JSON.parseObject(json.toCharArray(), Bean.class);
        assertEquals(bean.date.getTime(), bean2.date.getTime());

        Bean bean3 = JSON.parseObject(json.getBytes(), Bean.class);
        assertEquals(bean.date.getTime(), bean3.date.getTime());
    }

    @Test
    public void testError() {
        String[] strings = {
                "{\"date\":\"Aec 17, 2022 10:55:19 AM\"}"
        };
        for (String string : strings) {
            assertThrows(Exception.class, () -> JSON.parseObject(string, Bean.class));
            assertThrows(Exception.class, () -> JSONReader.of(string.toCharArray()).read(Bean.class));
            assertThrows(Exception.class, () -> JSONReader.of(string.getBytes()).read(Bean.class));
        }
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
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                }

                for (int hour = 1; hour <= 9; hour++) {
                    String str = "{\"date\":\"" + month + " " + day + ", 2022 " + hour + ":55:19 AM\"}";

                    {
                        Bean bean = JSONReader.of(str.getBytes()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                }

                for (int hour = 0; hour <= 1; hour++) {
                    String str = "{\"date\":\"" + month + " " + day + ", 2022 1" + hour + ":55:19 AM\"}";
                    {
                        Bean bean = JSONReader.of(str.getBytes()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour + 10, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour + 10, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                }

                for (int hour = 0; hour <= 9; hour++) {
                    String str = "{\"date\":\"" + month + " " + day + ", 2022 0" + hour + ":55:19 PM\"}";
                    {
                        Bean bean = JSONReader.of(str.getBytes()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour + 12, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour + 12, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                }

                for (int hour = 0; hour <= 1; hour++) {
                    String str = "{\"date\":\"" + month + " " + day + ", 2022 1" + hour + ":55:19 PM\"}";
                    {
                        Bean bean = JSONReader.of(str.getBytes()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour + 22, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                    {
                        Bean bean = JSONReader.of(str.toCharArray()).read(Bean.class);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(bean.date), ZoneId.SHANGHAI_ZONE_ID);
                        assertEquals(2022, zdt.dateTime.date.year);
                        assertEquals(i + 1, zdt.dateTime.date.monthValue);
                        assertEquals(day, zdt.dateTime.date.dayOfMonth);
                        assertEquals(hour + 22, zdt.dateTime.time.hour);
                        assertEquals(55, zdt.dateTime.time.minute);
                        assertEquals(19, zdt.dateTime.time.second);
                    }
                }
            }
        }
    }

    public static class Bean {
        public Date date;
    }
}
