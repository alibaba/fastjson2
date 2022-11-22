package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderFloatFunc<T>
        extends FieldReader<T> {
    final BiConsumer<T, Float> function;

    public FieldReaderFloatFunc(
            String fieldName,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Float defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, Float> function
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        Float floatValue = TypeUtils.toFloat(value);

        if (schema != null) {
            schema.assertValidate(floatValue);
        }

        function.accept(object, floatValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Float fieldValue;
        try {
            fieldValue = jsonReader.readFloat();
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

        function.accept(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readFloatValue();
    }
}
