package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

class ObjectWriterImplBoolean
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplBoolean INSTANCE = new ObjectWriterImplBoolean();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeBooleanNull();
            return;
        }
        boolean value = ((Boolean) object).booleanValue();
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
        } else {
            jsonWriter.writeBool(value);
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeBooleanNull();
            return;
        }
        boolean value = ((Boolean) object).booleanValue();
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
        } else {
            jsonWriter.writeBool(value);
        }
    }
}
