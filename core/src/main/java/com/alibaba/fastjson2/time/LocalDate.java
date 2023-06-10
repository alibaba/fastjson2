package com.alibaba.fastjson2.time;

import java.util.Arrays;
import java.util.Date;

import static com.alibaba.fastjson2.time.ZoneId.DEFAULT_ZONE_ID;

public final class LocalDate {
    public final int year;
    public final short monthValue;
    public final short dayOfMonth;

    private LocalDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.monthValue = (short) month;
        this.dayOfMonth = (short) dayOfMonth;
    }

    public static LocalDate of(int year, int month, int dayOfMonth) {
        if (month <= 0 || month > 12) {
            throw new DateTimeException("Invalid value for month (valid values [1,12]): " + month);
        }

        if (dayOfMonth <= 0 || dayOfMonth > 31) {
            throw new DateTimeException("Invalid value for month (valid values [1,31]): " + dayOfMonth);
        }

        return create(year, month, dayOfMonth);
    }

    private static LocalDate create(int year, int month, int dayOfMonth) {
        if (dayOfMonth > 28) {
            int dom = 31;
            switch (month) {
                case 2:
                    dom = (isLeapYear(year) ? 29 : 28);
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    dom = 30;
                    break;
            }
            if (dayOfMonth > dom) {
                if (dayOfMonth == 29) {
                    throw new DateTimeException("Invalid date 'February 29' as '" + year + "' is not a leap year");
                } else {
                    throw new DateTimeException("Invalid date month-" + dayOfMonth + "'");
                }
            }
        }
        return new LocalDate(year, month, dayOfMonth);
    }

    public static LocalDate ofEpochDay(long epochDay) {
        final int DAYS_PER_CYCLE = 146097;
        long zeroDay = epochDay + 719528; // DAYS_0000_TO_1970;
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
        int month = (marchMonth0 + 2) % 12 + 1;
        int dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
        yearEst += marchMonth0 / 10;

        // check year now we are certain it is correct
        int year = LocalDateTime.checkYear(yearEst);
        return new LocalDate(year, month, dom);
    }

    public long toEpochDay() {
        long y = year;
        long m = monthValue;
        long total = 0;
        total += 365 * y;
        if (y >= 0) {
            total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400;
        } else {
            total -= y / -4 - y / -100 + y / -400;
        }
        total += ((367 * m - 362) / 12);
        total += dayOfMonth - 1;
        if (m > 2) {
            total--;
            if (!isLeapYear(year)) {
                total--;
            }
        }
        return total - 719528; // DAYS_0000_TO_1970;
    }

    public static boolean isLeapYear(int year) {
        return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
    }

    public LocalDateTime atStartOfDay() {
        return LocalDateTime.of(this, LocalTime.MIN);
    }

    public Date toDate() {
        return atStartOfDay()
                .toInstant(DEFAULT_ZONE_ID)
                .toDate();
    }

    public ZonedDateTime atStartOfDay(ZoneId zone) {
        return ZonedDateTime.of(
                LocalDateTime.of(this, LocalTime.MIN),
                zone
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalDate localDate = (LocalDate) o;
        return year == localDate.year && monthValue == localDate.monthValue && dayOfMonth == localDate.dayOfMonth;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{year, monthValue, dayOfMonth});
    }
}
