package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;

final class FieldReaderInt32ValueArrayFinalField<T>
        extends FieldReader<T> {
    FieldReaderInt32ValueArrayFinalField(String fieldName, Class fieldType, int ordinal, long features, String format, int[] defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, null, defaultValue, schema, null, field, null, null, null);
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
            array = (int[]) propertyAccessor.getObject(object);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }

        if (jsonReader.nextIfArrayStart()) {
            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfArrayEnd()) {
                    break;
                }

                int value = jsonReader.readInt32Value();
                if (array != null && i < array.length) {
                    array[i] = value;
                }
            }
        }
    }

    @Override
    public void accept(T object, Object value) {
        int[] array;
        try {
            array = (int[]) propertyAccessor.getObject(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
        int[] valueArray = (int[]) value;
        System.arraycopy(valueArray, 0, array, 0, valueArray.length);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32ValueArray();
    }
}
