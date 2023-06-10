package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjShortConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;

final class FieldReaderInt16ValueFunc<T>
        extends FieldReader<T> {
    final ObjShortConsumer<T> function;

    public FieldReaderInt16ValueFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Short defaultValue,
            Method method,
            ObjShortConsumer<T> function
    ) {
        super(fieldName, short.class, short.class, ordinal, features, format, locale, defaultValue, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, short value) {
        function.accept(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        short shortValue = TypeUtils.toShortValue(value);
        function.accept(object, shortValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        short fieldInt = (short) jsonReader.readInt32Value();
        function.accept(object, fieldInt);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (short) jsonReader.readInt32Value();
    }
}
