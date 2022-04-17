package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

final class UtilDateWriter implements ObjectWriter {
    final String dateFormat;
    final boolean dateFormatMillis;
    final boolean dateFormatISO8601;
    DateTimeFormatter dateFormatter;

    public UtilDateWriter(String dateFormat) {
        this.dateFormat = dateFormat;
        this.dateFormatMillis = "millis".equals(dateFormat);
        this.dateFormatISO8601 = "iso8601".equalsIgnoreCase(dateFormat);
    }

    public DateTimeFormatter getDateFormatter() {
        if (dateFormatter == null && dateFormat != null && !dateFormatMillis && !dateFormatISO8601) {
            dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
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

        if (dateFormatMillis) {
            jsonWriter.writeInt64(millis);
            return;
        }

        ZoneId zoneId = ctx.getZoneId();
        Instant instant = Instant.ofEpochMilli(millis);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
        int offsetSeconds = zdt.getOffset().getTotalSeconds();

        if (dateFormatISO8601) {
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

        String str = getDateFormatter().format(zdt);
        jsonWriter.writeString(str);
    }
}
