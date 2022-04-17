package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.OffsetTime;
import java.time.temporal.ChronoField;

final class ObjectWriterImplOffsetTime extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplOffsetTime INSTANCE = new ObjectWriterImplOffsetTime();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.getContext();

        OffsetTime time = (OffsetTime) object;

        String dateFormat = ctx.getDateFormat();
        if (dateFormat == null) {
            int hour = time.get(ChronoField.HOUR_OF_DAY);
            int minute = time.get(ChronoField.MINUTE_OF_HOUR);
            int second = time.get(ChronoField.SECOND_OF_MINUTE);
            jsonWriter.writeTimeHHMMSS8(hour, minute, second);
        } else {
            String str = ctx.getDateFormatter().format(time);
            jsonWriter.writeString(str);
        }
    }
}
