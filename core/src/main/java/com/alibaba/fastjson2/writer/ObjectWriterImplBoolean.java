package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

final class ObjectWriterImplBoolean
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplBoolean INSTANCE = new ObjectWriterImplBoolean();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        write(jsonWriter, object, fieldName, fieldType, features);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeBooleanNull();
            return;
        }

        boolean value = (Boolean) object;
        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
            return;
        }

        jsonWriter.writeBool(value);
    }
}
