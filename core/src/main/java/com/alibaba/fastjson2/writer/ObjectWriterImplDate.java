package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

final class ObjectWriterImplDate
        extends DateTimeCodec
        implements ObjectWriter {
    static final ObjectWriterImplDate INSTANCE = new ObjectWriterImplDate(null, null);

    static final char[] PREFIX_CHARS = "new Date(".toCharArray();
    static final byte[] PREFIX_BYTES = "new Date(".getBytes(StandardCharsets.UTF_8);

    static final char[] PREFIX_CHARS_SQL = "{\"@type\":\"java.sql.Date\",\"val\":".toCharArray();
    static final byte[] PREFIX_BYTES_SQL = "{\"@type\":\"java.sql.Date\",\"val\":".getBytes(StandardCharsets.UTF_8);

    public ObjectWriterImplDate(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeMillis(
                ((Date) object).getTime());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.context;

        Date date = (Date) object;
        long millis = date.getTime();

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            char end = ')';
            if (jsonWriter.utf16) {
                char[] prefix;
                if ("java.sql.Date".equals(date.getClass().getName())) {
                    prefix = PREFIX_CHARS_SQL;
                    end = '}';
                } else {
                    prefix = PREFIX_CHARS;
                }
                jsonWriter.writeRaw(prefix, 0, prefix.length);
            } else {
                byte[] prefix;
                if ("java.sql.Date".equals(date.getClass().getName())) {
                    prefix = PREFIX_BYTES_SQL;
                    end = '}';
                } else {
                    prefix = PREFIX_BYTES;
                }
                jsonWriter.writeRaw(prefix);
            }
            jsonWriter.writeInt64(millis);
            jsonWriter.writeRaw(end);
            return;
        }

        if (formatMillis || (format == null && ctx.isDateFormatMillis())) {
            jsonWriter.writeInt64(millis);
            return;
        }

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            jsonWriter.writeInt64(millis / 1000);
            return;
        }

        ZoneId zoneId = ctx.getZoneId();
        int offsetSeconds;

        if (zoneId == IOUtils.SHANGHAI_ZONE_ID || zoneId.getRules() == IOUtils.SHANGHAI_ZONE_RULES) {
            offsetSeconds = IOUtils.getShanghaiZoneOffsetTotalSeconds(
                    Math.floorDiv(millis, 1000L)
            );
        } else if (zoneId == ZoneOffset.UTC || "UTC".equals(zoneId.getId())) {
            offsetSeconds = 0;
        } else {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId);
            offsetSeconds = zdt.getOffset().getTotalSeconds();
        }

        String dateFormat;
        boolean formatISO8601 = this.formatISO8601 || ctx.isDateFormatISO8601();
        if (formatISO8601) {
            dateFormat = null;
        } else {
            dateFormat = this.format;
            if (dateFormat == null) {
                dateFormat = ctx.getDateFormat();
            }
        }

        if (dateFormat == null) {
            final int SECONDS_PER_DAY = 60 * 60 * 24;

            long epochSecond = Math.floorDiv(millis, 1000L);
            int offsetTotalSeconds;
            if (zoneId == IOUtils.SHANGHAI_ZONE_ID || zoneId.getRules() == IOUtils.SHANGHAI_ZONE_RULES) {
                offsetTotalSeconds = IOUtils.getShanghaiZoneOffsetTotalSeconds(epochSecond);
            } else {
                Instant instant = Instant.ofEpochMilli(millis);
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
                int mos = (int) Math.floorMod(millis, 1000L);
                if (mos == 0 && !formatISO8601) {
                    if (hour == 0 && minute == 0 && second == 0 && "java.sql.Date".equals(date.getClass().getName())) {
                        jsonWriter.writeDateYYYMMDD10(year, month, dayOfMonth);
                    } else {
                        jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
                    }
                } else {
                    jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, mos, offsetSeconds, formatISO8601);
                }
                return;
            }
        }

        DateTimeFormatter formatter;
        if (this.format != null) {
            formatter = getDateFormatter();
        } else {
            formatter = ctx.getDateFormatter();
        }
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId);
        String str = formatter.format(zdt);
        jsonWriter.writeString(str);
    }
}
