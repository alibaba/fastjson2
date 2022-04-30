package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

final class ObjectWriterImplDate extends DateTimeCodec implements ObjectWriter {
    static final ObjectWriterImplDate INSTANCE = new ObjectWriterImplDate(null);
    static final ObjectWriterImplDate INSTANCE_UNIXTIME = new ObjectWriterImplDate("unixtime");

    public ObjectWriterImplDate(String format) {
        super (format);
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
                jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
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
