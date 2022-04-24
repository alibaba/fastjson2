package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.Optional;

final class ObjectWriterImplOptional extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplOptional INSTANCE = new ObjectWriterImplOptional();

    Type valueType;
    long features;

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Optional optional = (Optional) object;
        if (!optional.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        Object value = optional.get();
        ObjectWriter objectWriter = jsonWriter.getObjectWriter(value.getClass());
        objectWriter.writeJSONB(jsonWriter, value, fieldName, null, features);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Optional optional = (Optional) object;
        if (!optional.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        Object value = optional.get();
        ObjectWriter objectWriter = jsonWriter.getObjectWriter(value.getClass());
        objectWriter.write(jsonWriter, value, fieldName, valueType, this.features);
    }
}
