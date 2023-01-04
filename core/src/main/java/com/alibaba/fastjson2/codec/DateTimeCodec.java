package com.alibaba.fastjson2.codec;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public abstract class DateTimeCodec {
    public final String format;
    public final boolean formatUnixTime;
    public final boolean formatMillis;
    public final boolean formatISO8601;

    protected final boolean formatHasDay;
    protected final boolean formatHasHour;
    public final boolean useSimpleFormatter;

    public final Locale locale;

    protected final boolean yyyyMMddhhmmss19;
    protected final boolean yyyyMMddhhmmss14;
    protected final boolean yyyyMMdd10;
    protected final boolean yyyyMMdd8;

    DateTimeFormatter dateFormatter;

    public DateTimeCodec(String format) {
        this(format, null);
    }

    public DateTimeCodec(String format, Locale locale) {
        if (format != null) {
            format = format.replaceAll("aa", "a");
        }

        this.format = format;
        this.locale = locale;
        this.yyyyMMddhhmmss14 = "yyyyMMddHHmmss".equals(format);
        this.yyyyMMddhhmmss19 = "yyyy-MM-dd HH:mm:ss".equals(format);
        this.yyyyMMdd10 = "yyyy-MM-dd".equals(format);
        this.yyyyMMdd8 = "yyyyMMdd".equals(format);

        boolean formatUnixTime = false, formatISO8601 = false, formatMillis = false, hasDay = false, hasHour = false;
        if (format != null) {
            switch (format) {
                case "unixtime":
                    formatUnixTime = true;
                    break;
                case "iso8601":
                    formatISO8601 = true;
                    break;
                case "millis":
                    formatMillis = true;
                    break;
                default:
                    hasDay = format.indexOf('d') != -1;
                    hasHour = format.indexOf('H') != -1
                            || format.indexOf('h') != -1
                            || format.indexOf('K') != -1
                            || format.indexOf('k') != -1;
                    break;
            }
        }
        this.formatUnixTime = formatUnixTime;
        this.formatMillis = formatMillis;
        this.formatISO8601 = formatISO8601;

        this.formatHasDay = hasDay;
        this.formatHasHour = hasHour;
        this.useSimpleFormatter = "yyyyMMddHHmmssSSSZ".equals(format);
    }

    public DateTimeFormatter getDateFormatter() {
        if (dateFormatter == null && format != null && !formatMillis && !formatISO8601 && !formatUnixTime) {
            if (locale == null) {
                dateFormatter = DateTimeFormatter.ofPattern(format);
            } else {
                dateFormatter = DateTimeFormatter.ofPattern(format, locale);
            }
        }
        return dateFormatter;
    }

    public DateTimeFormatter getDateFormatter(Locale locale) {
        if (format == null || formatMillis || formatISO8601 || formatUnixTime) {
            return null;
        }

        if (dateFormatter != null) {
            if ((this.locale == null && (locale == null || locale == Locale.getDefault()))
                    || this.locale != null && this.locale.equals(locale)
            ) {
                return dateFormatter;
            }
        }

        if (locale == null) {
            if (this.locale == null) {
                return dateFormatter = DateTimeFormatter.ofPattern(format);
            } else {
                return dateFormatter = DateTimeFormatter.ofPattern(format, this.locale);
            }
        }

        return dateFormatter = DateTimeFormatter.ofPattern(format, locale);
    }
}
