package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.alibaba.fastjson2.time.ZoneId.DEFAULT_ZONE_ID;

public final class DateTimeFormatter {
    private final String pattern;
    private final PatternType type;
    private final Locale locale;

    public DateTimeFormatter(String pattern, Locale locale) {
        this(pattern, locale, null);
    }

    public DateTimeFormatter(String pattern, Locale locale, ZoneId zoneId) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null");
        }
        this.pattern = pattern;
        this.locale = locale;
        PatternType type;
        switch (pattern) {
            case "HHmmss":
                type = PatternType.P6;
                break;
            case "yyyyMMdd":
                type = PatternType.P8;
                break;
            case "yyyy-M-d":
                type = PatternType.P8_1;
                break;
            case "HH:mm:ss":
                type = PatternType.P8_2;
                break;
            case "d-M-yyyy":
                type = PatternType.P8_3;
                break;
            case "d.M.yyyy":
                type = PatternType.P8_4;
                break;
            case "yyyy/M/d":
                type = PatternType.P8_5;
                break;
            case "yyyy-M-dd":
                type = PatternType.P9;
                break;
            case "yyyy-MM-d":
                type = PatternType.P9_1;
                break;
            case "yyyy-MM-dd":
                type = PatternType.P10;
                break;
            case "yyyy/MM/dd":
                type = PatternType.P10_1;
                break;
            case "yyyy-MMM-dd":
                type = PatternType.P11;
                break;
            case "yyyyMMddHHmm":
                type = PatternType.P12;
                break;
            case "yyyyMMddHHmmss":
                type = PatternType.P14;
                break;
            case "yyyy-MM-dd HH:mm:ss.SSSSSSSSS":
                type = PatternType.P29;
                break;
            case "yyyy-MM-dd HH:mm:ss":
                type = PatternType.P19;
                break;
            case "yyyy/MM/dd HH:mm:ss":
                type = PatternType.P19_1;
                break;
            case "yyyy-MM-ddTHH:mm:ss":
            case "yyyy-MM-dd'T'HH:mm:ss":
                type = PatternType.P19_2;
                break;
            case "dd.MM.yyyy HH:mm:ss":
                type = PatternType.P19_3;
                break;
            default:
                type = PatternType.OTHER;
                break;
        }
        this.type = type;
    }

    public String format(LocalDateTime localDateTime) {
        throw new JSONException("TODO : " + pattern);
    }

    public String format(LocalDate ldt) {
        switch (type) {
            case P8:
                return DateUtils.formatYMD8(ldt.year, ldt.monthValue, ldt.dayOfMonth);
            case P9: {
                return DateUtils.formatYMD9(ldt.year, ldt.monthValue, ldt.dayOfMonth);
            }
            case P10:
                return DateUtils.formatYMD10(ldt);
            case P19:
                return DateUtils.formatYMDHMS19(ldt);
            default:
                break;
        }

        throw new JSONException("TODO : " + pattern);
    }

    public String format(ZonedDateTime zdt) {
        switch (type) {
            case P6:
                return DateUtils.formatHMS6(zdt.dateTime.time);
            case P8_2:
                return DateUtils.formatHMS8(zdt.dateTime.time);
            case P9:
                return DateUtils.formatYMD9(zdt.dateTime.date.year, zdt.dateTime.date.monthValue, zdt.dateTime.date.dayOfMonth);
            case P10:
                return DateUtils.formatYMD10(zdt.dateTime.date);
            case P19:
                return DateUtils.formatYMDHMS19(zdt.dateTime);
            case P29:
                return DateUtils.formatYMDHMS29(zdt.dateTime);
            default:
                break;
        }

        Date date = new Date(zdt.toEpochMilli());
        SimpleDateFormat fmt = getFormat(DEFAULT_ZONE_ID);
        return fmt.format(date);
    }

    public String format(Date date) {
        switch (type) {
            case P10:
                return DateUtils.formatYMD10(date.getTime(), DEFAULT_ZONE_ID);
            case P19:
                return DateUtils.formatYMDHMS19(date, DEFAULT_ZONE_ID);
//            case P29:
//                return DateUtils.formatYMDHMS29(date);
            default:
                break;
        }

        SimpleDateFormat fmt = locale == null
                ? new SimpleDateFormat(pattern)
                : new SimpleDateFormat(pattern, locale);
        return fmt.format(date);
    }

    public Date parseDate(String str, ZoneId zoneId) {
        SimpleDateFormat fmt = getFormat(zoneId);
        try {
            return fmt.parse(str);
        } catch (ParseException e) {
            throw new JSONException("parse error, format " + pattern, e);
        }
    }

    private SimpleDateFormat getFormat(ZoneId zoneId) {
        SimpleDateFormat fmt = locale == null
                ? new SimpleDateFormat(pattern)
                : new SimpleDateFormat(pattern, locale);
        fmt.setTimeZone(zoneId.timeZone);
        return fmt;
    }

    public LocalDate parseLocalDate(String str) {
        byte[] bytes = str.getBytes();
        switch (type) {
            case P8:
            case P8_1:
            case P8_3:
            case P8_4:
            case P8_5:
                if (bytes.length == 8) {
                    return DateUtils.parseLocalDate8(bytes, 0);
                }
                if (bytes.length == 9) {
                    return DateUtils.parseLocalDate9(bytes, 0);
                }
                if (bytes.length == 10) {
                    return DateUtils.parseLocalDate10(bytes, 0);
                }
                break;
            case P9:
            case P9_1:
                if (bytes.length == 9) {
                    return DateUtils.parseLocalDate9(bytes, 0);
                }
                if (bytes.length == 10) {
                    return DateUtils.parseLocalDate10(bytes, 0);
                }
                break;
            case P10:
            case P10_1:
                return DateUtils.parseLocalDate10(bytes, 0);
            default:
                break;
        }
        throw new JSONException("TODO : " + pattern);
    }

    public LocalDateTime parseLocalDateTime(String str) {
        byte[] bytes = str.getBytes();
        LocalDateTime ldt = null;
        switch (type) {
            case P12:
                ldt = DateUtils.parseLocalDateTime12(bytes, 0);
                break;
            case P14:
                ldt = DateUtils.parseLocalDateTime14(bytes, 0);
                break;
            case P19:
            case P19_1:
            case P19_2:
            case P19_3:
                ldt = DateUtils.parseLocalDateTime19(bytes, 0);
                break;
            default:
                break;
        }

        if (ldt == null) {
            SimpleDateFormat fmt = getFormat(DEFAULT_ZONE_ID);
            try {
                Date date = fmt.parse(str);
                ldt = ZonedDateTime.ofInstant(Instant.of(date), DEFAULT_ZONE_ID).dateTime;
            } catch (ParseException e) {
                throw new JSONException("parse error, format " + pattern + ", input " + str);
            }
        }
        return ldt;
    }

    public ZonedDateTime parseZonedDateTime(String str) {
        try {
            Date date = (getFormat(DEFAULT_ZONE_ID)).parse(str);
            return ZonedDateTime.ofInstant(Instant.of(date), DEFAULT_ZONE_ID);
        } catch (ParseException e) {
            throw new JSONException("parse error, format " + pattern + ", input " + str);
        }
    }

    public LocalTime parseLocalTime(String str) {
        switch (type) {
            case P6:
                return DateUtils.parseLocalTime6(str, 0);
            case P8:
            case P8_2:
                byte[] bytes = str.getBytes();
                return DateUtils.parseLocalTime8(bytes, 0);
            default:
                break;
        }
        throw new JSONException("TODO " + pattern);
    }

    public static DateTimeFormatter ofPattern(String format) {
        return new DateTimeFormatter(format, null);
    }

    public static DateTimeFormatter ofPattern(String format, Locale locale) {
        return new DateTimeFormatter(format, locale);
    }

    public static DateTimeFormatter ofPattern(String format, Locale locale, ZoneId zoneId) {
        return new DateTimeFormatter(format, locale, zoneId);
    }

    enum PatternType {
        P6("HHmmss"),
        P8("yyyyMMdd"),
        P8_1("yyyy-M-d"),
        P8_2("HH:mm:ss"),
        P8_3("d-M-yyyy"),
        P8_4("d.M.yyyy"),
        P8_5("yyyy/M/d"),
        P9("yyyy-M-dd"),
        P9_1("yyyy-MM-d"),
        P10("yyyy-MM-dd"),
        P10_1("yyyy/MM/dd"),
        P11("yyyy-MMM-dd"),
        P12("yyyyMMddHHmm"),
        P14("yyyyMMddHHmmss"),
        P19("yyyy-MM-dd HH:mm:ss"),
        P19_1("yyyy/MM/dd HH:mm:ss"),
        P19_2("yyyy-MM-dd'T'HH:mm:ss"),
        P19_3("dd.MM.yyyy HH:mm:ss"),
        P29("yyyy-MM-dd HH:mm:ss.SSSSSSSSS"),
        OTHER("");

        final String pattern;

        PatternType(String pattern) {
            this.pattern = pattern;
        }
    }
}
