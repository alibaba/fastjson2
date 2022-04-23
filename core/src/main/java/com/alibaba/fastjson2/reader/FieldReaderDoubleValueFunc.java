package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.function.ObjDoubleConsumer;

final class FieldReaderDoubleValueFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final ObjDoubleConsumer<T> function;

    public FieldReaderDoubleValueFunc(String fieldName, int ordinal, Method method, ObjDoubleConsumer<T> function) {
        super(fieldName, double.class, double.class, ordinal, 0, null);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, double value) {
        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object
                , TypeUtils.toDoubleValue(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        function.accept(object
                , jsonReader.readDoubleValue());
    }
}
