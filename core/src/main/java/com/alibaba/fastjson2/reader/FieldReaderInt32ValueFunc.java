package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjIntConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderInt32ValueFunc<T>
        extends FieldReader<T> {
    final ObjIntConsumer<T> function;

    public FieldReaderInt32ValueFunc(String fieldName, int ordinal, Integer defaultValue, Method method, ObjIntConsumer<T> function) {
        super(fieldName, int.class, int.class, ordinal, 0, null, null, defaultValue, method, null);
        this.function = function;
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
        int intValue = TypeUtils.toIntValue(value);
        function.accept(object, intValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int value = jsonReader.readInt32Value();
        function.accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
