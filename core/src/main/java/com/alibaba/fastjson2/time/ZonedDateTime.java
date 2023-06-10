package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.util.DateUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public final class ZonedDateTime {
    public final LocalDateTime dateTime;
    public final int offsetSeconds;
    public final ZoneId zone;

    private ZonedDateTime(LocalDateTime dateTime, int offsetSeconds, ZoneId zone) {
        this.dateTime = dateTime;
        this.offsetSeconds = offsetSeconds;
        this.zone = zone;
    }

    public static ZonedDateTime ofInstant(Instant instant, ZoneId zone) {
        return create(instant.epochSecond, instant.nanos, zone);
    }

    public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zone) {
        return ofLocal(localDateTime, zone);
    }

    public static ZonedDateTime of(LocalDate localDate, LocalTime localTime, ZoneId zone) {
        return ofLocal(LocalDateTime.of(localDate, localTime), zone);
    }

    public static ZonedDateTime now(ZoneId zoneId) {
        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        return ofInstant(instant, zoneId);
    }

    public ZonedDateTime minusNanos(long nanos) {
        Calendar calendar = Calendar.getInstance(zone.timeZone);
        calendar.set(
                dateTime.date.year,
                dateTime.date.monthValue - 1,
                dateTime.date.dayOfMonth,
                dateTime.time.hour,
                dateTime.time.minute,
                dateTime.time.second
        );
        long nano = dateTime.time.nano + nanos;
        calendar.set(Calendar.MILLISECOND, (int) nano / 1_000_000);

        long millis = calendar.getTimeInMillis();
        Instant instant = Instant.of(new Date(millis));
        return ofInstant(instant, zone);
    }

    public ZonedDateTime plusDays(long days) {
        Calendar calendar = Calendar.getInstance(zone.timeZone);
        calendar.set(
                dateTime.date.year,
                dateTime.date.monthValue - 1,
                dateTime.date.dayOfMonth + (int) days,
                dateTime.time.hour,
                dateTime.time.minute,
                dateTime.time.second
        );
        calendar.set(Calendar.MILLISECOND, dateTime.time.nano / 1_000_000);

        long millis = calendar.getTimeInMillis();
        Instant instant = Instant.of(new Date(millis));
        return ofInstant(instant, zone);
    }

    public static ZonedDateTime ofLocal(LocalDateTime ldt, ZoneId zone) {
        Instant instant = ldt.toInstant(zone);
        int offset = zone.getOffsetTotalSeconds(instant);
        return new ZonedDateTime(ldt, offset, zone);
    }

    private static ZonedDateTime create(long epochSecond, int nanoOfSecond, ZoneId zone) {
        Instant instant = Instant.ofEpochSecond(epochSecond, nanoOfSecond);  // TODO: rules should be queryable by epochSeconds
        int offset = zone.getOffsetTotalSeconds(instant);
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, offset);
        return new ZonedDateTime(ldt, offset, zone);
    }

    public Instant toInstant() {
        long seconds = toEpochMilli() / 1000;
        return Instant.ofEpochSecond(seconds, dateTime.time.nano);
    }

    public long toEpochSecond() {
        return toEpochMilli() / 1000;
    }

    public long toEpochMilli() {
        LocalDate date = dateTime.date;
        LocalTime time = dateTime.time;
        if (zone == ZoneId.SHANGHAI_ZONE_ID || zone.id.equals(ZoneId.SHANGHAI_ZONE_ID_NAME)) {
            long utcSeconds = DateUtils.utcSeconds(date.year, date.monthValue, date.dayOfMonth, time.hour, time.minute, time.second);
            long SECONDS_1991_09_15_02 = 684900000; // utcMillis(1991, 9, 15, 2, 0, 0);
            int zoneOffsetTotalSeconds;
            if (utcSeconds >= SECONDS_1991_09_15_02) {
                zoneOffsetTotalSeconds = 28800;
            } else {
                zoneOffsetTotalSeconds = DateUtils.getShanghaiZoneOffsetTotalSeconds(utcSeconds);
            }
            return (utcSeconds - zoneOffsetTotalSeconds) * 1000L + time.nano / 1_000_000;
        }
        Calendar calendar = Calendar.getInstance(zone.timeZone);
        calendar.set(
                date.year,
                date.monthValue - 1,
                date.dayOfMonth,
                time.hour,
                time.minute,
                time.second
        );
        calendar.set(Calendar.MILLISECOND, time.nano / 1_000_000);

        return calendar.getTimeInMillis();
    }

    public String format(DateTimeFormatter formatter) {
        return formatter.format(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZonedDateTime that = (ZonedDateTime) o;
        return (dateTime == that.dateTime) || (dateTime != null && dateTime.equals(that.dateTime))
                && offsetSeconds == that.offsetSeconds
                && (zone == that.zone) || (zone != null && zone.equals(that.zone));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{dateTime, offsetSeconds, zone});
    }
}
