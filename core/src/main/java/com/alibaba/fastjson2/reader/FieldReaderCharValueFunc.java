package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjCharConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;

final class FieldReaderCharValueFunc<T>
        extends FieldReader<T> {
    final ObjCharConsumer<T> function;

    FieldReaderCharValueFunc(String fieldName, int ordinal, String format, Character defaultValue, JSONSchema schema, Method method, ObjCharConsumer<T> function) {
        super(fieldName, char.class, char.class, ordinal, 0, format, null, defaultValue, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, char value) {
        function.accept(object, value);
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
        function.accept(object, ch);
    }

    @Override
    public String readFieldValue(JSONReader jsonReader) {
        return jsonReader.readString();
    }
}
