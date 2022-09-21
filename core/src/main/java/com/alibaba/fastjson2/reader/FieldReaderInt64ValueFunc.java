package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.function.ObjLongConsumer;

final class FieldReaderInt64ValueFunc<T>
        extends FieldReader<T> {
    final ObjLongConsumer<T> function;

    public FieldReaderInt64ValueFunc(String fieldName, int ordinal, Long defaultValue, JSONSchema schema, Method method, ObjLongConsumer<T> function) {
        super(fieldName, long.class, long.class, ordinal, 0, null, null, defaultValue, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        long longValue = TypeUtils.toLongValue(value);

        if (schema != null) {
            schema.assertValidate(longValue);
        }

        function.accept(object, longValue);
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldValue = jsonReader.readInt64Value();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        function.accept(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64Value();
    }
}
