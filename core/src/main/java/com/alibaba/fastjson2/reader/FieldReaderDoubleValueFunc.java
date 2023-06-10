package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjDoubleConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderDoubleValueFunc<T>
        extends FieldReader<T> {
    final ObjDoubleConsumer<T> function;

    public FieldReaderDoubleValueFunc(String fieldName, int ordinal, Double defaultValue, Method method, ObjDoubleConsumer<T> function) {
        super(fieldName, double.class, double.class, ordinal, 0, null, null, defaultValue, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, double value) {
        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        double doubleValue = TypeUtils.toDoubleValue(value);
        function.accept(object,
                doubleValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        double value = jsonReader.readDoubleValue();
        function.accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readDoubleValue();
    }
}
