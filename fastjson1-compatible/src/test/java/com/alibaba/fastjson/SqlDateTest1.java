package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlDateTest1 {
    @BeforeEach
    public void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = new Locale("zh_CN");
    }

    @Test
    public void test_date() throws Exception {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        assertEquals("1324138987429", JSON.toJSONString(date));
        assertEquals("{\"@type\":\"java.sql.Date\",\"val\":1324138987429}", JSON.toJSONString(date, SerializerFeature.WriteClassName));
        assertEquals(
                1324138987429L,
                ((java.util.Date) JSON.parse(
                        "{\"@type\":\"java.util.Date\",\"val\":1324138987429}", Feature.SupportAutoType
                )).getTime()
        );

        assertEquals("\"2011-12-18 00:23:07\"",
                JSON.toJSONString(date, SerializerFeature.WriteDateUseDateFormat)
        );
        assertEquals("\"2011-12-18 00:23:07.429\"",
                JSON.toJSONStringWithDateFormat(date, "yyyy-MM-dd HH:mm:ss.SSS")
        );
        assertEquals("'2011-12-18 00:23:07.429'",
                JSON.toJSONStringWithDateFormat(
                        date,
                        "yyyy-MM-dd HH:mm:ss.SSS",
                        SerializerFeature.UseSingleQuotes)
        );
    }
}
