package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchema;
import com.alibaba.fastjson2.function.ObjShortConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;

final class FieldReaderInt16ValueFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final ObjShortConsumer<T> function;

    public FieldReaderInt16ValueFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Short defaultValue,
            JSONSchema schema,
            Method method,
            ObjShortConsumer<T> function
    ) {
        super(fieldName, short.class, short.class, ordinal, features, format, locale, defaultValue, schema);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, short value) {
        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object
                , TypeUtils.toShortValue(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        short fieldInt = (short) jsonReader.readInt32Value();

        if (schema != null) {
            schema.assertValidate(fieldInt);
        }

        function.accept(object, fieldInt);
    }
}
