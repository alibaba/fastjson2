package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjBoolConsumer;

import java.lang.reflect.Method;

final class FieldReaderBoolValFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final ObjBoolConsumer<T> function;

    public FieldReaderBoolValFunc(String fieldName, int ordinal, Method method, ObjBoolConsumer<T> function) {
        super(fieldName, boolean.class, boolean.class, ordinal, 0, null);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            value = false;
        }
        function.accept(object, (Boolean) value);
    }

    @Override
    public void accept(T object, boolean value) {
        function.accept(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        function.accept(object
                , jsonReader.readBoolValue());
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
