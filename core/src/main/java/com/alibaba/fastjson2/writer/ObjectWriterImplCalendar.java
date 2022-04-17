package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

final class ObjectWriterImplCalendar extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplCalendar INSTANCE = new ObjectWriterImplCalendar();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeMillis(
                ((Calendar) object).getTimeInMillis());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.getContext();

        Calendar date = (Calendar) object;
        long millis = date.getTimeInMillis();

        if (ctx.isDateFormatMillis()) {
            jsonWriter.writeInt64(millis);
            return;
        }

        ZoneId zoneId = ctx.getZoneId();
        Instant instant = Instant.ofEpochMilli(millis);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
        int offsetSeconds = zdt.getOffset().getTotalSeconds();

        if (ctx.isDateFormatISO8601()) {
            int year = zdt.getYear();
            int month = zdt.getMonthValue();
            int dayOfMonth = zdt.getDayOfMonth();
            int hour = zdt.getHour();
            int minute = zdt.getMinute();
            int second = zdt.getSecond();
            int nano = zdt.getNano() / (1000 * 1000);
            jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano, offsetSeconds);
            return;
        }

        String dateFormat = ctx.getDateFormat();
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
        } else {
            String str = ctx.getDateFormatter().format(zdt);
            jsonWriter.writeString(str);
        }
    }
}
