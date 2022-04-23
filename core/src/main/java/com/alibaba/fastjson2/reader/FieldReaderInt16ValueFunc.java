package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjShortConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderInt16ValueFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final ObjShortConsumer<T> function;

    public FieldReaderInt16ValueFunc(String fieldName, int ordinal, Method method, ObjShortConsumer<T> function) {
        super(fieldName, short.class, short.class, ordinal, 0, null);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, short value) {
        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object
                , TypeUtils.toShortValue(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        function.accept(object
                , (short) jsonReader.readInt32Value());
    }
}
