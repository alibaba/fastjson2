package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class ObjectWriterImplLocalDateTime extends DateTimeCodec implements ObjectWriter {
    static final ObjectWriterImplLocalDateTime INSTANCE = new ObjectWriterImplLocalDateTime(null);
    static final ObjectWriterImplLocalDateTime INSTANCE_UNIXTIME = new ObjectWriterImplLocalDateTime("unixtime");

    public ObjectWriterImplLocalDateTime(String format) {
        super(format);
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

        if (formatUnixTime || ctx.isDateFormatUnixTime()) {
            long millis = dateTime.atZone(ctx.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis / 1000);
            return;
        }

        if (formatMillis || ctx.isDateFormatMillis()) {
            long millis = dateTime.atZone(ctx.getZoneId())
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis);
            return;
        }

        String dateFormat = ctx.getDateFormat();
        if (dateFormat == null) {
            jsonWriter.writeLocalDateTime(dateTime);
            return;
        }
        DateTimeFormatter formatter = ctx.getDateFormatter();
        if (formatter == null) {
            jsonWriter.writeLocalDateTime(dateTime);
            return;
        }
        String str = formatter.format(dateTime);
        jsonWriter.writeString(str);
    }
}
