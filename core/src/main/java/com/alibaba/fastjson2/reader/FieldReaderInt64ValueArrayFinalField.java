package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;

final class FieldReaderInt64ValueArrayFinalField<T>
        extends FieldReaderObject<T> {
    FieldReaderInt64ValueArrayFinalField(String fieldName, Class fieldType, int ordinal, long features, String format, long[] defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, null, defaultValue, schema, null, field, null);
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
            array = (long[]) propertyAccessor.getObject(object);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }

        if (jsonReader.nextIfArrayStart()) {
            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfArrayEnd()) {
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
