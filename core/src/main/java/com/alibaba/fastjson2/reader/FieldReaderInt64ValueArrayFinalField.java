package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;

final class FieldReaderInt64ValueArrayFinalField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt64ValueArrayFinalField(String fieldName, Class fieldType, int ordinal, long features, String format, long[] defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (jsonReader.readIfNull()) {
            return;
        }

        long[] array;
        try {
            array = (long[]) field.get(object);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }

        if (jsonReader.nextIfMatch('[')) {
            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                long value = jsonReader.readInt64Value();
                if (array != null && i < array.length) {
                    array[i] = value;
                }
            }
        }
    }
}
