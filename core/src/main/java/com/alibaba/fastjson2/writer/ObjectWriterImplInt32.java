package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

final class ObjectWriterImplInt32
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplInt32 INSTANCE = new ObjectWriterImplInt32();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        int value = (Integer) object;

        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
            return;
        }
        jsonWriter.writeInt32(value);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        int value = (Integer) object;

        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
            return;
        }
        jsonWriter.writeInt32(value);
    }
}
