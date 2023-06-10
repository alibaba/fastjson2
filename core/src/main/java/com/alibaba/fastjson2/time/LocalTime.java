package com.alibaba.fastjson2.time;

import java.util.Arrays;

public final class LocalTime {
    private static final LocalTime[] HOURS = new LocalTime[24];
    public static final LocalTime MIN;
    public static final LocalTime MIDNIGHT;

    public final byte hour;
    public final byte minute;
    public final byte second;
    public final int nano;

    static {
        for (int i = 0; i < HOURS.length; i++) {
            HOURS[i] = new LocalTime(i, 0, 0, 0);
        }
        MIDNIGHT = HOURS[0];
//        NOON = HOURS[12];
        MIN = HOURS[0];
//        MAX = new LocalTime(23, 59, 59, 999_999_999);
    }

    private LocalTime(int hour, int minute, int second, int nano) {
        this.hour = (byte) hour;
        this.minute = (byte) minute;
        this.second = (byte) second;
        this.nano = nano;
    }

    public static LocalTime of(int hour, int minute, int second) {
        return new LocalTime(hour, minute, second, 0);
    }

    public static LocalTime of(int hour, int minute, int second, int nano) {
        return new LocalTime(hour, minute, second, nano);
    }

    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        final long NANOS_PER_HOUR = 3600000000000L;
        final long NANOS_PER_MINUTE = 60000000000L;
        final long NANOS_PER_SECOND = 1000_000_000L;
        LocalDateTime.checkSecondOfDay(nanoOfDay);
        int hours = (int) (nanoOfDay / NANOS_PER_HOUR);
        nanoOfDay -= hours * NANOS_PER_HOUR;
        int minutes = (int) (nanoOfDay / NANOS_PER_MINUTE);
        nanoOfDay -= minutes * NANOS_PER_MINUTE;
        int seconds = (int) (nanoOfDay / NANOS_PER_SECOND);
        nanoOfDay -= seconds * NANOS_PER_SECOND;
        return create(hours, minutes, seconds, (int) nanoOfDay);
    }

    private static LocalTime create(int hour, int minute, int second, int nanoOfSecond) {
        if ((minute | second | nanoOfSecond) == 0) {
            return HOURS[hour];
        }
        return new LocalTime(hour, minute, second, nanoOfSecond);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalTime localTime = (LocalTime) o;
        return hour == localTime.hour && minute == localTime.minute && second == localTime.second && nano == localTime.nano;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{hour, minute, second, nano});
    }
}
