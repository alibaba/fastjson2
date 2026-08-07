package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import static com.alibaba.fastjson2.writer.ObjectWriterImplBoolValueArray.JSONB_TYPE_HASH;
import static com.alibaba.fastjson2.writer.ObjectWriterImplBoolValueArray.JSONB_TYPE_NAME_BYTES;

class ObjectWriterImplBoolValueArrayLambda
        extends ObjectWriterPrimitiveImpl {
    private final ToIntFunction functionSize;
    private final BiFunction<Object, Integer, Boolean> functionGet;

    public ObjectWriterImplBoolValueArrayLambda(ToIntFunction functionSize, BiFunction<Object, Integer, Boolean> functionGet) {
        this.functionSize = functionSize;
        this.functionGet = functionGet;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        int size = functionSize.applyAsInt(object);
        jsonWriter.startArray(size);
        for (int i = 0; i < size; i++) {
            boolean value = functionGet.apply(object, i);
            jsonWriter.writeBool(value);
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        int size = functionSize.applyAsInt(object);
        jsonWriter.startArray();
        for (int i = 0; i < size; i++) {
            boolean value = functionGet.apply(object, i);
            if (i != 0) {
                jsonWriter.writeComma();
            }
            jsonWriter.writeBool(value);
        }
        jsonWriter.endArray();
    }
}
