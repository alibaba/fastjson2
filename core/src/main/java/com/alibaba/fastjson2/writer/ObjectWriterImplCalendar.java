package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.time.Instant;
import com.alibaba.fastjson2.time.ZoneId;
import com.alibaba.fastjson2.time.ZonedDateTime;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

final class ObjectWriterImplCalendar
        extends DateTimeCodec implements ObjectWriter {
    static final ObjectWriterImplCalendar INSTANCE = new ObjectWriterImplCalendar(null, null);

    public ObjectWriterImplCalendar(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        long millis = ((Calendar) object).getTimeInMillis();
        jsonWriter.writeMillis(millis);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.context;

        Calendar calendar = (Calendar) object;
        long millis = calendar.getTimeInMillis();

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            jsonWriter.writeInt64(millis / 1000L);
            return;
        }

        if (format == null && ctx.isDateFormatMillis()) {
            jsonWriter.writeInt64(millis);
            return;
        }

        ZoneId zoneId = ctx.getZoneId();
        Instant instant = Instant.ofEpochMilli(millis);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
        int offsetSeconds = zdt.offsetSeconds;

        final int year = zdt.dateTime.date.year;
        if (year >= 0 && year <= 9999) {
            if (format == null && ctx.isDateFormatISO8601()) {
                int month = zdt.dateTime.date.monthValue;
                int dayOfMonth = zdt.dateTime.date.dayOfMonth;
                int hour = zdt.dateTime.time.hour;
                int minute = zdt.dateTime.time.minute;
                int second = zdt.dateTime.time.second;
                int nano = zdt.dateTime.time.nano / (1000 * 1000);
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano, offsetSeconds, true);
                return;
            }

            String dateFormat = format == null ? ctx.getDateFormat() : format;
            if (dateFormat == null) {
                int month = zdt.dateTime.date.monthValue;
                int dayOfMonth = zdt.dateTime.date.dayOfMonth;
                int hour = zdt.dateTime.time.hour;
                int minute = zdt.dateTime.time.minute;
                int second = zdt.dateTime.time.second;

                int nano = zdt.dateTime.time.nano;
                if (nano == 0) {
                    jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
                } else {
                    jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano / 1000_000, offsetSeconds, false);
                }
                return;
            }
        }

        String format = this.format;
        if (format == null) {
            format = jsonWriter.context.getDateFormat();
        }

        SimpleDateFormat fmt = new SimpleDateFormat(format);
        String str = fmt.format(calendar.getTime());
        jsonWriter.writeString(str);
    }
}
