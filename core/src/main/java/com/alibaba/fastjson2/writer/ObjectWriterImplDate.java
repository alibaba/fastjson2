package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

final class ObjectWriterImplDate
        extends DateTimeCodec
        implements ObjectWriter {
    static final ObjectWriterImplDate INSTANCE = new ObjectWriterImplDate(null, null);

    static final char[] PREFIX_CHARS = "new Date(".toCharArray();
    static final byte[] PREFIX_BYTES = "new Date(".getBytes(StandardCharsets.UTF_8);

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

        JSONWriter.Context ctx = jsonWriter.getContext();

        Date date = (Date) object;
        long millis = date.getTime();

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            if (jsonWriter.isUTF16()) {
                jsonWriter.writeRaw(PREFIX_CHARS);
            } else {
                jsonWriter.writeRaw(PREFIX_BYTES);
            }
            jsonWriter.writeInt64(millis);
            jsonWriter.writeRaw(')');
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
        Instant instant = Instant.ofEpochMilli(millis);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
        int offsetSeconds = zdt.getOffset().getTotalSeconds();

        if (formatISO8601 || ctx.isDateFormatISO8601()) {
            int year = zdt.getYear();
            int month = zdt.getMonthValue();
            int dayOfMonth = zdt.getDayOfMonth();
            int hour = zdt.getHour();
            int minute = zdt.getMinute();
            int second = zdt.getSecond();
            int nano = zdt.getNano() / 1000_000;
            jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano, offsetSeconds);
            return;
        }

        String dateFormat = this.format;
        if (dateFormat == null) {
            dateFormat = ctx.getDateFormat();
        }

        if (dateFormat == null) {
            int year = zdt.getYear();
            int month = zdt.getMonthValue();
            int dayOfMonth = zdt.getDayOfMonth();
            int hour = zdt.getHour();
            int minute = zdt.getMinute();
            int second = zdt.getSecond();
            int nano = zdt.getNano();
            if (nano == 0) {
                if (hour == 0 && minute == 0 && second == 0 && "java.sql.Date".equals(date.getClass().getName())) {
                    jsonWriter.writeDateYYYMMDD10(year, month, dayOfMonth);
                } else {
                    jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
                }
            } else {
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano / 1000_000, offsetSeconds);
            }
            return;
        }

        DateTimeFormatter formatter;
        if (this.format != null) {
            formatter = getDateFormatter();
        } else {
            formatter = ctx.getDateFormatter();
        }
        String str = formatter.format(zdt);
        jsonWriter.writeString(str);
    }
}
