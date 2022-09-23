package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateParse {
    static final String[] dates = {
            "2001-07-01 01:02:03", "2011-06-02 09:02:03",
            "2021-11-03 02:02:03", "2001-11-14 07:02:03",
            "2002-10-07 16:02:03", "2003-09-12 15:02:03",
            "2006-08-16 02:02:03", "2002-01-30 22:02:03",
            "2009-02-27 01:02:03", "2011-04-26 11:02:03",
            "2012-06-23 01:02:03", "2022-02-18 09:02:03"
    };

    static ThreadLocal<SimpleDateFormat> formatThreadLocal = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    );
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    static final ZoneId SHANGHAI_ZONE_ID = "Asia/Shanghai".equals(DEFAULT_ZONE_ID.getId()) ? DEFAULT_ZONE_ID : ZoneId.of("Asia/Shanghai");
    static final ZoneOffset SHANGHAI_ZONE_OFFSET = SHANGHAI_ZONE_ID
            .getRules()
            .getOffset(
                    LocalDateTime.of(
                            LocalDate.of(1992, 1, 1), LocalTime.MIN
                    )
            );

    @Benchmark
    public void simpleDateFormat(Blackhole bh) throws Exception {
        for (int i = 0; i < dates.length; i++) {
            String str = dates[i];
            SimpleDateFormat format = formatThreadLocal.get();
            bh.consume(
                    format.parse(str)
            );
        }
    }

    @Benchmark
    public void dateTimeFormatter(Blackhole bh) throws Exception {
        for (int i = 0; i < dates.length; i++) {
            String str = dates[i];
            LocalDateTime ldt = LocalDateTime.parse(str, formatter);
            ZoneOffset offset = DEFAULT_ZONE_ID.getRules().getOffset(ldt);
            long millis = ldt.toInstant(offset).toEpochMilli();
            bh.consume(new Date(millis));
        }
    }

    @Benchmark
    public void localDateTimeParse(Blackhole bh) throws Exception {
        for (int i = 0; i < dates.length; i++) {
            String str = dates[i];
            LocalDateTime ldt = LocalDateTime.parse(str, formatter);
            ZoneOffset offset;
            if (DEFAULT_ZONE_ID == SHANGHAI_ZONE_ID && ldt.getYear() >= 1992) {
                offset = SHANGHAI_ZONE_OFFSET;
            } else {
                offset = DEFAULT_ZONE_ID.getRules().getOffset(ldt);
            }
            long millis = ldt.toInstant(offset).toEpochMilli();
            bh.consume(new Date(millis));
        }
    }

    @Benchmark
    public void parseYYYYMMDDHHMMSS19(Blackhole bh) throws Exception {
        for (int i = 0; i < dates.length; i++) {
            String str = dates[i];
            bh.consume(parseYYYYMMDDHHMMSS19(str));
        }
    }

    public static Date parseYYYYMMDDHHMMSS19(String str) {
        if (str == null || str.length() != 19) {
            throw new DateTimeParseException("not support input ", str, 0);
        }

        char c0 = str.charAt(0);
        char c1 = str.charAt(1);
        char c2 = str.charAt(2);
        char c3 = str.charAt(3);
        char c4 = str.charAt(4);
        char c5 = str.charAt(5);
        char c6 = str.charAt(6);
        char c7 = str.charAt(7);
        char c8 = str.charAt(8);
        char c9 = str.charAt(9);
        char c10 = str.charAt(10);
        char c11 = str.charAt(11);
        char c12 = str.charAt(12);
        char c13 = str.charAt(13);
        char c14 = str.charAt(14);
        char c15 = str.charAt(15);
        char c16 = str.charAt(16);
        char c17 = str.charAt(17);
        char c18 = str.charAt(18);

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1;
        if (c4 == '-' && c7 == '-' && c10 == ' ' && c13 == ':' && c16 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;
        } else {
            throw new DateTimeParseException("not support input ", str, 0);
        }

        int year;
        if (y0 >= '0' && y0 <= '9'
                && y1 >= '0' && y1 <= '9'
                && y2 >= '0' && y2 <= '9'
                && y3 >= '0' && y3 <= '9'
        ) {
            year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
        } else {
            throw new DateTimeParseException("not support input ", str, 0);
        }

        int month;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
        ) {
            month = (m0 - '0') * 10 + (m1 - '0');
        } else {
            throw new DateTimeParseException("not support input ", str, 0);
        }

        int dom;
        if (d0 >= '0' && d0 <= '9'
                && d1 >= '0' && d1 <= '9'
        ) {
            dom = (d0 - '0') * 10 + (d1 - '0');
        } else {
            throw new DateTimeParseException("not support input ", str, 0);
        }

        int hour;
        if (h0 >= '0' && h0 <= '9'
                && h1 >= '0' && h1 <= '9'
        ) {
            hour = (h0 - '0') * 10 + (h1 - '0');
        } else {
            throw new DateTimeParseException("not support input ", str, 0);
        }

        int minute;
        if (i0 >= '0' && i0 <= '9'
                && i1 >= '0' && i1 <= '9'
        ) {
            minute = (i0 - '0') * 10 + (i1 - '0');
        } else {
            throw new DateTimeParseException("not support input ", str, 0);
        }

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
        } else {
            throw new DateTimeParseException("not support input ", str, 0);
        }

        long millis;
        if (DEFAULT_ZONE_ID == SHANGHAI_ZONE_ID || DEFAULT_ZONE_ID.getRules() == IOUtils.SHANGHAI_ZONE_RULES) {
            long seconds = DateUtils.utcSeconds(year, month, dom, hour, minute, second);
            int zoneOffsetTotalSeconds = IOUtils.getShanghaiZoneOffsetTotalSeconds(seconds);
            seconds -= zoneOffsetTotalSeconds;
            millis = seconds * 1000L;
        } else {
            LocalDate localDate = LocalDate.of(year, month, dom);
            LocalTime localTime = LocalTime.of(hour, minute, second, 0);
            LocalDateTime ldt = LocalDateTime.of(localDate, localTime);
            ZoneOffset offset = DEFAULT_ZONE_ID.getRules().getOffset(ldt);
            millis = ldt.toEpochSecond(offset) * 1000;
        }

        return new Date(millis);
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(DateParse.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
