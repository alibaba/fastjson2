package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ObjectWriterImplLocalDate
        extends DateTimeCodec
        implements ObjectWriter {
    static final ObjectWriterImplLocalDate INSTANCE = new ObjectWriterImplLocalDate(null, null);

    public ObjectWriterImplLocalDate(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeLocalDate((LocalDate) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.context;

        LocalDate date = (LocalDate) object;

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MIN);
            long millis = dateTime.atZone(ctx.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis / 1000);
            return;
        }

        if (formatMillis || (format == null && ctx.isDateFormatMillis())) {
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MIN);
            long millis = dateTime.atZone(ctx.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis);
            return;
        }

        if (yyyyMMdd8) {
            jsonWriter.writeDateYYYMMDD8(
                    date.getYear(),
                    date.getMonthValue(),
                    date.getDayOfMonth());
            return;
        }

        if (yyyyMMdd10) {
            jsonWriter.writeDateYYYMMDD10(
                    date.getYear(),
                    date.getMonthValue(),
                    date.getDayOfMonth());
            return;
        }

        if (yyyyMMddhhmmss19) {
            jsonWriter.writeDateTime19(
                    date.getYear(),
                    date.getMonthValue(),
                    date.getDayOfMonth(), 0, 0, 0);
            return;
        }

        DateTimeFormatter formatter = this.getDateFormatter();
        if (formatter == null) {
            formatter = ctx.getDateFormatter();
        }

        if (formatter == null) {
            jsonWriter.writeDateYYYMMDD10(
                    date.getYear(),
                    date.getMonthValue(),
                    date.getDayOfMonth()
            );
            return;
        }

        String str;
        if (formatHasHour || ctx.isDateFormatHasHour()) {
            str = formatter.format(LocalDateTime.of(date, LocalTime.MIN));
        } else {
            str = formatter.format(date);
        }

        jsonWriter.writeString(str);
    }
}
