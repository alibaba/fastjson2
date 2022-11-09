package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.util.JDKUtils.*;

public class DateWrite {
    static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZONE_ID_SHANGHAI);

    static final ThreadLocal<SimpleDateFormat> formatThreadLocal = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    );

    static Date[] dates = new Date[12];

    static {
        String[] strings = {
                "2001-07-01 01:02:03", "2011-06-02 09:02:03",
                "2021-11-03 02:02:03", "2001-11-14 07:02:03",
                "2002-10-07 16:02:03", "2003-09-12 15:02:03",
                "2006-08-16 02:02:03", "2002-01-30 22:02:03",
                "2009-02-27 01:02:03", "2011-04-26 11:02:03",
                "2012-06-23 01:02:03", "2022-02-18 09:02:03"
        };

        for (int i = 0; i < strings.length; i++) {
            LocalDateTime ldt = LocalDateTime.parse(strings[i], formatter);
            long millis = ldt.atZone(ZONE_ID_SHANGHAI).toInstant().toEpochMilli();
            dates[i] = new Date(millis);
        }
    }

    @Benchmark
    public void dateTimeFormatter(Blackhole bh) {
        for (int i = 0; i < dates.length; i++) {
            Date date = dates[i];
            bh.consume(
                    formatter.format(
                            date.toInstant()
                    )
            );
        }
    }

    @Benchmark
    public void simpleDateFormat(Blackhole bh) throws Exception {
        for (int i = 0; i < dates.length; i++) {
            Date date = dates[i];
            SimpleDateFormat format = formatThreadLocal.get();
            bh.consume(
                    format.format(date)
            );
        }
    }

    @Benchmark
    public void formatYYYYMMDDHHMMSS19(Blackhole bh) throws Throwable {
        for (int i = 0; i < dates.length; i++) {
            Date date = dates[i];
            formatYYYYMMDDHHMMSS19(ZONE_ID_SHANGHAI, date);
        }
    }

    static String formatYYYYMMDDHHMMSS19(ZoneId zoneId, Date date) throws Throwable {
        long millis = date.getTime();

        final int SECONDS_PER_DAY = 60 * 60 * 24;

        long epochSecond = Math.floorDiv(millis, 1000L);
        int offsetTotalSeconds;
        if (zoneId == IOUtils.SHANGHAI_ZONE_ID || zoneId.getRules() == IOUtils.SHANGHAI_ZONE_RULES) {
            offsetTotalSeconds = IOUtils.getShanghaiZoneOffsetTotalSeconds(epochSecond);
        } else {
            Instant instant = Instant.ofEpochMilli(millis);
            offsetTotalSeconds = zoneId.getRules().getOffset(instant).getTotalSeconds();
        }

        long localSecond = epochSecond + offsetTotalSeconds;
        long localEpochDay = Math.floorDiv(localSecond, (long) SECONDS_PER_DAY);
        int secsOfDay = (int) Math.floorMod(localSecond, (long) SECONDS_PER_DAY);
        int year, month, dayOfMonth;
        {
            final int DAYS_PER_CYCLE = 146097;
            final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

            long zeroDay = localEpochDay + DAYS_0000_TO_1970;
            // find the march-based year
            zeroDay -= 60;  // adjust to 0000-03-01 so leap day is at end of four year cycle
            long adjust = 0;
            if (zeroDay < 0) {
                // adjust negative years to positive for calculation
                long adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1;
                adjust = adjustCycles * 400;
                zeroDay += -adjustCycles * DAYS_PER_CYCLE;
            }
            long yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE;
            long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
            if (doyEst < 0) {
                // fix estimate
                yearEst--;
                doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
            }
            yearEst += adjust;  // reset any negative year
            int marchDoy0 = (int) doyEst;

            // convert march-based values back to january-based
            int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
            month = (marchMonth0 + 2) % 12 + 1;
            dayOfMonth = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
            yearEst += marchMonth0 / 10;

            // check year now we are certain it is correct
            if (yearEst < Year.MIN_VALUE || yearEst > Year.MAX_VALUE) {
                throw new DateTimeException("Invalid year " + yearEst);
            }

            year = (int) yearEst;
        }

        int hour, minute, second;
        {
            final int MINUTES_PER_HOUR = 60;
            final int SECONDS_PER_MINUTE = 60;
            final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

            long secondOfDay = secsOfDay;
            if (secondOfDay < 0 || secondOfDay > 86399) {
                throw new DateTimeException("Invalid secondOfDay " + secondOfDay);
            }
            int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
            secondOfDay -= hours * SECONDS_PER_HOUR;
            int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
            secondOfDay -= minutes * SECONDS_PER_MINUTE;

            hour = hours;
            minute = minutes;
            second = (int) secondOfDay;
        }

        if (JDKUtils.STRING_CREATOR_JDK8 != null) {
            char[] chars = new char[19];

            chars[0] = (char) (year / 1000 + '0');
            chars[1] = (char) ((year / 100) % 10 + '0');
            chars[2] = (char) ((year / 10) % 10 + '0');
            chars[3] = (char) (year % 10 + '0');
            chars[4] = '-';
            chars[5] = (char) (month / 10 + '0');
            chars[6] = (char) (month % 10 + '0');
            chars[7] = '-';
            chars[8] = (char) (dayOfMonth / 10 + '0');
            chars[9] = (char) (dayOfMonth % 10 + '0');
            chars[10] = ' ';
            chars[11] = (char) (hour / 10 + '0');
            chars[12] = (char) (hour % 10 + '0');
            chars[13] = ':';
            chars[14] = (char) (minute / 10 + '0');
            chars[15] = (char) (minute % 10 + '0');
            chars[16] = ':';
            chars[17] = (char) (second / 10 + '0');
            chars[18] = (char) (second % 10 + '0');

            return STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
        } else {
            byte[] bytes = new byte[19];

            bytes[0] = (byte) (year / 1000 + '0');
            bytes[1] = (byte) ((year / 100) % 10 + '0');
            bytes[2] = (byte) ((year / 10) % 10 + '0');
            bytes[3] = (byte) (year % 10 + '0');
            bytes[4] = '-';
            bytes[5] = (byte) (month / 10 + '0');
            bytes[6] = (byte) (month % 10 + '0');
            bytes[7] = '-';
            bytes[8] = (byte) (dayOfMonth / 10 + '0');
            bytes[9] = (byte) (dayOfMonth % 10 + '0');
            bytes[10] = ' ';
            bytes[11] = (byte) (hour / 10 + '0');
            bytes[12] = (byte) (hour % 10 + '0');
            bytes[13] = ':';
            bytes[14] = (byte) (minute / 10 + '0');
            bytes[15] = (byte) (minute % 10 + '0');
            bytes[16] = ':';
            bytes[17] = (byte) (second / 10 + '0');
            bytes[18] = (byte) (second % 10 + '0');

            if (STRING_CREATOR_JDK11 != null) {
                return STRING_CREATOR_JDK11.apply(bytes, LATIN1);
            } else {
                return new String(bytes, 0, bytes.length);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(DateWrite.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
