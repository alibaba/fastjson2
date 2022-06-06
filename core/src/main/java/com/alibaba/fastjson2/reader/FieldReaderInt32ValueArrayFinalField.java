package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;

final class FieldReaderInt32ValueArrayFinalField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt32ValueArrayFinalField(String fieldName, Class fieldType, int ordinal, long features, String format, int[] defaultValue, JSONSchema schema, Field field) {
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

        int[] array;
        try {
            array = (int[]) field.get(object);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }

        if (jsonReader.nextIfMatch('[')) {
            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                int value = jsonReader.readInt32Value();
                if (array != null && i < array.length) {
                    array[i] = value;
                }
            }
        }
    }
}
