package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplClass
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplClass INSTANCE = new ObjectWriterImplClass();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            jsonWriter.writeTypeName("java.lang.Class");
        }

        Class clazz = (Class) object;
        jsonWriter.writeString(clazz.getName());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        Class clazz = (Class) object;
        jsonWriter.writeString(
                clazz.getName());
    }
}
