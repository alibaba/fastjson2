package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Function;

final class FieldWriterEnumFunc extends FieldWriterEnum {
    final Type fieldType;
    final Method method;
    final Function function;

    protected FieldWriterEnumFunc(
            String name
            , int ordinal
            , long features
            , Type fieldType
            , Class fieldClass
            , Method method
            , Function function) {
        super(name, ordinal, features, null, fieldClass);
        this.fieldType = fieldType;
        this.method = method;
        this.function = function;
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object o) {
        Enum value = (Enum) function.apply(o);

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        writeEnum(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        Enum value = (Enum) function.apply(object);
        jsonWriter.writeEnum(value);
    }
}
