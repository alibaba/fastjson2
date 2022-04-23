package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

final class FieldReaderStringFunc<T, V> extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, V> function;

    final String format;
    final boolean trim;

    FieldReaderStringFunc(String fieldName, Class<V> fieldClass, int ordinal, long features, String format, Method method, BiConsumer<T, V> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, null);
        this.method = method;
        this.function = function;

        this.format = format;
        trim = "trim".equals(format);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, Object value) {
        String fieldValue = (String) value;
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        function.accept(object, (V) fieldValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();

        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readString();
    }
}
