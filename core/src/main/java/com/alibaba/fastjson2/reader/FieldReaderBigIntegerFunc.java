package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.function.BiConsumer;

final class FieldReaderBigIntegerFunc<T, V> extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, V> function;

    public FieldReaderBigIntegerFunc(String fieldName, Class<V> fieldClass, int ordinal, Method method, BiConsumer<T, V> function) {
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
                , (V) TypeUtils.toBigInteger(value));
    }

    @Override
    public void accept(T object, int value) {
        function.accept(object, (V) BigInteger.valueOf(value));
    }

    @Override
    public void accept(T object, long value) {
        function.accept(object, (V) BigInteger.valueOf(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigInteger fieldValue = jsonReader.readBigInteger();
        function.accept(object, (V) fieldValue);
    }
}
