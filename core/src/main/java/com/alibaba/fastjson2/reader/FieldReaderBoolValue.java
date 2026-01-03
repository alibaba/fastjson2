package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjBoolConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderBoolValue<T>
        extends FieldReaderObject<T> {
    public FieldReaderBoolValue(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Boolean defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjBoolConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, boolean.class, boolean.class, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        boolean fieldValue = jsonReader.readBoolValue();

        propertyAccessor.setBooleanValue(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBoolValue();
    }

    @Override
    public void accept(T object, Object value) {
        boolean booleanValue = TypeUtils.toBooleanValue(value);

        propertyAccessor.setBooleanValue(object, booleanValue);
    }
}
