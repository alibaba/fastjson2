package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static java.time.temporal.ChronoField.SECOND_OF_DAY;
import static java.time.temporal.ChronoField.YEAR;

abstract class FieldWriterDate<T> extends FieldWriterImpl<T> {
    volatile byte[] cacheFormat19UTF8;
    static AtomicReferenceFieldUpdater<FieldWriterDate, byte[]> CACHE_UTF8_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(FieldWriterDate.class, byte[].class, "cacheFormat19UTF8");

    volatile char[] cacheFormat19UTF16;
    static AtomicReferenceFieldUpdater<FieldWriterDate, char[]> CACHE_UTF16_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(FieldWriterDate.class, char[].class, "cacheFormat19UTF16");

    protected DateTimeFormatter formatter;
    boolean formatMillis;
    boolean formatISO8601;
    boolean formatUnixTime;

    protected ObjectWriter dateWriter;

    protected FieldWriterDate(String fieldName, int ordinal, long features, String format, Type fieldType, Class fieldClass) {
        super(fieldName, ordinal, features, format, fieldType, fieldClass);

        boolean formatMillis = false, formatISO8601 = false, formatUnixTime = false;
        if (format != null) {
            switch (format) {
                case "millis":
                    formatMillis = true;
                    break;
                case "iso8601":
                    formatISO8601 = true;
                    break;
                case "unixtime":
                    formatUnixTime = true;
                default:
                    break;
            }
        }

        this.formatMillis = formatMillis;
        this.formatISO8601 = formatISO8601;
        this.formatUnixTime = formatUnixTime;
    }

    @Override
    public boolean isDateFormatMillis() {
        return formatMillis;
    }

    @Override
    public boolean isDateFormatISO8601() {
        return formatISO8601;
    }

    public DateTimeFormatter getFormatter() {
        if (formatter == null
                && format != null
                && !formatMillis
                && !formatISO8601
                && !formatUnixTime
        ) {
            formatter = DateTimeFormatter.ofPattern(format);
        }

        return formatter;
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (valueClass == fieldClass) {
            if (dateWriter == null) {
                if (format == null) {
                    return dateWriter = ObjectWriterImplDate.INSTANCE;
                }
                return dateWriter = new ObjectWriterImplDate(format);
            }

            return dateWriter;
        }

        return jsonWriter.getObjectWriter(valueClass);
    }

    @Override
    public void writeDate(JSONWriter jsonWriter, long timeMillis) {
        if (jsonWriter.isJSONB()) {
            writeFieldName(jsonWriter);
            jsonWriter.writeMillis(timeMillis);
            return;
        }

        final int SECONDS_PER_DAY = 60 * 60 * 24;

        JSONWriter.Context ctx = jsonWriter.getContext();
        if (formatMillis || (format == null && ctx.isDateFormatMillis())) {
            writeFieldName(jsonWriter);
            jsonWriter.writeInt64(timeMillis);
            return;
        }

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            writeFieldName(jsonWriter);
            jsonWriter.writeInt64(timeMillis/1000);
            return;
        }

        ZoneId zoneId = ctx.getZoneId();

        String dateFormat = this.format != null
                ? this.format
                : ctx.getDateFormat();
        if (dateFormat == null) {
            Instant instant = Instant.ofEpochMilli(timeMillis);
            long epochSecond = instant.getEpochSecond();
            ZoneOffset offset = zoneId
                    .getRules()
                    .getOffset(instant);

            long localSecond = epochSecond + offset.getTotalSeconds();
            long localEpochDay = Math.floorDiv(localSecond, (long) SECONDS_PER_DAY);
            int secsOfDay = (int) Math.floorMod(localSecond, (long) SECONDS_PER_DAY);
            int year, month, dayOfMonth;
            {
                final int DAYS_PER_CYCLE = 146097;
                final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

                long zeroDay = localEpochDay + DAYS_0000_TO_1970;
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
                month = (marchMonth0 + 2) % 12 + 1;
                dayOfMonth = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
                yearEst += marchMonth0 / 10;

                // check year now we are certain it is correct
                year = YEAR.checkValidIntValue(yearEst);
            }

            int hour, minute, second;
            {
                final int MINUTES_PER_HOUR = 60;
                final int SECONDS_PER_MINUTE = 60;
                final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

                long secondOfDay = secsOfDay;
                SECOND_OF_DAY.checkValidValue(secondOfDay);
                int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
                secondOfDay -= hours * SECONDS_PER_HOUR;
                int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
                secondOfDay -= minutes * SECONDS_PER_MINUTE;

                hour = hours;
                minute = minutes;
                second = (int) secondOfDay;
            }

            int millis = instant.getNano() / 1000_000;
            if (millis != 0) {
                int offsetSeconds = ctx
                        .getZoneId()
                        .getRules()
                        .getOffset(instant)
                        .getTotalSeconds();
                writeFieldName(jsonWriter);
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, millis, offsetSeconds);
                return;
            }

            if (jsonWriter.isUTF8()) {
                byte[] bytes = CACHE_UTF8_UPDATER.getAndSet(this, null);
                if (bytes == null) {
                    bytes = Arrays.copyOfRange(nameWithColonUTF8, 0, nameWithColonUTF8.length + 21);
                    int off = nameWithColonUTF8.length;
                    bytes[off++] = '"';
                    off += 4;
                    bytes[off] = '-';
                    off += 3;
                    bytes[off] = '-';
                    off += 3;
                    bytes[off] = ' ';
                    off += 3;
                    bytes[off] = ':';
                    off += 3;
                    bytes[off] = ':';
                    off += 3;
                    bytes[off] = '"';
                }
                int off = nameWithColonUTF8.length + 1;
                bytes[off] = (byte) (year / 1000 + '0');
                bytes[off + 1] = (byte) ((year / 100) % 10 + '0');
                bytes[off + 2] = (byte) ((year / 10) % 10 + '0');
                bytes[off + 3] = (byte) (year % 10 + '0');
                bytes[off + 5] = (byte) (month / 10 + '0');
                bytes[off + 6] = (byte) (month % 10 + '0');
                bytes[off + 8] = (byte) (dayOfMonth / 10 + '0');
                bytes[off + 9] = (byte) (dayOfMonth % 10 + '0');
                bytes[off + 11] = (byte) (hour / 10 + '0');
                bytes[off + 12] = (byte) (hour % 10 + '0');
                bytes[off + 14] = (byte) (minute / 10 + '0');
                bytes[off + 15] = (byte) (minute % 10 + '0');
                bytes[off + 17] = (byte) (second / 10 + '0');
                bytes[off + 18] = (byte) (second % 10 + '0');

                try {
                    jsonWriter.writeNameRaw(bytes);
                } finally {
                    CACHE_UTF8_UPDATER.set(this, bytes);
                }
                return;
            }
            if (jsonWriter.isUTF16()) {
                char[] chars = CACHE_UTF16_UPDATER.getAndSet(this, null);
                if (chars == null) {
                    chars = Arrays.copyOfRange(nameWithColonUTF16, 0, nameWithColonUTF16.length + 21);
                    int off = nameWithColonUTF16.length;
                    chars[off++] = '"';
                    off += 4;
                    chars[off] = '-';
                    off += 3;
                    chars[off] = '-';
                    off += 3;
                    chars[off] = ' ';
                    off += 3;
                    chars[off] = ':';
                    off += 3;
                    chars[off] = ':';
                    off += 3;
                    chars[off] = '"';
                }
                int off = nameWithColonUTF16.length + 1;
                chars[off] = (char) (year / 1000 + '0');
                chars[off + 1] = (char) ((year / 100) % 10 + '0');
                chars[off + 2] = (char) ((year / 10) % 10 + '0');
                chars[off + 3] = (char) (year % 10 + '0');
                chars[off + 5] = (char) (month / 10 + '0');
                chars[off + 6] = (char) (month % 10 + '0');
                chars[off + 8] = (char) (dayOfMonth / 10 + '0');
                chars[off + 9] = (char) (dayOfMonth % 10 + '0');
                chars[off + 11] = (char) (hour / 10 + '0');
                chars[off + 12] = (char) (hour % 10 + '0');
                chars[off + 14] = (char) (minute / 10 + '0');
                chars[off + 15] = (char) (minute % 10 + '0');
                chars[off + 17] = (char) (second / 10 + '0');
                chars[off + 18] = (char) (second % 10 + '0');

                try {
                    jsonWriter.writeNameRaw(chars);
                } finally {
                    CACHE_UTF16_UPDATER.set(this, chars);
                }
                return;
            }

            writeFieldName(jsonWriter);
            jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
        } else {
            writeFieldName(jsonWriter);

            ZonedDateTime zdt = ZonedDateTime
                    .ofInstant(
                            Instant.ofEpochMilli(timeMillis), zoneId);

            if (isDateFormatISO8601() || ctx.isDateFormatISO8601()) {
                int year = zdt.getYear();
                int month = zdt.getMonthValue();
                int dayOfMonth = zdt.getDayOfMonth();
                int hour = zdt.getHour();
                int minute = zdt.getMinute();
                int second = zdt.getSecond();
                int millis = zdt.getNano() / 1000_000;
                int offsetSeconds = zdt.getOffset().getTotalSeconds();
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, millis, offsetSeconds);
                return;
            }

            DateTimeFormatter formatter = this.getFormatter();
            if (formatter == null) {
                formatter = ctx.getDateFormatter();
            }

            String str = formatter.format(zdt);

            jsonWriter.writeString(str);
        }
    }
}
