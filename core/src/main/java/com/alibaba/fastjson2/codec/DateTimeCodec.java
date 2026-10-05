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
    protected final boolean yyyyMMddhhmm16;
    protected final boolean yyyyMMddhhmmss14;
    protected final boolean yyyyMMdd10;
    protected final boolean yyyyMMdd8;
    protected final boolean useSimpleDateFormat;

    DateTimeFormatter dateFormatter;

    public DateTimeCodec(String format) {
        this(format, null);
    }

    public DateTimeCodec(String format, Locale locale) {
        if (format != null) {
            format = format.replace("aa", "a");
        }

        this.format = format;
        this.locale = locale;
        this.yyyyMMddhhmmss14 = "yyyyMMddHHmmss".equals(format);
        this.yyyyMMddhhmmss19 = "yyyy-MM-dd HH:mm:ss".equals(format);
        this.yyyyMMddhhmm16 = "yyyy-MM-dd HH:mm".equals(format);
        this.yyyyMMdd10 = "yyyy-MM-dd".equals(format);
        this.yyyyMMdd8 = "yyyyMMdd".equals(format);
        this.useSimpleDateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX".equals(format);

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

    /**
     * Returns a formatter with the requested locale without changing the cached default formatter.
     * A null locale uses this codec's configured locale or the default locale.
     * <details><summary>中文</summary>
     * 返回使用指定区域设置的格式化器，不改变缓存的默认格式化器。
     * 区域设置为 null 时使用此编解码器的区域设置或默认区域设置。
     * </details>
     *
     * @param locale the locale override, or null to use the codec's locale
     * @return the formatter, or null for a missing format or a special timestamp format
     */
    public DateTimeFormatter getDateFormatter(Locale locale) {
        DateTimeFormatter formatter = getDateFormatter();
        return formatter == null || locale == null ? formatter : formatter.withLocale(locale);
    }
}
