package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplInt32
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt32 INSTANCE = new ObjectWriterImplInt32();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(((Integer) object).intValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(((Integer) object).intValue());
    }
}
