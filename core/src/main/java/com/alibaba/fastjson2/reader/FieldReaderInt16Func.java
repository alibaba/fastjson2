package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

final class FieldReaderInt16Func<T, V> extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, V> function;

    public FieldReaderInt16Func(String fieldName, Class<V> fieldClass, int ordinal, String format, Object defaultValue,Method method, BiConsumer<T, V> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, format, defaultValue);
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
                , (V) TypeUtils.toShort(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Integer value = jsonReader.readInt32();
        Short fieldValue = value == null ? null : value.shortValue();
        function.accept(object, (V) fieldValue);
    }
}
