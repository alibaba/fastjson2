package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

final class ObjectWriterImplZonedDateTime extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplZonedDateTime INSTANCE = new ObjectWriterImplZonedDateTime(null);
    static final ObjectWriterImplZonedDateTime INSTANCE_UNIXTIME = new ObjectWriterImplZonedDateTime("unixtime");

    protected final String format;
    protected final boolean formatUnixTime;
    protected final boolean formatMillis;
    protected final boolean formatISO8601;

    DateTimeFormatter dateFormatter;

    public ObjectWriterImplZonedDateTime(String format) {
        this.format = format;

        boolean formatUnixTime = false, formatISO8601 = false, formatMillis = false;
        if (format != null) {
            switch (format) {
                case "unixtime":
                    formatUnixTime = true;
                    break;
                case "iso8601":
                    formatISO8601 = true;
                    break;
                case "millis":
                    formatMillis = true;
                    break;
                default:
                    break;
            }
        }
        this.formatUnixTime = formatUnixTime;
        this.formatMillis = formatMillis;
        this.formatISO8601 = formatISO8601;
    }

    public DateTimeFormatter getDateFormatter() {
        if (dateFormatter == null && format != null && !formatMillis && !formatISO8601 && !formatUnixTime) {
            dateFormatter = DateTimeFormatter.ofPattern(format);
        }
        return dateFormatter;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeZonedDateTime((ZonedDateTime) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        ZonedDateTime zdt = (ZonedDateTime) object;

        JSONWriter.Context ctx = jsonWriter.getContext();

        if (formatUnixTime || ctx.isDateFormatUnixTime()) {
            long millis = zdt.toInstant().toEpochMilli();
            jsonWriter.writeInt64(millis / 1000);
            return;
        }


        if (ctx.isDateFormatMillis()) {
            jsonWriter.writeInt64(zdt
                    .toInstant()
                    .toEpochMilli());
            return;
        }

        String dateFormat = ctx.getDateFormat();
        if (dateFormat == null) {
            jsonWriter.writeZonedDateTime(zdt);
        } else {
            String str = ctx.getDateFormatter().format(zdt);
            jsonWriter.writeString(str);
        }
    }
}
