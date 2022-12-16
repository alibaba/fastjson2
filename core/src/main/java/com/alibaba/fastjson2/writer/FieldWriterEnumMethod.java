package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterEnumMethod
        extends FieldWriterEnum {
    protected FieldWriterEnumMethod(String name, int ordinal, long features, String format, String label, Class fieldType, Method method) {
        super(name, ordinal, features, format, label, fieldType, null, method);
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + fieldName, e);
        }
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        Enum value = (Enum) getFieldValue(object);
        jsonWriter.writeEnum(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        Enum value = (Enum) getFieldValue(object);

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
}
