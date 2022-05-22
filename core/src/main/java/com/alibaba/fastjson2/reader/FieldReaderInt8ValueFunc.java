package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjByteConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderInt8ValueFunc<T>
        extends FieldReaderImpl<T> {
    final Method method;
    final ObjByteConsumer<T> function;

    public FieldReaderInt8ValueFunc(String fieldName, int ordinal, JSONSchema schema, Method method, ObjByteConsumer<T> function) {
        super(fieldName, byte.class, byte.class, ordinal, 0, null, null, null, schema);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, byte value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object,
                TypeUtils.toByteValue(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        byte value = (byte) jsonReader.readInt32Value();

        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (byte) jsonReader.readInt32Value();
    }
}
