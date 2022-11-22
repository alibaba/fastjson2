package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderInt8Func<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    FieldReaderInt8Func(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, V> function
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        Byte byteValue = TypeUtils.toByte(value);

        if (schema != null) {
            schema.assertValidate(byteValue);
        }

        function.accept(object, (V) byteValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Byte fieldValue;
        try {
            Integer value = jsonReader.readInt32();
            fieldValue = value == null ? null : value.byteValue();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32();
    }
}
