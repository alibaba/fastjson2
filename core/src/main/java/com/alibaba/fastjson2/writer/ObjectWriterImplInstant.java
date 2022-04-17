package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.Instant;

final class ObjectWriterImplInstant extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInstant INSTANCE = new ObjectWriterImplInstant();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeInstant((Instant) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.getContext();

        Instant instant = (Instant) object;

        String dateFormat = ctx.getDateFormat();
        if (dateFormat == null || ctx.isDateFormatMillis()) {
            jsonWriter.writeInstant(instant);
        } else {
            String str = ctx.getDateFormatter().format(instant);
            jsonWriter.writeString(str);
        }
    }
}
