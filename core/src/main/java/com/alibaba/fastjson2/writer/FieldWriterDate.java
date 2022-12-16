package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

abstract class FieldWriterDate<T>
        extends FieldWriter<T> {
    volatile byte[] cacheFormat19UTF8;
    static AtomicReferenceFieldUpdater<FieldWriterDate, byte[]> CACHE_UTF8_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(FieldWriterDate.class, byte[].class, "cacheFormat19UTF8");

    volatile char[] cacheFormat19UTF16;
    static AtomicReferenceFieldUpdater<FieldWriterDate, char[]> CACHE_UTF16_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(FieldWriterDate.class, char[].class, "cacheFormat19UTF16");

    protected DateTimeFormatter formatter;
    final boolean formatMillis;
    final boolean formatISO8601;
    final boolean formatyyyyMMddhhmmss14;
    final boolean formatyyyyMMddhhmmss19;
    final boolean formatUnixTime;

    protected ObjectWriter dateWriter;

    protected FieldWriterDate(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, method);

        boolean formatMillis = false, formatISO8601 = false, formatUnixTime = false;
        boolean formatyyyyMMddhhmmss14 = false, formatyyyyMMddhhmmss19 = false;
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
                    break;
                case "yyyy-MM-dd HH:mm:ss":
                    formatyyyyMMddhhmmss19 = true;
                    break;
                case "yyyyMMddHHmmss":
                    formatyyyyMMddhhmmss14 = true;
                    break;
                default:
                    break;
            }
        }

        this.formatMillis = formatMillis;
        this.formatISO8601 = formatISO8601;
        this.formatUnixTime = formatUnixTime;
        this.formatyyyyMMddhhmmss14 = formatyyyyMMddhhmmss14;
        this.formatyyyyMMddhhmmss19 = formatyyyyMMddhhmmss19;
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
            ObjectWriterProvider provider = jsonWriter.context.provider;
            if (dateWriter == null) {
                if ((provider.userDefineMask & ObjectWriterProvider.TYPE_DATE_MASK) != 0) {
                    dateWriter = provider.getObjectWriter(valueClass, valueClass, false);
                } else {
                    if (format == null) {
                        return dateWriter = ObjectWriterImplDate.INSTANCE;
                    }
                    return dateWriter = new ObjectWriterImplDate(format, null);
                }
            }

            return dateWriter;
        }

        return jsonWriter.getObjectWriter(valueClass);
    }

    @Override
    public void writeDate(JSONWriter jsonWriter, long timeMillis) {
        if (jsonWriter.jsonb) {
            writeFieldName(jsonWriter);
            jsonWriter.writeMillis(timeMillis);
            return;
        }

        final int SECONDS_PER_DAY = 60 * 60 * 24;

        JSONWriter.Context ctx = jsonWriter.context;

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            writeFieldName(jsonWriter);
            jsonWriter.writeInt64(timeMillis / 1000);
            return;
        }

        if (formatMillis || (format == null && ctx.isDateFormatMillis())) {
            writeFieldName(jsonWriter);
            jsonWriter.writeInt64(timeMillis);
            return;
        }

        ZoneId zoneId = ctx.getZoneId();

        String dateFormat = this.format != null
                ? this.format
                : ctx.getDateFormat();
        boolean formatyyyyMMddhhmmss19 = this.formatyyyyMMddhhmmss19 || (ctx.isFormatyyyyMMddhhmmss19() && this.format == null);
        if (dateFormat == null || formatyyyyMMddhhmmss14 || formatyyyyMMddhhmmss19) {
            long epochSecond = Math.floorDiv(timeMillis, 1000L);
            int offsetTotalSeconds;
            if (zoneId == IOUtils.SHANGHAI_ZONE_ID || zoneId.getRules() == IOUtils.SHANGHAI_ZONE_RULES) {
                offsetTotalSeconds = IOUtils.getShanghaiZoneOffsetTotalSeconds(epochSecond);
            } else {
                Instant instant = Instant.ofEpochMilli(timeMillis);
                offsetTotalSeconds = zoneId.getRules().getOffset(instant).getTotalSeconds();
            }

            long localSecond = epochSecond + offsetTotalSeconds;
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
                if (yearEst < Year.MIN_VALUE || yearEst > Year.MAX_VALUE) {
                    throw new DateTimeException("Invalid year " + yearEst);
                }

                year = (int) yearEst;
            }

            int hour, minute, second;
            {
                final int MINUTES_PER_HOUR = 60;
                final int SECONDS_PER_MINUTE = 60;
                final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

                long secondOfDay = secsOfDay;
                if (secondOfDay < 0 || secondOfDay > 86399) {
                    throw new DateTimeException("Invalid secondOfDay " + secondOfDay);
                }
                int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
                secondOfDay -= hours * SECONDS_PER_HOUR;
                int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
                secondOfDay -= minutes * SECONDS_PER_MINUTE;

                hour = hours;
                minute = minutes;
                second = (int) secondOfDay;
            }

            if (year >= 0 && year <= 9999) {
                if (formatyyyyMMddhhmmss14) {
                    writeFieldName(jsonWriter);
                    jsonWriter.writeDateTime14(
                            year,
                            month,
                            dayOfMonth,
                            hour,
                            minute,
                            second
                    );
                    return;
                }

                if (formatyyyyMMddhhmmss19) {
                    writeFieldName(jsonWriter);
                    jsonWriter.writeDateTime19(
                            year,
                            month,
                            dayOfMonth,
                            hour,
                            minute,
                            second
                    );
                    return;
                }

                int millis = (int) Math.floorMod(timeMillis, 1000L);
                if (millis != 0) {
                    Instant instant = Instant.ofEpochMilli(timeMillis);
                    int offsetSeconds = ctx
                            .getZoneId()
                            .getRules()
                            .getOffset(instant)
                            .getTotalSeconds();
                    writeFieldName(jsonWriter);
                    jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, millis, offsetSeconds, false);
                    return;
                }
                writeFieldName(jsonWriter);
                jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
                return;
            }
        }

        writeFieldName(jsonWriter);
        ZonedDateTime zdt = ZonedDateTime
                .ofInstant(
                        Instant.ofEpochMilli(timeMillis), zoneId);

        if (formatISO8601 || (ctx.isDateFormatISO8601() && this.format == null)) {
            int year = zdt.getYear();
            if (year >= 0 && year <= 9999) {
                int month = zdt.getMonthValue();
                int dayOfMonth = zdt.getDayOfMonth();
                int hour = zdt.getHour();
                int minute = zdt.getMinute();
                int second = zdt.getSecond();
                int millis = zdt.getNano() / 1000_000;
                int offsetSeconds = zdt.getOffset().getTotalSeconds();
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, millis, offsetSeconds, true);
                return;
            }
        }

        DateTimeFormatter formatter = this.getFormatter();
        if (formatter == null) {
            formatter = ctx.getDateFormatter();
        }

        String str = formatter.format(zdt);

        jsonWriter.writeString(str);
    }
}
