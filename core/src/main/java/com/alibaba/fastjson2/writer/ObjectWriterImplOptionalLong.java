package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.OptionalLong;

final class ObjectWriterImplOptionalLong
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplOptionalLong INSTANCE = new ObjectWriterImplOptionalLong();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        OptionalLong optionalLong = (OptionalLong) object;
        if (!optionalLong.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeInt64(optionalLong.getAsLong());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        OptionalLong optionalLong = (OptionalLong) object;
        if (!optionalLong.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        long value = optionalLong.getAsLong();
        jsonWriter.writeInt64(value);
    }
}
