package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjCharConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderCharValue<T>
        extends FieldReaderObject<T> {
    public FieldReaderCharValue(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Character defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjCharConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, method, field,
                function, paramName, parameter);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readString();
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        char ch = jsonReader.readCharValue();
        if (ch == '\0' && jsonReader.wasNull()) {
            return;
        }

        if (schema != null) {
            schema.assertValidate(ch);
        }

        propertyAccessor.setChar(object, ch);
    }

    @Override
    public void accept(T object, char value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setChar(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        char charValue;
        if (value instanceof String) {
            String str = (String) value;
            if (str.length() > 0) {
                charValue = str.charAt(0);
            } else {
                charValue = '\0';
            }
        } else if (value instanceof Character) {
            charValue = (Character) value;
        } else {
            throw new JSONException("cast to char error");
        }

        if (schema != null) {
            schema.assertValidate(charValue);
        }

        propertyAccessor.setChar(object, charValue);
    }
}
