package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.function.ObjIntConsumer;

final class FieldReaderInt32ValueFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final ObjIntConsumer<T> function;

    public FieldReaderInt32ValueFunc(String fieldName, int ordinal, Method method, ObjIntConsumer<T> function) {
        super(fieldName, int.class, int.class, ordinal, 0, null);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, int value) {
        function.accept(object, value);
    }

    @Override
    public void accept(T object, long value) {
        function.accept(object, (int) value);
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object
                , TypeUtils.toIntValue(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        function.accept(object
                , jsonReader.readInt32Value());
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
