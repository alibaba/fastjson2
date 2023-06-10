package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.BiConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;

final class FieldReaderInt16Func<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    public FieldReaderInt16Func(
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
        Short shortValue = TypeUtils.toShort(value);
        function.accept(object, (V) shortValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Short fieldValue;
        try {
            Integer value = jsonReader.readInt32();
            fieldValue = value == null ? null : value.shortValue();
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
        return (short) jsonReader.readInt32Value();
    }
}
