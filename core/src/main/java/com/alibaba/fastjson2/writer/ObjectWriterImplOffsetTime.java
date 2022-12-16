package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;

final class ObjectWriterImplOffsetTime
        extends DateTimeCodec
        implements ObjectWriter {
    static final ObjectWriterImplOffsetTime INSTANCE = new ObjectWriterImplOffsetTime(null, null);

    public ObjectWriterImplOffsetTime(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.context;

        OffsetTime time = (OffsetTime) object;

        DateTimeFormatter formatter = this.getDateFormatter();
        if (formatter == null) {
            formatter = ctx.getDateFormatter();
        }

        if (formatter == null) {
            int hour = time.get(ChronoField.HOUR_OF_DAY);
            int minute = time.get(ChronoField.MINUTE_OF_HOUR);
            int second = time.get(ChronoField.SECOND_OF_MINUTE);
            jsonWriter.writeTimeHHMMSS8(hour, minute, second);
        } else {
            String str = formatter.format(time);
            jsonWriter.writeString(str);
        }
    }
}
