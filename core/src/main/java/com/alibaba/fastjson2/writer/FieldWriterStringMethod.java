package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_NON_FIELD_GETTER;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_EMPTY_ARRAY;
import static com.alibaba.fastjson2.JSONWriter.MASK_NULL_AS_DEFAULT_VALUE;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_MAP_NULL_VALUE;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_NULL_STRING_AS_EMPTY;

final class FieldWriterStringMethod<T>
        extends FieldWriter<T> {
    FieldWriterStringMethod(
            String fieldName,
            int ordinal,
            String format,
            String label,
            long features,
            Field field,
            Method method
    ) {
        super(fieldName, ordinal, features, format, null, label, String.class, String.class, field, method);
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
    public void writeValue(JSONWriter jsonWriter, T object) {
        String value = (String) getFieldValue(object);

        if (trim && value != null) {
            value = value.trim();
        }

        if (symbol && jsonWriter instanceof JSONWriterJSONB) {
            jsonWriter.writeSymbol(value);
        } else {
            if (raw) {
                jsonWriter.writeRaw(value);
            } else {
                jsonWriter.writeString(value);
            }
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        String value;
        try {
            value = (String) getFieldValue(object);
        } catch (JSONException error) {
            if ((jsonWriter.getFeatures(features) | MASK_IGNORE_NON_FIELD_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        long features = this.features | jsonWriter.getFeatures();
        if (value == null) {
            if ((features & (MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_STRING_AS_EMPTY)) == 0) {
                return false;
            }
        } else if (trim) {
            value = value.trim();
        }

        if (value != null
                && value.isEmpty()
                && (features & MASK_NOT_WRITE_EMPTY_ARRAY) != 0
        ) {
            return false;
        }

        writeString(jsonWriter, value);
        return true;
    }
}
