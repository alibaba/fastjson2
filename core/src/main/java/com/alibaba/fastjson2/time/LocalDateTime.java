package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.alibaba.fastjson2.time.ZoneId.DEFAULT_ZONE_ID;

public final class LocalDateTime {
    public final LocalDate date;
    public final LocalTime time;

    private LocalDateTime(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    public static LocalDateTime ofEpochSecond(long epochSecond, int nanoOfSecond, int offsetTotalSeconds) {
        checkNanoOfSecond(nanoOfSecond);

        final int SECONDS_PER_DAY = 86400;
        long localSecond = epochSecond + offsetTotalSeconds;  // overflow caught later
        long localEpochDay = IOUtils.floorDiv(localSecond, SECONDS_PER_DAY);
        int secsOfDay = (int) IOUtils.floorMod(localSecond, SECONDS_PER_DAY);
        LocalDate date = LocalDate.ofEpochDay(localEpochDay);
        LocalTime time = LocalTime.ofNanoOfDay(secsOfDay * 1000_000_000L + nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(LocalDate date, LocalTime time) {
        if (date == null) {
            throw new NullPointerException("date");
        }

        if (time == null) {
            throw new NullPointerException("time");
        }

        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nano) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second, nano);
        return new LocalDateTime(date, time);
    }

    public Date toDate() {
        return toInstant(DEFAULT_ZONE_ID)
                .toDate();
    }

    public Date toDate(ZoneId zoneId) {
        return toInstant(zoneId)
                .toDate();
    }

    public Timestamp toTimestamp() {
        return new Timestamp(date.year - 1900,
                date.monthValue - 1,
                date.dayOfMonth,
                time.hour,
                time.minute,
                time.second,
                time.nano);
    }

    public Instant toInstant(ZoneId zone) {
        Calendar calendar = Calendar.getInstance(zone.timeZone);
        calendar.set(
                date.year,
                date.monthValue - 1,
                date.dayOfMonth,
                time.hour,
                time.minute,
                time.second
        );
        long seconds = calendar.getTime().getTime() / 1000;

        return Instant.ofEpochSecond(seconds, time.nano);
    }

    public static LocalDateTime ofInstant(Instant instant, ZoneId zone) {
        if (instant == null) {
            throw new NullPointerException("instant");
        }

        if (zone == null) {
            throw new NullPointerException("zone");
        }

        int offset = zone.getOffsetTotalSeconds(instant);
        return ofEpochSecond(instant.epochSecond, instant.nanos, offset);
    }

    public static int checkYear(long value) {
        if (value < -999_999_999 || value > 999_999_999) {
            throw new DateTimeException("Invalid value for year (valid values [-999_999_999, 999_999_999]): " + value);
        }
        return (int) value;
    }

    public static void checkSecondOfDay(long value) {
        if (value < 0 || value > 86399999999999L) {
            throw new DateTimeException("Invalid value for year (valid values [0, 86399999999999]): " + value);
        }
    }

    // NANO_OF_SECOND (0, 999_999_999)
    public static void checkNanoOfSecond(long value) {
        if (value < 0 || value > 999_999_999) {
            throw new DateTimeException("Invalid value for year (valid values [0, 999_999_999]): " + value);
        }
    }

    public static LocalDateTime now() {
        return ZonedDateTime.now(DEFAULT_ZONE_ID).dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalDateTime that = (LocalDateTime) o;
        return (date == that.date) || (date != null && date.equals(that.date))
                && (time == that.time) || (time != null && time.equals(that.time));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{date, time});
    }

    public String toString() {
        return DateUtils.format(this, "yyyy-MM-dd HH:mm:ss");
    }
}
