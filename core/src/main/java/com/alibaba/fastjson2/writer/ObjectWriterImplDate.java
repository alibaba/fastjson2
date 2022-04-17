package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

final class ObjectWriterImplDate extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplDate INSTANCE = new ObjectWriterImplDate(null);

    final String format;
    final boolean formatMillis;
    final boolean formatISO8601;

    DateTimeFormatter dateFormatter;

    public ObjectWriterImplDate(String format) {
        this.format = format;
        this.formatMillis = "millis".equals(format);
        this.formatISO8601 = "iso8601".equalsIgnoreCase(format);
    }

    public DateTimeFormatter getFormatter() {
        if (dateFormatter == null && format != null && !formatMillis && !formatISO8601) {
            dateFormatter = DateTimeFormatter.ofPattern(format);
        }
        return dateFormatter;
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

        if (formatMillis || ctx.isDateFormatMillis()) {
            jsonWriter.writeInt64(millis);
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
                jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
            } else {
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano / 1000_000, offsetSeconds);
            }
            return;
        }

        DateTimeFormatter formatter;
        if (this.format != null) {
            formatter = getFormatter();
        } else {
            formatter = ctx.getDateFormatter();
        }
        String str = formatter.format(zdt);
        jsonWriter.writeString(str);
    }


}
