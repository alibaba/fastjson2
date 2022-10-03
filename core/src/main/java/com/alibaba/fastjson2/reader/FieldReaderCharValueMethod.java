package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjCharConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldReaderCharValueMethod<T>
        extends FieldReader<T> {

    FieldReaderCharValueMethod(String fieldName, int ordinal, String format, Character defaultValue, JSONSchema schema, Method method) {
        super(fieldName, char.class, char.class, ordinal, 0, format, null, defaultValue, schema, method, null);
    }

    @Override
    public void accept(T object, Object value) {
        char charValue;
        if (value instanceof String) {
            charValue = ((String) value).charAt(0);
        } else if (value instanceof Character) {
            charValue = (Character) value;
        } else {
            throw new JSONException("cast to char error");
        }
        accept(object, charValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        char ch = jsonReader.readCharValue();
        if (ch == '\0' && jsonReader.wasNull()) {
            return;
        }
        try {
            method.invoke(object, Character.valueOf(ch));
        } catch (Exception e) {
            throw new JSONException("invoke method error " + method + ", " + e.getMessage(), e);
        }
    }

    @Override
    public String readFieldValue(JSONReader jsonReader) {
        return jsonReader.readString();
    }
}
