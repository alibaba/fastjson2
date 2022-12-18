package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateUtilsTestFormat {
    @Test
    public void autoCase() {
        Bean bean = JSONObject.of("date", "23/06/2012 12:13:14").to(Bean.class);
        ZonedDateTime zdt = bean.date.toInstant().atZone(IOUtils.DEFAULT_ZONE_ID);
        assertEquals(23, zdt.getDayOfMonth());
        assertEquals(6, zdt.getMonthValue());
        assertEquals(2012, zdt.getYear());
        assertEquals(12, zdt.getHour());
        assertEquals(13, zdt.getMinute());
        assertEquals(14, zdt.getSecond());
    }

    @Test
    public void format() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String str = "23/06/2012 12:13:14";
        LocalDateTime ldt = LocalDateTime.parse(str, formatter);
        long epochMilli = ldt.atZone(IOUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli();

        Bean bean = new Bean();
        bean.date = new Date(epochMilli);

        BeanTo beanTo = JSON.copyTo(bean, BeanTo.class);
        assertEquals(str, beanTo.date);
    }

    public static class Bean {
        @JSONField(format = "dd/MM/yyyy HH:mm:ss")
        public Date date;
    }

    public static class BeanTo {
        public String date;
    }

    @Test
    public void format1() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String str = "23/06/2012 12:13:14";
        LocalDateTime ldt = LocalDateTime.parse(str, formatter);
        long epochMilli = ldt.atZone(IOUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli();

        Date date = new Date(epochMilli);

        assertNull(DateUtils.format((Date) null));
        assertEquals("2012-06-23 12:13:14", DateUtils.format(date));

        assertEquals("2012-06-23 12:13:14", DateUtils.format(epochMilli));
        assertEquals("2012-06-23 12:13:14", DateUtils.format(date, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23T12:13:14", DateUtils.format(date, "yyyy-MM-ddTHH:mm:ss"));
        assertEquals("2012-06-23T12:13:14", DateUtils.format(date, "yyyy-MM-dd'T'HH:mm:ss"));
        assertEquals("2012-06-23", DateUtils.format(date, "yyyy-MM-dd"));
        assertEquals("2012/06/23", DateUtils.format(date, "yyyy/MM/dd"));
        assertEquals("23.06.2012", DateUtils.format(date, "dd.MM.yyyy"));
        assertEquals("2012-6-23", DateUtils.format(date, "yyyy-M-dd"));

        assertEquals("2012-06-23", DateUtils.format(2012, 6, 23));
        assertEquals("2012-06-23 12:13:14", DateUtils.format(2012, 6, 23, 12, 13, 14));
    }
}
