package com.alibaba.fastjson3.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * High-performance date/time parsing utilities.
 * Uses manual character-by-character parsing to avoid DateTimeFormatter overhead.
 *
 * <p>Supported formats:
 * <ul>
 *   <li>LocalDate: yyyy-MM-dd, yyyy/MM/dd, yyyyMMdd, dd.MM.yyyy</li>
 *   <li>LocalDateTime: yyyy-MM-dd HH:mm:ss, yyyy-MM-ddTHH:mm:ss, yyyy/MM/dd HH:mm:ss,
 *       yyyy-MM-dd HH:mm, yyyyMMddHHmmss, with optional millis (.SSS) and timezone</li>
 *   <li>LocalTime: HH:mm:ss, HH:mm</li>
 *   <li>Instant: ISO 8601 with Z or +/-offset</li>
 *   <li>Numeric timestamps (millis since epoch)</li>
 * </ul>
 */
public final class DateUtils {
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);

    private DateUtils() {
    }

    // ---- digit extraction (no bounds check – callers guarantee bounds) ----

    static int digit1(char c) {
        int d = c - '0';
        if (d < 0 || d > 9) {
            return -1;
        }
        return d;
    }

    static int digit2(char c0, char c1) {
        int d0 = c0 - '0';
        int d1 = c1 - '0';
        if ((d0 | d1) < 0 || d0 > 9 || d1 > 9) {
            return -1;
        }
        return d0 * 10 + d1;
    }

    static int digit4(char c0, char c1, char c2, char c3) {
        int d0 = c0 - '0';
        int d1 = c1 - '0';
        int d2 = c2 - '0';
        int d3 = c3 - '0';
        if ((d0 | d1 | d2 | d3) < 0 || d0 > 9 || d1 > 9 || d2 > 9 || d3 > 9) {
            return -1;
        }
        return d0 * 1000 + d1 * 100 + d2 * 10 + d3;
    }

    static int maxDayOfMonth(int year, int month) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            case 2 -> isLeapYear(year) ? 29 : 28;
            default -> -1;
        };
    }

    static boolean isLeapYear(int year) {
        return (year & 3) == 0 && ((year % 100) != 0 || (year % 400) == 0);
    }

    // ---- LocalDate parsing ----

    /**
     * Parse a date string to LocalDate. Supports:
     * <ul>
     *   <li>yyyy-MM-dd (10 chars)</li>
     *   <li>yyyy/MM/dd (10 chars)</li>
     *   <li>dd.MM.yyyy (10 chars)</li>
     *   <li>yyyyMMdd (8 chars)</li>
     * </ul>
     */
    public static LocalDate parseLocalDate(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        int len = str.length();
        return switch (len) {
            case 8 -> parseLocalDate8(str);
            case 10 -> parseLocalDate10(str);
            default -> LocalDate.parse(str); // fallback
        };
    }

    // yyyyMMdd
    static LocalDate parseLocalDate8(String str) {
        int year = digit4(str.charAt(0), str.charAt(1), str.charAt(2), str.charAt(3));
        int month = digit2(str.charAt(4), str.charAt(5));
        int dom = digit2(str.charAt(6), str.charAt(7));
        if (year < 0 || month < 1 || month > 12 || dom < 1 || dom > maxDayOfMonth(year, month)) {
            throw new DateTimeParseException("invalid date", str, 0);
        }
        return LocalDate.of(year, month, dom);
    }

    // yyyy-MM-dd, yyyy/MM/dd, dd.MM.yyyy
    static LocalDate parseLocalDate10(String str) {
        char c2 = str.charAt(2);
        char c4 = str.charAt(4);
        char c5 = str.charAt(5);
        char c7 = str.charAt(7);

        int year, month, dom;
        if ((c4 == '-' && c7 == '-') || (c4 == '/' && c7 == '/')) {
            // yyyy-MM-dd or yyyy/MM/dd
            year = digit4(str.charAt(0), str.charAt(1), str.charAt(2), str.charAt(3));
            month = digit2(str.charAt(5), str.charAt(6));
            dom = digit2(str.charAt(8), str.charAt(9));
        } else if (c2 == '.' && c5 == '.') {
            // dd.MM.yyyy
            dom = digit2(str.charAt(0), str.charAt(1));
            month = digit2(str.charAt(3), str.charAt(4));
            year = digit4(str.charAt(6), str.charAt(7), str.charAt(8), str.charAt(9));
        } else {
            return LocalDate.parse(str); // fallback
        }

        if (year < 0 || month < 1 || month > 12 || dom < 1 || dom > maxDayOfMonth(year, month)) {
            throw new DateTimeParseException("invalid date", str, 0);
        }
        return LocalDate.of(year, month, dom);
    }

    // ---- LocalTime parsing ----

    /**
     * Parse a time string to LocalTime. Supports:
     * <ul>
     *   <li>HH:mm:ss (8 chars)</li>
     *   <li>HH:mm (5 chars)</li>
     *   <li>HH:mm:ss.SSS... (12+ chars)</li>
     * </ul>
     */
    public static LocalTime parseLocalTime(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        int len = str.length();
        if (len == 8 && str.charAt(2) == ':' && str.charAt(5) == ':') {
            int hour = digit2(str.charAt(0), str.charAt(1));
            int minute = digit2(str.charAt(3), str.charAt(4));
            int second = digit2(str.charAt(6), str.charAt(7));
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                throw new DateTimeParseException("invalid time", str, 0);
            }
            return LocalTime.of(hour, minute, second);
        }
        if (len == 5 && str.charAt(2) == ':') {
            int hour = digit2(str.charAt(0), str.charAt(1));
            int minute = digit2(str.charAt(3), str.charAt(4));
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new DateTimeParseException("invalid time", str, 0);
            }
            return LocalTime.of(hour, minute);
        }
        // HH:mm:ss.SSS...
        if (len > 8 && str.charAt(2) == ':' && str.charAt(5) == ':' && str.charAt(8) == '.') {
            int hour = digit2(str.charAt(0), str.charAt(1));
            int minute = digit2(str.charAt(3), str.charAt(4));
            int second = digit2(str.charAt(6), str.charAt(7));
            int nanos = parseFractionNanos(str, 9, len);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59 || nanos < 0) {
                throw new DateTimeParseException("invalid time", str, 0);
            }
            return LocalTime.of(hour, minute, second, nanos);
        }

        return LocalTime.parse(str); // fallback
    }

    // ---- LocalDateTime parsing ----

    /**
     * Parse a datetime string to LocalDateTime. Supports common formats with
     * high-performance manual parsing.
     */
    public static LocalDateTime parseLocalDateTime(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        int len = str.length();
        return switch (len) {
            case 8 -> {
                // Could be HH:mm:ss (time) or yyyyMMdd (date)
                if (str.charAt(2) == ':' && str.charAt(5) == ':') {
                    LocalTime time = parseLocalTime(str);
                    yield time != null ? LocalDateTime.of(EPOCH, time) : null;
                }
                LocalDate date = parseLocalDate8(str);
                yield date != null ? LocalDateTime.of(date, LocalTime.MIN) : null;
            }
            case 10 -> {
                LocalDate date = parseLocalDate10(str);
                yield date != null ? LocalDateTime.of(date, LocalTime.MIN) : null;
            }
            case 14 -> parseLocalDateTime14(str);
            case 16 -> parseLocalDateTime16(str);
            case 19 -> parseLocalDateTime19(str);
            default -> {
                if (len > 19) {
                    yield parseLocalDateTimeX(str);
                }
                yield LocalDateTime.parse(str); // fallback
            }
        };
    }

    // yyyyMMddHHmmss
    static LocalDateTime parseLocalDateTime14(String str) {
        int year = digit4(str.charAt(0), str.charAt(1), str.charAt(2), str.charAt(3));
        int month = digit2(str.charAt(4), str.charAt(5));
        int dom = digit2(str.charAt(6), str.charAt(7));
        int hour = digit2(str.charAt(8), str.charAt(9));
        int minute = digit2(str.charAt(10), str.charAt(11));
        int second = digit2(str.charAt(12), str.charAt(13));
        if (year < 0 || month < 1 || month > 12 || dom < 1 || dom > maxDayOfMonth(year, month)
                || hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
            throw new DateTimeParseException("invalid datetime", str, 0);
        }
        return LocalDateTime.of(year, month, dom, hour, minute, second);
    }

    // yyyy-MM-dd HH:mm or yyyy-MM-ddTHH:mm
    static LocalDateTime parseLocalDateTime16(String str) {
        char c4 = str.charAt(4);
        char c7 = str.charAt(7);
        char c10 = str.charAt(10);
        char c13 = str.charAt(13);

        if (((c4 == '-' && c7 == '-') || (c4 == '/' && c7 == '/')) && (c10 == ' ' || c10 == 'T') && c13 == ':') {
            int year = digit4(str.charAt(0), str.charAt(1), str.charAt(2), str.charAt(3));
            int month = digit2(str.charAt(5), str.charAt(6));
            int dom = digit2(str.charAt(8), str.charAt(9));
            int hour = digit2(str.charAt(11), str.charAt(12));
            int minute = digit2(str.charAt(14), str.charAt(15));
            if (year < 0 || month < 1 || month > 12 || dom < 1 || dom > maxDayOfMonth(year, month)
                    || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new DateTimeParseException("invalid datetime", str, 0);
            }
            return LocalDateTime.of(year, month, dom, hour, minute);
        }

        return LocalDateTime.parse(str);
    }

    // yyyy-MM-dd HH:mm:ss, yyyy-MM-ddTHH:mm:ss, yyyy/MM/dd HH:mm:ss, dd/MM/yyyy HH:mm:ss
    static LocalDateTime parseLocalDateTime19(String str) {
        char c2 = str.charAt(2);
        char c4 = str.charAt(4);
        char c5 = str.charAt(5);
        char c7 = str.charAt(7);
        char c10 = str.charAt(10);
        char c13 = str.charAt(13);
        char c16 = str.charAt(16);

        int year, month, dom, hour, minute, second;

        if (((c4 == '-' && c7 == '-') || (c4 == '/' && c7 == '/'))
                && (c10 == ' ' || c10 == 'T')
                && c13 == ':' && c16 == ':') {
            // yyyy-MM-dd HH:mm:ss or yyyy/MM/dd HH:mm:ss
            year = digit4(str.charAt(0), str.charAt(1), str.charAt(2), str.charAt(3));
            month = digit2(str.charAt(5), str.charAt(6));
            dom = digit2(str.charAt(8), str.charAt(9));
            hour = digit2(str.charAt(11), str.charAt(12));
            minute = digit2(str.charAt(14), str.charAt(15));
            second = digit2(str.charAt(17), str.charAt(18));
        } else if (c2 == '/' && c5 == '/' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
            // dd/MM/yyyy HH:mm:ss
            dom = digit2(str.charAt(0), str.charAt(1));
            month = digit2(str.charAt(3), str.charAt(4));
            year = digit4(str.charAt(6), str.charAt(7), str.charAt(8), str.charAt(9));
            hour = digit2(str.charAt(11), str.charAt(12));
            minute = digit2(str.charAt(14), str.charAt(15));
            second = digit2(str.charAt(17), str.charAt(18));
        } else if (c2 == '.' && c5 == '.' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
            // dd.MM.yyyy HH:mm:ss
            dom = digit2(str.charAt(0), str.charAt(1));
            month = digit2(str.charAt(3), str.charAt(4));
            year = digit4(str.charAt(6), str.charAt(7), str.charAt(8), str.charAt(9));
            hour = digit2(str.charAt(11), str.charAt(12));
            minute = digit2(str.charAt(14), str.charAt(15));
            second = digit2(str.charAt(17), str.charAt(18));
        } else {
            return LocalDateTime.parse(str);
        }

        if (year < 0 || month < 1 || month > 12 || dom < 1 || dom > maxDayOfMonth(year, month)
                || hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
            throw new DateTimeParseException("invalid datetime", str, 0);
        }
        return LocalDateTime.of(year, month, dom, hour, minute, second);
    }

    // Handles lengths > 19: yyyy-MM-dd HH:mm:ss.SSS, with Z or +offset
    static LocalDateTime parseLocalDateTimeX(String str) {
        int len = str.length();
        // Must start with 19-char datetime base
        if (len < 19) {
            return LocalDateTime.parse(str);
        }

        // Parse the first 19 chars as base datetime
        char c4 = str.charAt(4);
        char c7 = str.charAt(7);
        char c10 = str.charAt(10);
        char c13 = str.charAt(13);
        char c16 = str.charAt(16);

        if (!(((c4 == '-' && c7 == '-') || (c4 == '/' && c7 == '/')) && (c10 == ' ' || c10 == 'T')
                && c13 == ':' && c16 == ':')) {
            return LocalDateTime.parse(str);
        }

        int year = digit4(str.charAt(0), str.charAt(1), str.charAt(2), str.charAt(3));
        int month = digit2(str.charAt(5), str.charAt(6));
        int dom = digit2(str.charAt(8), str.charAt(9));
        int hour = digit2(str.charAt(11), str.charAt(12));
        int minute = digit2(str.charAt(14), str.charAt(15));
        int second = digit2(str.charAt(17), str.charAt(18));

        if (year < 0 || month < 1 || month > 12 || dom < 1 || dom > maxDayOfMonth(year, month)
                || hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
            throw new DateTimeParseException("invalid datetime", str, 0);
        }

        int nanos = 0;
        int pos = 19;
        if (pos < len && str.charAt(pos) == '.') {
            int fracEnd = pos + 1;
            while (fracEnd < len && str.charAt(fracEnd) >= '0' && str.charAt(fracEnd) <= '9') {
                fracEnd++;
            }
            nanos = parseFractionNanos(str, pos + 1, fracEnd);
            pos = fracEnd;
        }

        // Ignore trailing timezone for LocalDateTime (Z, +HH:mm, -HH:mm)
        return LocalDateTime.of(year, month, dom, hour, minute, second, nanos);
    }

    // ---- Instant parsing ----

    /**
     * Parse a string to Instant. Supports:
     * <ul>
     *   <li>ISO 8601: 2024-06-15T10:30:00Z</li>
     *   <li>ISO 8601 with offset: 2024-06-15T10:30:00+08:00</li>
     *   <li>ISO 8601 with millis: 2024-06-15T10:30:00.123Z</li>
     *   <li>Numeric millis timestamp</li>
     *   <li>yyyy-MM-dd HH:mm:ss (interpreted as default timezone)</li>
     * </ul>
     */
    public static Instant parseInstant(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        int len = str.length();

        // Fast path for 20 chars: yyyy-MM-ddTHH:mm:ssZ
        // Also handles > 20 chars with 'T' at position 10 (ISO 8601 with millis/offset)
        if (len >= 20 && str.charAt(10) == 'T') {
            return parseInstantISO(str);
        }

        // 19 chars without Z: treat as local timezone
        if (len == 19) {
            LocalDateTime ldt = parseLocalDateTime19(str);
            if (ldt != null) {
                return ldt.atZone(DEFAULT_ZONE_ID).toInstant();
            }
        }

        return Instant.parse(str); // fallback
    }

    static Instant parseInstantISO(String str) {
        int len = str.length();
        // Parse base datetime (first 19 chars)
        char c4 = str.charAt(4);
        char c7 = str.charAt(7);
        char c10 = str.charAt(10);
        char c13 = str.charAt(13);
        char c16 = str.charAt(16);

        if (!((c4 == '-') && (c7 == '-') && (c10 == 'T') && c13 == ':' && c16 == ':')) {
            return Instant.parse(str);
        }

        int year = digit4(str.charAt(0), str.charAt(1), str.charAt(2), str.charAt(3));
        int month = digit2(str.charAt(5), str.charAt(6));
        int dom = digit2(str.charAt(8), str.charAt(9));
        int hour = digit2(str.charAt(11), str.charAt(12));
        int minute = digit2(str.charAt(14), str.charAt(15));
        int second = digit2(str.charAt(17), str.charAt(18));

        if (year < 0 || month < 1 || month > 12 || dom < 1 || dom > maxDayOfMonth(year, month)
                || hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
            return Instant.parse(str);
        }

        int nanos = 0;
        int pos = 19;
        if (pos < len && str.charAt(pos) == '.') {
            int fracEnd = pos + 1;
            while (fracEnd < len && str.charAt(fracEnd) >= '0' && str.charAt(fracEnd) <= '9') {
                fracEnd++;
            }
            nanos = parseFractionNanos(str, pos + 1, fracEnd);
            pos = fracEnd;
        }

        // Parse timezone
        ZoneOffset offset;
        if (pos < len) {
            char tz = str.charAt(pos);
            if (tz == 'Z') {
                offset = ZoneOffset.UTC;
            } else if (tz == '+' || tz == '-') {
                offset = parseZoneOffset(str, pos);
                if (offset == null) {
                    return Instant.parse(str);
                }
            } else {
                return Instant.parse(str);
            }
        } else {
            // No timezone suffix - treat as UTC for Instant
            offset = ZoneOffset.UTC;
        }

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, second, nanos);
        return ldt.toInstant(offset);
    }

    // ---- ZonedDateTime / OffsetDateTime parsing ----

    /**
     * Parse a string to ZonedDateTime. Supports ISO 8601 formats and
     * common datetime formats with timezone info.
     */
    public static ZonedDateTime parseZonedDateTime(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        // 19 chars without timezone: use default zone
        if (str.length() == 19) {
            LocalDateTime ldt = parseLocalDateTime19(str);
            if (ldt != null) {
                return ldt.atZone(DEFAULT_ZONE_ID);
            }
        }

        return ZonedDateTime.parse(str); // fallback to JDK (handles all ISO variants)
    }

    /**
     * Parse a string to OffsetDateTime. Supports ISO 8601 formats.
     */
    public static OffsetDateTime parseOffsetDateTime(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        int len = str.length();

        // Fast path for ISO 8601 with Z or offset via Instant parsing
        if (len >= 20 && str.charAt(10) == 'T') {
            Instant instant = parseInstantISO(str);
            if (instant != null) {
                // Recover the offset for OffsetDateTime
                char c19 = str.charAt(19);
                ZoneOffset offset;
                if (c19 == 'Z') {
                    offset = ZoneOffset.UTC;
                } else if (c19 == '.' || (c19 >= '0' && c19 <= '9')) {
                    // has fractional seconds, find timezone after
                    int tzPos = 19;
                    while (tzPos < len && str.charAt(tzPos) != 'Z' && str.charAt(tzPos) != '+' && str.charAt(tzPos) != '-') {
                        tzPos++;
                    }
                    if (tzPos < len && str.charAt(tzPos) == 'Z') {
                        offset = ZoneOffset.UTC;
                    } else if (tzPos < len) {
                        offset = parseZoneOffset(str, tzPos);
                        if (offset == null) {
                            return OffsetDateTime.parse(str);
                        }
                    } else {
                        offset = ZoneOffset.UTC;
                    }
                } else if (c19 == '+' || c19 == '-') {
                    offset = parseZoneOffset(str, 19);
                    if (offset == null) {
                        return OffsetDateTime.parse(str);
                    }
                } else {
                    return OffsetDateTime.parse(str);
                }
                return instant.atOffset(offset);
            }
        }

        // 19 chars without timezone: use default zone offset
        if (len == 19) {
            LocalDateTime ldt = parseLocalDateTime19(str);
            if (ldt != null) {
                ZoneOffset offset = DEFAULT_ZONE_ID.getRules().getOffset(ldt);
                return OffsetDateTime.of(ldt, offset);
            }
        }

        return OffsetDateTime.parse(str); // fallback
    }

    // ---- Date (java.util.Date) parsing ----

    /**
     * Parse a string to java.util.Date. Supports all formats supported by
     * parseInstant, plus common date-only and datetime formats.
     */
    public static Date parseDate(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        int len = str.length();

        // Numeric millis timestamp
        if (isNumeric(str)) {
            return new Date(Long.parseLong(str));
        }

        // Date-only formats → midnight in default timezone
        if (len == 10 || len == 8) {
            LocalDate ld = parseLocalDate(str);
            if (ld != null) {
                Instant instant = ld.atStartOfDay(DEFAULT_ZONE_ID).toInstant();
                return Date.from(instant);
            }
        }

        // Datetime formats (parseInstant handles all remaining cases with fallback)
        Instant instant = parseInstant(str);
        return Date.from(instant);
    }

    // ---- Millis calculation (for high-perf Date creation) ----

    /**
     * Calculate epoch millis from datetime components, applying the given zone offset.
     */
    public static long toEpochMilli(int year, int month, int dom,
                                    int hour, int minute, int second, int nanos,
                                    int offsetTotalSeconds) {
        long epochDay = calcEpochDay(year, month, dom);
        long secondsOfDay = hour * 3600L + minute * 60L + second;
        long epochSecond = epochDay * 86400L + secondsOfDay - offsetTotalSeconds;
        return epochSecond * 1000L + nanos / 1_000_000;
    }

    static long calcEpochDay(int year, int month, int dom) {
        final int DAYS_PER_CYCLE = 146097;
        final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

        long total = (365L * year)
                + ((year + 3) / 4 - (year + 99) / 100 + (year + 399) / 400)
                + ((367 * month - 362) / 12)
                + (dom - 1);

        if (month > 2) {
            total--;
            if (!isLeapYear(year)) {
                total--;
            }
        }

        return total - DAYS_0000_TO_1970;
    }

    // ---- Helpers ----

    static int parseFractionNanos(String str, int start, int end) {
        int digits = end - start;
        if (digits <= 0 || digits > 9) {
            return -1;
        }
        int value = 0;
        for (int i = start; i < end; i++) {
            int d = str.charAt(i) - '0';
            if (d < 0 || d > 9) {
                return -1;
            }
            value = value * 10 + d;
        }
        // Scale to nanoseconds (9 digits)
        for (int i = digits; i < 9; i++) {
            value *= 10;
        }
        return value;
    }

    static ZoneOffset parseZoneOffset(String str, int pos) {
        int len = str.length();
        if (pos >= len) {
            return null;
        }
        char sign = str.charAt(pos);
        if (sign != '+' && sign != '-') {
            return null;
        }

        int remaining = len - pos - 1;
        int offsetHour, offsetMinute = 0;

        if (remaining == 2) {
            // +HH
            offsetHour = digit2(str.charAt(pos + 1), str.charAt(pos + 2));
        } else if (remaining == 4 && str.charAt(pos + 2) == ':') {
            // +H:mm
            offsetHour = digit1(str.charAt(pos + 1));
            offsetMinute = digit2(str.charAt(pos + 3), str.charAt(pos + 4));
            if (offsetHour < 0) {
                return null;
            }
        } else if (remaining == 5 && str.charAt(pos + 3) == ':') {
            // +HH:mm
            offsetHour = digit2(str.charAt(pos + 1), str.charAt(pos + 2));
            offsetMinute = digit2(str.charAt(pos + 4), str.charAt(pos + 5));
        } else if (remaining == 4) {
            // +HHmm
            offsetHour = digit2(str.charAt(pos + 1), str.charAt(pos + 2));
            offsetMinute = digit2(str.charAt(pos + 3), str.charAt(pos + 4));
        } else {
            return null;
        }

        if (offsetHour < 0 || offsetHour > 18 || offsetMinute < 0 || offsetMinute > 59) {
            return null;
        }

        int totalSeconds = (offsetHour * 3600 + offsetMinute * 60) * (sign == '-' ? -1 : 1);
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    static boolean isNumeric(String str) {
        if (str.isEmpty()) {
            return false;
        }
        int start = 0;
        if (str.charAt(0) == '-') {
            if (str.length() == 1) {
                return false;
            }
            start = 1;
        }
        for (int i = start; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    // ---- Optimized date writing helpers ----

    /**
     * Write a LocalDate to the given byte array in yyyy-MM-dd format.
     * Caller must ensure buf has at least off+10 capacity.
     * Returns the number of bytes written (always 10).
     */
    public static int writeLocalDate(byte[] buf, int off, LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int dom = date.getDayOfMonth();

        buf[off] = (byte) (year / 1000 + '0');
        buf[off + 1] = (byte) (year / 100 % 10 + '0');
        buf[off + 2] = (byte) (year / 10 % 10 + '0');
        buf[off + 3] = (byte) (year % 10 + '0');
        buf[off + 4] = '-';
        buf[off + 5] = (byte) (month / 10 + '0');
        buf[off + 6] = (byte) (month % 10 + '0');
        buf[off + 7] = '-';
        buf[off + 8] = (byte) (dom / 10 + '0');
        buf[off + 9] = (byte) (dom % 10 + '0');
        return 10;
    }

    /**
     * Write a LocalDateTime to the given byte array in yyyy-MM-ddTHH:mm:ss format.
     * Returns the number of bytes written (19, or more if nanos present).
     */
    public static int writeLocalDateTime(byte[] buf, int off, LocalDateTime ldt) {
        writeLocalDate(buf, off, ldt.toLocalDate());
        buf[off + 10] = 'T';
        writeLocalTime(buf, off + 11, ldt.toLocalTime());
        int nanos = ldt.getNano();
        if (nanos == 0) {
            return 19;
        }
        return 19 + writeFractionNanos(buf, off + 19, nanos);
    }

    /**
     * Write a LocalTime to the given byte array in HH:mm:ss format.
     * Returns 8.
     */
    public static int writeLocalTime(byte[] buf, int off, LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        int second = time.getSecond();

        buf[off] = (byte) (hour / 10 + '0');
        buf[off + 1] = (byte) (hour % 10 + '0');
        buf[off + 2] = ':';
        buf[off + 3] = (byte) (minute / 10 + '0');
        buf[off + 4] = (byte) (minute % 10 + '0');
        buf[off + 5] = ':';
        buf[off + 6] = (byte) (second / 10 + '0');
        buf[off + 7] = (byte) (second % 10 + '0');
        return 8;
    }

    public static int writeFractionNanos(byte[] buf, int off, int nanos) {
        buf[off] = '.';
        // Write up to 9 digits, trimming trailing zeros
        int digits = 9;
        while (digits > 1 && nanos % 10 == 0) {
            nanos /= 10;
            digits--;
        }
        int pos = off + digits;
        for (int i = digits; i > 0; i--) {
            buf[pos--] = (byte) (nanos % 10 + '0');
            nanos /= 10;
        }
        return 1 + digits;
    }
}
