package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplCharacter
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplCharacter INSTANCE = new ObjectWriterImplCharacter();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        char ch = (Character) object;

        jsonWriter.writeChar(ch);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        char ch = (Character) object;

        jsonWriter.writeChar(ch);
    }
}
