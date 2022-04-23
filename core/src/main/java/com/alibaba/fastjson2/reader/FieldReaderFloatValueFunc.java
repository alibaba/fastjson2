package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjFloatConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderFloatValueFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final ObjFloatConsumer<T> function;

    public FieldReaderFloatValueFunc(String fieldName, int ordinal, Method method, ObjFloatConsumer<T> function) {
        super(fieldName, float.class, float.class, ordinal, 0, null);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, float value) {
        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        try {
            method.invoke(object
                    , TypeUtils.toFloatValue(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        function.accept(object
                , jsonReader.readFloatValue());
    }
}
