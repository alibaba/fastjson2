package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplString
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static ObjectWriterImplString INSTANCE = new ObjectWriterImplString();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeString((String) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeString((String) object);
    }
}
