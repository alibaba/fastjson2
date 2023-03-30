package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLongArray;

final class ObjectWriterImplAtomicLongArray
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplAtomicLongArray INSTANCE = new ObjectWriterImplAtomicLongArray();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        AtomicLongArray array = (AtomicLongArray) object;

        jsonWriter.startArray(array.length());
        for (int i = 0; i < array.length(); ++i) {
            jsonWriter.writeInt64(array.get(i));
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        AtomicLongArray array = (AtomicLongArray) object;

        jsonWriter.startArray();
        for (int i = 0; i < array.length(); ++i) {
            if (i != 0) {
                jsonWriter.writeComma();
            }
            jsonWriter.writeInt64(array.get(i));
        }
        jsonWriter.endArray();
    }
}
