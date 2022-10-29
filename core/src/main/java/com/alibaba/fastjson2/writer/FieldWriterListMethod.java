package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

final class FieldWriterListMethod<T>
        extends FieldWriterList<T> {
    protected FieldWriterListMethod(
            String fieldName,
            Type itemType,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            Type fieldType,
            Class fieldClass
    ) {
        super(fieldName, itemType, ordinal, features, format, label, fieldType, fieldClass, null, method);
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
        List value;
        try {
            value = (List) getFieldValue(object);
        } catch (JSONException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        long features = this.features | jsonWriter.getFeatures();
        if (value == null) {
            if ((features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullListAsEmpty.mask)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeArrayNull();
                return true;
            } else {
                return false;
            }
        }

        if ((features & NotWriteEmptyArray.mask) != 0 && value.isEmpty()) {
            return false;
        }

        writeList(jsonWriter, true, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        List value = (List) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        writeList(jsonWriter, false, value);
    }
}
