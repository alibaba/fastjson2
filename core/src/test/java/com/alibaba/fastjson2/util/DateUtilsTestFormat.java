package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import static com.alibaba.fastjson2.util.DateUtils.DEFAULT_ZONE_ID;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTestFormat {
    Locale locale;
    @BeforeEach
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

    @Test
    public void autoCase() {
        Bean bean = JSONObject.of("date", "23/06/2012 12:13:14").to(Bean.class);
        ZonedDateTime zdt = bean.date.toInstant().atZone(DEFAULT_ZONE_ID);
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
        long epochMilli = ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();

        Bean bean = new Bean();
        bean.date = new Date(epochMilli);

        BeanTo beanTo = JSON.copyTo(bean, BeanTo.class);
        assertEquals(str, beanTo.date);

        Bean bean1 = JSON.copyTo(beanTo, Bean.class);
        assertEquals(bean.date, bean1.date);

        JSONPath path = JSONPath.of("$.date");

        Bean bean2 = new Bean();
        path.set(bean2, str);
        assertEquals(bean.date, bean2.date);

        Bean bean3 = JSONObject.of("date", str).toJavaObject(Bean.class);
        assertEquals(bean.date, bean3.date);
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
        assertSame(
                ZoneId.of("Asia/Shanghai").getRules(),
                ZoneId.of("Asia/Shanghai").getRules()
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String str = "23/06/2012 12:13:14";
        LocalDateTime ldt = LocalDateTime.parse(str, formatter);
        long epochMilli = ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();

        Date date = new Date(epochMilli);

        assertEquals(epochMilli, DateUtils.millis(ldt));
        assertEquals(epochMilli, DateUtils.millis(ldt, DEFAULT_ZONE_ID));

        assertNull(DateUtils.format((Date) null));
        assertNull(DateUtils.formatYMDHMS19((Date) null));
        assertNull(DateUtils.format((Date) null, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23 12:13:14", DateUtils.format(date));
        assertEquals("2012-06-23 12:13:14", DateUtils.format(date, null));
        assertEquals("2012-06-23 12:13:14", DateUtils.formatYMDHMS19(date));

        assertEquals("2012-06-23 12:13:14", DateUtils.format(epochMilli));
        assertEquals("2012-06-23 12:13:14", DateUtils.format(date, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23T12:13:14", DateUtils.format(date, "yyyy-MM-ddTHH:mm:ss"));
        assertEquals("2012-06-23T12:13:14", DateUtils.format(date, "yyyy-MM-dd'T'HH:mm:ss"));
        assertEquals("23.06.2012 12:13:14", DateUtils.format(date, "dd.MM.yyyy HH:mm:ss"));
        assertEquals("2012-06-23", DateUtils.format(date, "yyyy-MM-dd"));
        assertEquals("2012/06/23", DateUtils.format(date, "yyyy/MM/dd"));
        assertEquals("23.06.2012", DateUtils.format(date, "dd.MM.yyyy"));
        assertEquals("2012-6-23", DateUtils.format(date, "yyyy-M-dd"));
        assertEquals("2012-Jun-23", DateUtils.format(date, "yyyy-MMM-dd"));
        assertEquals("2012-06-23", DateUtils.format(date, "yyyy-MM-dd"));
        assertEquals("2012-06-23", DateUtils.formatYMD10(date));
        assertEquals("20120623", DateUtils.formatYMD8(date));

        assertEquals("2012-06-23", DateUtils.format(2012, 6, 23));
        assertEquals("2012-06-23 12:13:14", DateUtils.format(2012, 6, 23, 12, 13, 14));
    }

    @Test
    public void format1_zdt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String str = "23/06/2012 12:13:14";
        LocalDateTime ldt = LocalDateTime.parse(str, formatter);
        ZonedDateTime zdt = ldt.atZone(DEFAULT_ZONE_ID);

        assertNull(DateUtils.formatYMDHMS19((Date) null));
        assertNull(DateUtils.format((ZonedDateTime) null, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23 12:13:14", DateUtils.formatYMDHMS19(zdt));

        assertEquals("2012-06-23 12:13:14", DateUtils.format(zdt, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23T12:13:14", DateUtils.format(zdt, "yyyy-MM-ddTHH:mm:ss"));
        assertEquals("2012-06-23T12:13:14", DateUtils.format(zdt, "yyyy-MM-dd'T'HH:mm:ss"));
        assertEquals("2012-06-23", DateUtils.format(zdt, "yyyy-MM-dd"));
        assertEquals("2012/06/23", DateUtils.format(zdt, "yyyy/MM/dd"));
        assertEquals("23.06.2012", DateUtils.format(zdt, "dd.MM.yyyy"));
        assertEquals("2012-6-23", DateUtils.format(zdt, "yyyy-M-dd"));
        assertEquals("2012-Jun-23", DateUtils.format(zdt, "yyyy-MMM-dd"));
    }

    @Test
    public void format1_ldt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String str = "23/06/2012 12:13:14";
        LocalDateTime ldt = LocalDateTime.parse(str, formatter);

        assertNull(DateUtils.formatYMDHMS19((Date) null));
        assertNull(DateUtils.format((LocalDateTime) null, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23 12:13:14", DateUtils.formatYMDHMS19(ldt));

        assertEquals("2012-06-23 12:13:14", DateUtils.format(ldt, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23T12:13:14", DateUtils.format(ldt, "yyyy-MM-ddTHH:mm:ss"));
        assertEquals("2012-06-23T12:13:14", DateUtils.format(ldt, "yyyy-MM-dd'T'HH:mm:ss"));
        assertEquals("2012-06-23", DateUtils.format(ldt, "yyyy-MM-dd"));
        assertEquals("2012/06/23", DateUtils.format(ldt, "yyyy/MM/dd"));
        assertEquals("23.06.2012", DateUtils.format(ldt, "dd.MM.yyyy"));
        assertEquals("2012-6-23", DateUtils.format(ldt, "yyyy-M-dd"));
        assertEquals("2012-Jun-23", DateUtils.format(ldt, "yyyy-MMM-dd"));
    }

    @Test
    public void format1_localDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String str = "23/06/2012 12:13:14";
        LocalDateTime ldt = LocalDateTime.parse(str, formatter);
        LocalDate localDate = ldt.toLocalDate();

        assertNull(DateUtils.formatYMDHMS19((LocalDate) null));
        assertNull(DateUtils.format((LocalDate) null, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23 00:00:00", DateUtils.formatYMDHMS19(localDate));

        assertEquals("2012-06-23 00:00:00", DateUtils.format(localDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2012-06-23T00:00:00", DateUtils.format(localDate, "yyyy-MM-ddTHH:mm:ss"));
        assertEquals("2012-06-23T00:00:00", DateUtils.format(localDate, "yyyy-MM-dd'T'HH:mm:ss"));
        assertEquals("2012-06-23", DateUtils.format(localDate, "yyyy-MM-dd"));
        assertEquals("2012/06/23", DateUtils.format(localDate, "yyyy/MM/dd"));
        assertEquals("23.06.2012", DateUtils.format(localDate, "dd.MM.yyyy"));
        assertEquals("2012-6-23", DateUtils.format(localDate, "yyyy-M-dd"));
        assertEquals("2012-Jun-23", DateUtils.format(localDate, "yyyy-MMM-dd"));
    }

    @Test
    public void utcSeconds() {
        LocalDateTime ldt = LocalDateTime.of(2012, 6, 23, 12, 13, 14);
        long utcSeconds = DateUtils.utcSeconds(
                ldt.getYear(),
                ldt.getMonthValue(),
                ldt.getDayOfMonth(),
                ldt.getHour(),
                ldt.getMinute(),
                ldt.getSecond()
        );
        long utcMillis = utcSeconds * 1000;
        assertEquals(
                ldt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
                utcMillis
        );
    }

    @Test
    public void formatYMD8() {
        assertNull(DateUtils.formatYMD8((Date) null));
        assertNull(DateUtils.formatYMD8((LocalDate) null));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        long epochDay = LocalDate.now().toEpochDay();
        long start = epochDay - 1000;
        long end = epochDay + 1000;
        for (long i = start; i <= end; i++) {
            LocalDate localDate = LocalDate.ofEpochDay(i);
            long epochMilli = localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
            Date date = new Date(epochMilli);

            String result = localDate.format(formatter);
            assertEquals(result, DateUtils.formatYMD8(date));
            assertEquals(result, DateUtils.formatYMD8(localDate));
            assertEquals(result, DateUtils.formatYMD8(date.getTime(), DEFAULT_ZONE_ID));
        }
    }

    @Test
    public void formatYMD8_1() {
        assertNull(DateUtils.formatYMD8((Date) null));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        long epochDay = LocalDate.of(1900, 1, 1).toEpochDay();
        long start = epochDay - 1000;
        long end = epochDay + 1000;
        for (long i = start; i <= end; i++) {
            LocalDate localDate = LocalDate.ofEpochDay(i);
            long epochMilli = localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
            Date date = new Date(epochMilli);

            String result = localDate.format(formatter);
            assertEquals(result, DateUtils.formatYMD8(date));
            assertEquals(result, DateUtils.formatYMD8(localDate));
            assertEquals(result, DateUtils.formatYMD8(date.getTime(), DEFAULT_ZONE_ID));
        }
    }

    @Test
    public void formatYMD10() {
        assertNull(DateUtils.formatYMD10((Date) null));
        assertNull(DateUtils.formatYMD10((LocalDate) null));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        long epochDay = LocalDate.now().toEpochDay();
        long start = epochDay - 1000;
        long end = epochDay + 1000;
        for (long i = start; i <= end; i++) {
            LocalDate localDate = LocalDate.ofEpochDay(i);
            long epochMilli = localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
            Date date = new Date(epochMilli);

            String result = localDate.format(formatter);
            assertEquals(result, DateUtils.formatYMD10(localDate));
            assertEquals(result, DateUtils.formatYMD10(date));
            assertEquals(result, DateUtils.formatYMD10(date.getTime(), DEFAULT_ZONE_ID));
        }
    }

    @Test
    public void formatYMD10_1() {
        assertNull(DateUtils.formatYMD10((Date) null));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        long epochDay = LocalDate.of(1100, 1, 1).toEpochDay();
        long start = epochDay - 1000;
        long end = epochDay + 1000;
        for (long i = start; i <= end; i++) {
            LocalDate localDate = LocalDate.ofEpochDay(i);
            long epochMilli = localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
            Date date = new Date(epochMilli);

            String result = localDate.format(formatter);
            assertEquals(result, DateUtils.formatYMD10(localDate));
            assertEquals(result, DateUtils.formatYMD10(date));
            assertEquals(result, DateUtils.formatYMD10(date.getTime(), DEFAULT_ZONE_ID));
        }
    }
}
