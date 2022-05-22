package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.OptionalDouble;

final class ObjectWriterImplOptionalDouble
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplOptionalDouble INSTANCE = new ObjectWriterImplOptionalDouble();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        OptionalDouble optionalDouble = (OptionalDouble) object;
        if (!optionalDouble.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeDouble(optionalDouble.getAsDouble());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        OptionalDouble optionalDouble = (OptionalDouble) object;
        if (!optionalDouble.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeDouble(optionalDouble.getAsDouble());
    }
}
