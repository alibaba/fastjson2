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

    /**
     * Constructs a DateTimeCodec with the specified format pattern.
     * Uses the default locale for date/time formatting.
     *
     * @param format the date/time format pattern (e.g., "yyyy-MM-dd HH:mm:ss", "unixtime", "millis")
     */
    public DateTimeCodec(String format) {
        this(format, null);
    }

    /**
     * Constructs a DateTimeCodec with the specified format pattern and locale.
     * Supports special formats including "unixtime", "millis", "iso8601", and standard date/time patterns.
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * // Standard format
     * DateTimeCodec codec1 = new DateTimeCodec("yyyy-MM-dd HH:mm:ss");
     *
     * // Unix timestamp
     * DateTimeCodec codec2 = new DateTimeCodec("unixtime");
     *
     * // Milliseconds
     * DateTimeCodec codec3 = new DateTimeCodec("millis");
     *
     * // With locale
     * DateTimeCodec codec4 = new DateTimeCodec("yyyy-MM-dd", Locale.US);
     * }</pre>
     *
     * @param format the date/time format pattern
     * @param locale the locale to use for formatting, null for default locale
     */
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

    /**
     * Returns a DateTimeFormatter instance for the configured format pattern.
     * The formatter is lazily initialized and cached for reuse.
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * DateTimeCodec codec = new DateTimeCodec("yyyy-MM-dd HH:mm:ss");
     * DateTimeFormatter formatter = codec.getDateFormatter();
     * String formatted = formatter.format(LocalDateTime.now());
     * }</pre>
     *
     * @return a DateTimeFormatter for the configured pattern, or null if format is null or uses special formats
     */
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
     * Returns a DateTimeFormatter instance for the configured format pattern with the specified locale.
     * Reuses the cached formatter if the locale matches, otherwise creates a new formatter.
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * DateTimeCodec codec = new DateTimeCodec("yyyy-MM-dd");
     * DateTimeFormatter formatter = codec.getDateFormatter(Locale.FRANCE);
     * String formatted = formatter.format(LocalDate.now());
     * }</pre>
     *
     * @param locale the locale to use for formatting, null to use the codec's default locale
     * @return a DateTimeFormatter for the configured pattern with the specified locale, or null if format is null or uses special formats
     */
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
