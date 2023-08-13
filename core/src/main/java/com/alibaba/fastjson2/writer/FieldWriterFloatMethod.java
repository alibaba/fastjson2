package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

class FieldWriterFloatMethod<T>
        extends FieldWriter<T> {
    protected FieldWriterFloatMethod(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method);
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
    public boolean write(JSONWriter jsonWriter, T object) {
        Float value;
        try {
            value = (Float) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = jsonWriter.getFeatures(this.features);
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0
                    && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) == 0
            ) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNumberNull();
                return true;
            }
            return false;
        }

        writeFieldName(jsonWriter);

        float floatValue = value;
        if (decimalFormat != null) {
            jsonWriter.writeFloat(floatValue, decimalFormat);
        } else {
            jsonWriter.writeFloat(floatValue);
        }

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Float value = (Float) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
        } else {
            float floatValue = value;
            if (decimalFormat != null) {
                jsonWriter.writeFloat(floatValue, decimalFormat);
            } else {
                jsonWriter.writeFloat(floatValue);
            }
        }
    }
}
