package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.BiConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;

final class FieldReaderInt64Func<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    public FieldReaderInt64Func(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            Method method,
            BiConsumer<T, V> function
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        Long longValue = TypeUtils.toLong(value);
        function.accept(object, (V) longValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long fieldValue;
        try {
            fieldValue = jsonReader.readInt64();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }
        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64();
    }
}
