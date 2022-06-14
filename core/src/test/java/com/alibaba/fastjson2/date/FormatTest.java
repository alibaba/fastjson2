package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FormatTest {
    @Test
    public void test() {
        LocalDate date = LocalDate.of(2017, 12, 13);
        LocalDateTime dateTime = LocalDateTime.of(2017, 1, 2, 12, 13, 14);

        Bean bean = new Bean();
        bean.date = date;
        bean.dateTime = dateTime;

        JSON.mixIn(LocalDateTime.class, LocalDateTimeMixin.class);
        assertEquals("{\"date\":\"2017-12-13\",\"dateTime\":\"2017-01-02 12:13:14\"}", JSON.toJSONString(bean));

        // clear cache
        JSON.mixIn(LocalDateTime.class, null);
        JSON.mixIn(Bean.class, null);

        assertEquals("{\"date\":\"2017-12-13\",\"dateTime\":\"2017-01-02 12:13:14\"}", JSON.toJSONString(bean));

        JSON.mixIn(LocalDateTime.class, null);
        JSON.mixIn(Bean.class, null);

        JSON.mixIn(LocalDate.class, LocalDateMixin.class);
        assertEquals("\"2017-12-13 00:00:00\"", JSON.toJSONString(date));
        JSON.mixIn(LocalDate.class, null);
        assertEquals("\"2017-12-13\"", JSON.toJSONString(date));

        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean.class).date);
    }

    @Test
    public void testLocalDateTime1() {
        String str = "\"20220613T121317Z\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            LocalDateTime localDateTime = jsonReader.readLocalDateTime();
            assertEquals(2022, localDateTime.getYear());
            assertEquals(6, localDateTime.getMonthValue());
            assertEquals(13, localDateTime.getDayOfMonth());
            assertEquals(12, localDateTime.getHour());
            assertEquals(13, localDateTime.getMinute());
            assertEquals(17, localDateTime.getSecond());
        }
    }

    @Test
    public void testLocalDateTime2() {
        String str = "\"2022-06-13T12:13:17+00:00\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            LocalDateTime localDateTime = jsonReader.readLocalDateTime();
            assertEquals(2022, localDateTime.getYear());
            assertEquals(6, localDateTime.getMonthValue());
            assertEquals(13, localDateTime.getDayOfMonth());
            assertEquals(12, localDateTime.getHour());
            assertEquals(13, localDateTime.getMinute());
            assertEquals(17, localDateTime.getSecond());
        }
    }

    @Test
    public void testLocalDateTime3() {
        String str = "\"2022-06-13T12:13:17Z\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            LocalDateTime localDateTime = jsonReader.readLocalDateTime();
            assertEquals(2022, localDateTime.getYear());
            assertEquals(6, localDateTime.getMonthValue());
            assertEquals(13, localDateTime.getDayOfMonth());
            assertEquals(12, localDateTime.getHour());
            assertEquals(13, localDateTime.getMinute());
            assertEquals(17, localDateTime.getSecond());
        }
    }

    @Test
    public void testZonedDateTime1() {
        String str = "\"20220613T121317Z\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            ZonedDateTime zdt = jsonReader.readZonedDateTime();
            assertEquals(2022, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(12, zdt.getHour());
            assertEquals(13, zdt.getMinute());
            assertEquals(17, zdt.getSecond());
        }
    }

    @Test
    public void testZonedDateTime2() {
        String str = "\"2022-06-13T12:13:17+00:00\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            ZonedDateTime zdt = jsonReader.readZonedDateTime();
            assertEquals(2022, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(12, zdt.getHour());
            assertEquals(13, zdt.getMinute());
            assertEquals(17, zdt.getSecond());
        }
    }

    @Test
    public void testZonedDateTime3() {
        String str = "\"2022-06-13T12:13:17Z\"";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            ZonedDateTime zdt = jsonReader.readZonedDateTime();
            assertEquals(2022, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(12, zdt.getHour());
            assertEquals(13, zdt.getMinute());
            assertEquals(17, zdt.getSecond());
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
