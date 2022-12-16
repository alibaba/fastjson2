package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ObjectWriterImplLocalDateTime
        extends DateTimeCodec
        implements ObjectWriter {
    static final ObjectWriterImplLocalDateTime INSTANCE = new ObjectWriterImplLocalDateTime(null, null);

    public ObjectWriterImplLocalDateTime(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeLocalDateTime((LocalDateTime) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.context;

        LocalDateTime ldt = (LocalDateTime) object;

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            long millis = ldt.atZone(ctx.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis / 1000);
            return;
        }

        if (formatMillis || (format == null && ctx.isDateFormatMillis())) {
            long millis = ldt.atZone(ctx.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis);
            return;
        }

        int year = ldt.getYear();
        if (year >= 0 && year <= 9999) {
            if (formatISO8601 || (format == null && ctx.isDateFormatISO8601())) {
                int month = ldt.getMonthValue();
                int dayOfMonth = ldt.getDayOfMonth();
                int hour = ldt.getHour();
                int minute = ldt.getMinute();
                int second = ldt.getSecond();
                int nano = ldt.getNano() / 1000_000;
                int offsetSeconds = ctx.getZoneId().getRules().getOffset(ldt).getTotalSeconds();
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano, offsetSeconds, true);
                return;
            }

            if (yyyyMMddhhmmss19) {
                jsonWriter.writeDateTime19(
                        year,
                        ldt.getMonthValue(),
                        ldt.getDayOfMonth(),
                        ldt.getHour(),
                        ldt.getMinute(),
                        ldt.getSecond()
                );
                return;
            }

            if (yyyyMMddhhmmss14) {
                jsonWriter.writeDateTime14(
                        year,
                        ldt.getMonthValue(),
                        ldt.getDayOfMonth(),
                        ldt.getHour(),
                        ldt.getMinute(),
                        ldt.getSecond()
                );
                return;
            }

            if (yyyyMMdd8) {
                jsonWriter.writeDateYYYMMDD8(
                        year,
                        ldt.getMonthValue(),
                        ldt.getDayOfMonth());
                return;
            }

            if (yyyyMMdd10) {
                jsonWriter.writeDateYYYMMDD10(
                        year,
                        ldt.getMonthValue(),
                        ldt.getDayOfMonth());
                return;
            }
        }

        DateTimeFormatter formatter = this.getDateFormatter();
        if (formatter == null) {
            formatter = ctx.getDateFormatter();
        }

        if (formatter == null) {
            jsonWriter.writeLocalDateTime(ldt);
            return;
        }

        String str = formatter.format(ldt);
        jsonWriter.writeString(str);
    }
}
