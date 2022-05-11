package com.alibaba.fastjson2.codec;


import java.time.format.DateTimeFormatter;
import java.util.Locale;

public abstract class DateTimeCodec {
    public final String format;
    public final boolean formatUnixTime;
    public final boolean formatMillis;
    public final boolean formatISO8601;
    public final Locale locale;

    DateTimeFormatter dateFormatter;

    public DateTimeCodec(String format) {
        this(format, null);
    }

    public DateTimeCodec(String format, Locale locale) {
        this.format = format;
        this.locale = locale;

        boolean formatUnixTime = false, formatISO8601 = false, formatMillis = false;
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
                    break;
            }
        }
        this.formatUnixTime = formatUnixTime;
        this.formatMillis = formatMillis;
        this.formatISO8601 = formatISO8601;
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
