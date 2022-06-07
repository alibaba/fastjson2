package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.LocalTime;
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

        JSONWriter.Context ctx = jsonWriter.getContext();

        LocalTime time = (LocalTime) object;

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

        String str = formatter.format(time);
        jsonWriter.writeString(str);
    }
}
