package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.time.LocalDate;
import com.alibaba.fastjson2.time.LocalDateTime;
import com.alibaba.fastjson2.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatTest {
    @Test
    public void testLocalDateTime1() {
        String str = "\"20220613T121317Z\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            LocalDateTime localDateTime = jsonReader.readLocalDateTime();
            assertEquals(2022, localDateTime.date.year);
            assertEquals(6, localDateTime.date.monthValue);
            assertEquals(13, localDateTime.date.dayOfMonth);
            assertEquals(12, localDateTime.time.hour);
            assertEquals(13, localDateTime.time.minute);
            assertEquals(17, localDateTime.time.second);
        }
    }

    @Test
    public void testLocalDateTime2() {
        String str = "\"2022-06-13T12:13:17+00:00\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            LocalDateTime localDateTime = jsonReader.readLocalDateTime();
            assertEquals(2022, localDateTime.date.year);
            assertEquals(6, localDateTime.date.monthValue);
            assertEquals(13, localDateTime.date.dayOfMonth);
            assertEquals(20, localDateTime.time.hour);
            assertEquals(13, localDateTime.time.minute);
            assertEquals(17, localDateTime.time.second);
        }
    }

    @Test
    public void testLocalDateTime3() {
        String str = "\"2022-06-13T12:13:17Z\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            LocalDateTime localDateTime = jsonReader.readLocalDateTime();
            assertEquals(2022, localDateTime.date.year);
            assertEquals(6, localDateTime.date.monthValue);
            assertEquals(13, localDateTime.date.dayOfMonth);
            assertEquals(12, localDateTime.time.hour);
            assertEquals(13, localDateTime.time.minute);
            assertEquals(17, localDateTime.time.second);
        }
    }

    @Test
    public void testZonedDateTime1() {
        String str = "\"20220613T121317Z\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            ZonedDateTime zdt = jsonReader.readZonedDateTime();
            assertEquals(2022, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(12, zdt.dateTime.time.hour);
            assertEquals(13, zdt.dateTime.time.minute);
            assertEquals(17, zdt.dateTime.time.second);
        }
    }

    @Test
    public void testZonedDateTime2() {
        String str = "\"2022-06-13T12:13:17+00:00\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            ZonedDateTime zdt = jsonReader.readZonedDateTime();
            assertEquals(2022, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(12, zdt.dateTime.time.hour);
            assertEquals(13, zdt.dateTime.time.minute);
            assertEquals(17, zdt.dateTime.time.second);
        }
    }

    @Test
    public void testZonedDateTime3() {
        String str = "\"2022-06-13T12:13:17Z\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            ZonedDateTime zdt = jsonReader.readZonedDateTime();
            assertEquals(2022, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(12, zdt.dateTime.time.hour);
            assertEquals(13, zdt.dateTime.time.minute);
            assertEquals(17, zdt.dateTime.time.second);
        }
    }

    @JSONType(format = "yyyy-MM-dd HH:mm:ss")
    public static class LocalDateMixin {
    }

    @JSONType(format = "yyyy-MM-dd HH:mm:ss")
    public static class LocalDateTimeMixin {
    }

    public static class Bean {
        public LocalDateTime dateTime;
        public LocalDate date;
    }
}
