package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToLongFunction;

import java.lang.reflect.Method;

final class FieldWriterMillisFunc<T>
        extends FieldWriterDate<T> {
    final ToLongFunction function;

    FieldWriterMillisFunc(String fieldName,
            int ordinal,
            long features,
            String dateTimeFormat,
            String label,
            Method method,
            ToLongFunction function
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, label, long.class, long.class, null, method);
        this.function = function;
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
