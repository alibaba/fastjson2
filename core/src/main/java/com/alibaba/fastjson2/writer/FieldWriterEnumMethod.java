package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterEnumMethod
        extends FieldWriterEnum {
    FieldWriterEnumMethod(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Class fieldType,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldType, field, method);
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
