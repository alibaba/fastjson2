package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.function.ObjLongConsumer;

final class FieldReaderInt64ValueFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final ObjLongConsumer<T> function;

    public FieldReaderInt64ValueFunc(String fieldName, int ordinal, Method method, ObjLongConsumer<T> function) {
        super(fieldName, long.class, long.class, ordinal, 0, null);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, long value) {
        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object
                , TypeUtils.toLongValue(value));
    }

    @Override
    public void accept(T object, int value) {
        function.accept(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        function.accept(object
                , jsonReader.readInt64Value());
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64Value();
    }
}
