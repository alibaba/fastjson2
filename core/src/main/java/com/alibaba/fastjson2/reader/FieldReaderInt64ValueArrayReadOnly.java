package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderInt64ValueArrayReadOnly<T>
        extends FieldReaderObject<T> {
    public FieldReaderInt64ValueArrayReadOnly(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            long[] defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, method, field,
                null, paramName, parameter, null);
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
            long[] array = new long[8]; // initial size
            int size = 0;

            for (;;) {
                if (jsonReader.nextIfArrayEnd()) {
                    break;
                }

                long value = jsonReader.readInt64Value();

                if (size == array.length) {
                    long[] newArray = new long[array.length + (array.length >> 1)]; // grow by 50%
                    System.arraycopy(array, 0, newArray, 0, array.length);
                    array = newArray;
                }

                array[size++] = value;
            }

            if (size != array.length) {
                long[] newArray = new long[size];
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
            long[] array = new long[8]; // initial size
            int size = 0;

            for (;;) {
                if (jsonReader.nextIfArrayEnd()) {
                    break;
                }

                long value = jsonReader.readInt64Value();

                if (size == array.length) {
                    long[] newArray = new long[array.length + (array.length >> 1)]; // grow by 50%
                    System.arraycopy(array, 0, newArray, 0, array.length);
                    array = newArray;
                }

                array[size++] = value;
            }

            if (size != array.length) {
                long[] newArray = new long[size];
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
        long[] longArray;
        if (value instanceof long[]) {
            longArray = (long[]) value;
        } else if (value instanceof String) {
            // Parse string if possible
            throw new JSONException("can't convert " + value + " to long[]");
        } else {
            throw new JSONException("can't convert " + value + " to long[]");
        }

        propertyAccessor.setObject(object, longArray);
    }
}
