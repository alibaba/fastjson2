package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjCharConsumer;

import java.lang.reflect.Method;

final class FieldReaderCharValueFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final ObjCharConsumer<T> function;

    FieldReaderCharValueFunc(String fieldName, int ordinal, Method method, ObjCharConsumer<T> function) {
        super(fieldName, char.class, char.class, ordinal, 0, null);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, char value) {
        function.accept(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        String str = jsonReader.readString();
        if (str == null || str.isEmpty()) {
            return;
        }
        function.accept(object, str.charAt(0));
    }

    @Override
    public String readFieldValue(JSONReader jsonReader) {
        return jsonReader.readString();
    }
}
