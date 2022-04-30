package com.alibaba.fastjson2.codec;


import java.time.format.DateTimeFormatter;

public abstract class DateTimeCodec {
    public final String format;
    public final boolean formatUnixTime;
    public final boolean formatMillis;
    public final boolean formatISO8601;

    DateTimeFormatter dateFormatter;

    public DateTimeCodec(String format) {
        this.format = format;

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
            dateFormatter = DateTimeFormatter.ofPattern(format);
        }
        return dateFormatter;
    }
}
