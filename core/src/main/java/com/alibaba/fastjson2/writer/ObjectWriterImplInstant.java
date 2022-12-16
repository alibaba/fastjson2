package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ObjectWriterImplInstant
        extends DateTimeCodec
        implements ObjectWriter {
    static final ObjectWriterImplInstant INSTANCE = new ObjectWriterImplInstant(null, null);
    public ObjectWriterImplInstant(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeInstant((Instant) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context context = jsonWriter.context;

        String dateFormat = this.format != null
                ? this.format
                : context.getDateFormat();

        Instant instant = (Instant) object;
        if (dateFormat == null) {
            jsonWriter.writeInstant(instant);
            return;
        }

        boolean yyyyMMddhhmmss19 = this.yyyyMMddhhmmss19 || (context.isFormatyyyyMMddhhmmss19() && this.format == null);
        if (yyyyMMddhhmmss14 || yyyyMMddhhmmss19 || yyyyMMdd8 || yyyyMMdd10) {
            final int SECONDS_PER_DAY = 60 * 60 * 24;
            ZoneId zoneId = context.getZoneId();
            long epochSecond = instant.getEpochSecond();
            int offsetTotalSeconds;
            if (zoneId == IOUtils.SHANGHAI_ZONE_ID || zoneId.getRules() == IOUtils.SHANGHAI_ZONE_RULES) {
                offsetTotalSeconds = IOUtils.getShanghaiZoneOffsetTotalSeconds(epochSecond);
            } else {
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

            if (yyyyMMddhhmmss19) {
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

            if (yyyyMMddhhmmss14) {
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

            if (yyyyMMdd10) {
                jsonWriter.writeDateYYYMMDD10(
                        year,
                        month,
                        dayOfMonth
                );
                return;
            }

            if (yyyyMMdd8) {
                jsonWriter.writeDateYYYMMDD8(
                        year,
                        month,
                        dayOfMonth
                );
                return;
            }
        }

        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());

        if (formatUnixTime || (format == null && context.isDateFormatUnixTime())) {
            long millis = zdt.toInstant().toEpochMilli();
            jsonWriter.writeInt64(millis / 1000);
            return;
        }

        if (formatMillis || (format == null && context.isDateFormatMillis())) {
            jsonWriter.writeInt64(zdt
                    .toInstant()
                    .toEpochMilli());
            return;
        }

        int year = zdt.getYear();
        if (year >= 0 && year <= 9999) {
            if (formatISO8601 || (format == null && context.isDateFormatISO8601())) {
                jsonWriter.writeDateTimeISO8601(
                        year,
                        zdt.getMonthValue(),
                        zdt.getDayOfMonth(),
                        zdt.getHour(),
                        zdt.getMinute(),
                        zdt.getSecond(),
                        zdt.getNano() / 1000_000,
                        zdt.getOffset().getTotalSeconds(),
                        true
                );
                return;
            }

            if (yyyyMMdd8) {
                jsonWriter.writeDateYYYMMDD8(
                        year,
                        zdt.getMonthValue(),
                        zdt.getDayOfMonth());
                return;
            }

            if (yyyyMMdd10) {
                jsonWriter.writeDateYYYMMDD10(
                        year,
                        zdt.getMonthValue(),
                        zdt.getDayOfMonth());
                return;
            }
        }

        DateTimeFormatter formatter = this.getDateFormatter();
        if (formatter == null) {
            formatter = context.getDateFormatter();
        }

        if (formatter == null) {
            jsonWriter.writeZonedDateTime(zdt);
        } else {
            String str = formatter.format(zdt);
            jsonWriter.writeString(str);
        }
    }
}
