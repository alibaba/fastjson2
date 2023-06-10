package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.BiConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;

final class FieldReaderDoubleFunc<T>
        extends FieldReader<T> {
    final BiConsumer<T, Double> function;

    public FieldReaderDoubleFunc(
            String fieldName,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Double defaultValue,
            Method method,
            BiConsumer<T, Double> function
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        Double doubleValue = TypeUtils.toDouble(value);
        function.accept(object, doubleValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Double fieldValue;
        try {
            fieldValue = jsonReader.readDouble();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        function.accept(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readDouble();
    }
}
