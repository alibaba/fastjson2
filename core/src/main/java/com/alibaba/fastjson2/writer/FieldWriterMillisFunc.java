package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.util.function.ToLongFunction;

final class FieldWriterMillisFunc<T> extends FieldWriterDate<T> {
    final Method method;
    final ToLongFunction function;

    FieldWriterMillisFunc(String fieldName
            , int ordinal
            , long features
            , String dateTimeFormat
            , Method method
            , ToLongFunction function
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, long.class, long.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.applyAsLong(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long millis = function.applyAsLong(object);
        writeDate(jsonWriter, millis);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        long millis = function.applyAsLong(object);
        writeDate(jsonWriter, false, millis);
    }
}
