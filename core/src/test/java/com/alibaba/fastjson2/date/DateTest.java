package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest {
    static final long millis = DateUtils.parseMillis("2023-03-11T11:33:22Z");

    @Test
    public void test() {
        assertEquals(millis, DateUtils.parseMillis("2023-03-11 19:33:22 CST"));
        assertEquals(
                millis,
                JSON.parseObject("\"2023-03-11 19:33:22 CST\"", Date.class)
                        .getTime()
        );

        assertEquals(
                millis,
                JSON.parseObject("\"2023-03-11T11:33:22Z\"", Date.class)
                        .getTime()
        );

        // ISO8601
        assertEquals(
                millis,
                JSON.parseObject("\"2023-03-11T11:33:22+0000\"", Date.class)
                        .getTime()
        );
    }

    @Test
    public void test1() {
        String str = "Sun Mar 11 19:33:22 CST 2023";
        String json = "\"" + str + "\"";

        assertEquals(
                millis,
                DateUtils.parseMillis(str)
        );

        assertEquals(
                millis,
                JSON.parseObject(json.getBytes(), Date.class)
                        .getTime()
        );

        assertEquals(
                millis,
                JSON.parseObject(json, Date.class)
                        .getTime()
        );
    }

    @Test
    public void test2() {
        long millis = 1678669374000L;
        String str = "Mon Mar 13 09:02:54 CST 2023";
        String json = "\"" + str + "\"";

        assertEquals(
                millis,
                DateUtils.parseMillis(str)
        );

        assertEquals(
                millis,
                JSON.parseObject(json.getBytes(), Date.class)
                        .getTime()
        );

        assertEquals(
                millis,
                JSON.parseObject(json, Date.class)
                        .getTime()
        );
    }

    @Test
    public void cookie() {
        String str = "Saturday, 11-Mar-2023 11:33:22 UTC";
        String json = "\"" + str + "\"";

        assertEquals(
                millis,
                DateUtils.parseMillis(str)
        );

        assertEquals(
                millis,
                JSON.parseObject(json.getBytes(), Date.class)
                        .getTime()
        );

        assertEquals(
                millis,
                JSON.parseObject(json, Date.class)
                        .getTime()
        );
    }

    @Test
    public void cookieUTC() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd-MMM-yyyy HH:mm:ss zzz");
        ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("UTC"));
        zdt = zdt.minusNanos(zdt.getNano());
        for (int i = 0; i < 500; i++) {
            ZonedDateTime zdtI = zdt.plusDays(i);
            long millisI = zdtI.toInstant().toEpochMilli();

            String str = zdtI.format(formatter);
            long millis = DateUtils.parseMillis(str);
            assertEquals(millisI, millis, str);

            String json = "\"" + str + "\"";
            assertEquals(
                    millisI,
                    JSON.parseObject(json.getBytes(), Date.class)
                            .getTime()
            );
        }
    }

    @Test
    public void cookieCST() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd-MMM-yyyy HH:mm:ss zzz");
        ZonedDateTime zdt = ZonedDateTime.now(DateUtils.SHANGHAI_ZONE_ID);
        zdt = zdt.minusNanos(zdt.getNano());
        for (int i = 0; i < 500; i++) {
            ZonedDateTime zdtI = zdt.plusDays(i);
            long millisI = zdtI.toInstant().toEpochMilli();

            String str = zdtI.format(formatter);
            long millis = DateUtils.parseMillis(str);
            assertEquals(millisI, millis, str);

            String json = "\"" + str + "\"";
            assertEquals(
                    millisI,
                    JSON.parseObject(json.getBytes(), Date.class)
                            .getTime()
            );
        }
    }
}
