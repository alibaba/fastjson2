package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.OptionalInt;

final class ObjectWriterImplOptionalInt
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplOptionalInt INSTANCE = new ObjectWriterImplOptionalInt();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        OptionalInt optionalInt = (OptionalInt) object;
        if (!optionalInt.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeInt32(optionalInt.getAsInt());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        OptionalInt optionalInt = (OptionalInt) object;
        if (!optionalInt.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeInt32(optionalInt.getAsInt());
    }
}
