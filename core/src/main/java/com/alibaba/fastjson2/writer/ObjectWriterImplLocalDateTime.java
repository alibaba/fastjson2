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

        JSONWriter.Context ctx = jsonWriter.getContext();

        LocalDateTime dateTime = (LocalDateTime) object;

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            long millis = dateTime.atZone(ctx.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis / 1000);
            return;
        }

        if (formatMillis || (format == null && ctx.isDateFormatMillis())) {
            long millis = dateTime.atZone(ctx.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis);
            return;
        }

        if (formatISO8601 || (format == null && ctx.isDateFormatISO8601())) {
            int year = dateTime.getYear();
            int month = dateTime.getMonthValue();
            int dayOfMonth = dateTime.getDayOfMonth();
            int hour = dateTime.getHour();
            int minute = dateTime.getMinute();
            int second = dateTime.getSecond();
            int nano = dateTime.getNano() / 1000_000;
            int offsetSeconds = ctx.getZoneId().getRules().getOffset(dateTime).getTotalSeconds();
            jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano, offsetSeconds);
            return;
        }

        DateTimeFormatter formatter = this.getDateFormatter();
        if (formatter == null) {
            formatter = ctx.getDateFormatter();
        }

        if (formatter == null) {
            jsonWriter.writeLocalDateTime(dateTime);
            return;
        }

        String str = formatter.format(dateTime);
        jsonWriter.writeString(str);
    }
}
