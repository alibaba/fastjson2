package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;

final class ObjectWriterImplZonedDateTime extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplZonedDateTime INSTANCE = new ObjectWriterImplZonedDateTime();

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

        JSONWriter.Context ctx = jsonWriter.getContext();

        ZonedDateTime zdt = (ZonedDateTime) object;
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
