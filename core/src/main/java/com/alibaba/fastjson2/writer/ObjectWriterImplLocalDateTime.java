package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class ObjectWriterImplLocalDateTime extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplLocalDateTime INSTANCE = new ObjectWriterImplLocalDateTime();

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
