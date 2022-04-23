package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

final class FieldReaderInt64Func<T, V> extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, V> function;

    public FieldReaderInt64Func(String fieldName, Class<V> fieldClass, int ordinal, Method method, BiConsumer<T, V> function) {
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
        function.accept(object
                , (V) TypeUtils.toLong(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long fieldValue = jsonReader.readInt64();
        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64();
    }
}
