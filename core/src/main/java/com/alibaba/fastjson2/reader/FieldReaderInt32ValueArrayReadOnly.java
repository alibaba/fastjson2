package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderInt32ValueArrayReadOnly<T>
        extends FieldReaderObject<T> {
    public FieldReaderInt32ValueArrayReadOnly(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            int[] defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, method, field,
                null, paramName, parameter);
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

        if (jsonReader.nextIfArrayStart()) {
            int[] array = new int[8]; // initial size
            int size = 0;

            for (;;) {
                if (jsonReader.nextIfArrayEnd()) {
                    break;
                }

                int value = jsonReader.readInt32Value();

                if (size == array.length) {
                    int[] newArray = new int[array.length + (array.length >> 1)]; // grow by 50%
                    System.arraycopy(array, 0, newArray, 0, array.length);
                    array = newArray;
                }

                array[size++] = value;
            }

            if (size != array.length) {
                int[] newArray = new int[size];
                System.arraycopy(array, 0, newArray, 0, size);
                array = newArray;
            }

            if (schema != null) {
                schema.assertValidate(array);
            }

            propertyAccessor.setObject(object, array);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfArrayStart()) {
            int[] array = new int[8]; // initial size
            int size = 0;

            for (;;) {
                if (jsonReader.nextIfArrayEnd()) {
                    break;
                }

                int value = jsonReader.readInt32Value();

                if (size == array.length) {
                    int[] newArray = new int[array.length + (array.length >> 1)]; // grow by 50%
                    System.arraycopy(array, 0, newArray, 0, array.length);
                    array = newArray;
                }

                array[size++] = value;
            }

            if (size != array.length) {
                int[] newArray = new int[size];
                System.arraycopy(array, 0, newArray, 0, size);
                array = newArray;
            }

            if (schema != null) {
                schema.assertValidate(array);
            }

            return array;
        }

        return null;
    }

    @Override
    public void accept(T object, Object value) {
        int[] intArray;
        if (value instanceof int[]) {
            intArray = (int[]) value;
        } else if (value instanceof String) {
            // Parse string if possible
            throw new JSONException("can't convert " + value + " to int[]");
        } else {
            throw new JSONException("can't convert " + value + " to int[]");
        }

        if (schema != null) {
            schema.assertValidate(intArray);
        }

        propertyAccessor.setObject(object, intArray);
    }
}
