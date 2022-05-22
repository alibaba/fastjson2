package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.TimeZone;

final class ObjectWriterImplTimeZone
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplTimeZone INSTANCE = new ObjectWriterImplTimeZone();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeString(((TimeZone) object).getID());
    }
}
