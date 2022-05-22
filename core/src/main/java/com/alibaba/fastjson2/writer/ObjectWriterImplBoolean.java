package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

class ObjectWriterImplBoolean
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplBoolean INSTANCE = new ObjectWriterImplBoolean();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeBooleanNull();
            return;
        }
        jsonWriter.writeBool(((Boolean) object).booleanValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeBooleanNull();
            return;
        }
        jsonWriter.writeBool(((Boolean) object).booleanValue());
    }
}
