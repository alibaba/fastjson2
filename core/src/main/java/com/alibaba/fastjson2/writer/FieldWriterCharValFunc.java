package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToCharFunction;

import java.lang.reflect.Method;

final class FieldWriterCharValFunc
        extends FieldWriterImpl {
    final Method method;
    final ToCharFunction function;

    FieldWriterCharValFunc(String fieldName, int ordinal, Method method, ToCharFunction function) {
        super(fieldName, ordinal, 0, null, char.class, char.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        char value = function.applyAsChar(object);
        jsonWriter.writeString(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        char value = function.applyAsChar(object);
        writeFieldName(jsonWriter);
        jsonWriter.writeString(value);
        return true;
    }
}
