package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ObjectWriterImplLocalTime
        extends DateTimeCodec
        implements ObjectWriter {
    static final ObjectWriterImplLocalTime INSTANCE = new ObjectWriterImplLocalTime(null, null);

    public ObjectWriterImplLocalTime(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeLocalTime((LocalTime) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.context;

        LocalTime time = (LocalTime) object;

        if (formatMillis || (format == null && ctx.isDateFormatMillis())) {
            LocalDateTime dateTime = LocalDateTime.of(
                    LocalDate.of(1970, 1, 1),
                    time
            );
            Instant instant = dateTime.atZone(ctx.getZoneId()).toInstant();
            long millis = instant.toEpochMilli();
            jsonWriter.writeInt64(millis);
            return;
        }

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            LocalDateTime dateTime = LocalDateTime.of(
                    LocalDate.of(1970, 1, 1),
                    time
            );
            Instant instant = dateTime.atZone(ctx.getZoneId()).toInstant();
            int seconds = (int) (instant.toEpochMilli() / 1000);
            jsonWriter.writeInt32(seconds);
            return;
        }

        DateTimeFormatter formatter = this.getDateFormatter();
        if (formatter == null) {
            formatter = ctx.getDateFormatter();
        }

        if (formatter == null) {
            int hour = time.getHour();
            int minute = time.getMinute();
            int second = time.getSecond();
            int nano = time.getNano();
            if (nano == 0) {
                jsonWriter.writeTimeHHMMSS8(hour, minute, second);
            } else {
                jsonWriter.writeLocalTime(time);
            }
            return;
        }

        String str;
        if (formatHasDay || ctx.isDateFormatHasDay()) {
            str = formatter.format(LocalDateTime.of(LocalDate.of(1970, 1, 1), time));
        } else {
            str = formatter.format(time);
        }
        jsonWriter.writeString(str);
    }
}
