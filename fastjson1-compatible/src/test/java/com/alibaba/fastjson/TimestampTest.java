package com.alibaba.fastjson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimestampTest {
    @BeforeEach
    public void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_0() throws Exception {
        long millis = 1668216743000L;

        SimpleDateFormat format = new SimpleDateFormat(JSON.DEFFAULT_DATE_FORMAT, JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
        String text = "\"" + format.format(new Date(millis)) + "\"";
        System.out.println(text);
        assertEquals(new Timestamp(millis), JSON.parseObject("" + millis, Timestamp.class));
        assertEquals(new Timestamp(millis), JSON.parseObject("\"" + millis + "\"", Timestamp.class));
        assertEquals(new Timestamp(millis), JSON.parseObject(text, Timestamp.class));

        assertEquals(
                "\"2022-11-12 00:00:00\"",
                JSON.toJSONStringWithDateFormat(
                        JSON.parseObject(text, java.sql.Date.class),
                        JSON.DEFFAULT_DATE_FORMAT
                )
        );
        assertEquals(
                "\"2022-11-12 09:32:23\"",
                JSON.toJSONStringWithDateFormat(
                        JSON.parseObject(text, java.util.Date.class),
                        JSON.DEFFAULT_DATE_FORMAT
                )
        );
    }
}
