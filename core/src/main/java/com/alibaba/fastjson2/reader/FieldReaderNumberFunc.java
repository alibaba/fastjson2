package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

final class FieldReaderNumberFunc<T, V> extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, V> function;

    public FieldReaderNumberFunc(String fieldName, Class<V> fieldClass, int ordinal, Method method, BiConsumer<T, V> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, null);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object, (V) value);
    }

    @Override
    public void accept(T object, int value) {
        function.accept(object, (V) Integer.valueOf(value));
    }

    @Override
    public void accept(T object, long value) {
        function.accept(object, (V) Long.valueOf(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Number fieldValue = jsonReader.readNumber();
        function.accept(object, (V) fieldValue);
    }
}
