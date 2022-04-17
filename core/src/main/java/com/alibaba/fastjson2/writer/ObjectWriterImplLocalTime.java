package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.LocalTime;

final class ObjectWriterImplLocalTime extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplLocalTime INSTANCE = new ObjectWriterImplLocalTime();

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

        String dateFormat = ctx.getDateFormat();
        if (dateFormat == null) {
            int hour = time.getHour();
            int minute = time.getMinute();
            int second = time.getSecond();
            int nano = time.getNano();
            if (nano == 0) {
                jsonWriter.writeTimeHHMMSS8(hour, minute, second);
            } else {
                jsonWriter.writeLocalTime(time);
            }
        } else {
            String str = ctx.getDateFormatter().format(time);
            jsonWriter.writeString(str);
        }
    }
}
