package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjFloatConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderFloatValue<T>
        extends FieldReaderObject<T> {
    public FieldReaderFloatValue(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Float defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjFloatConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, float.class, float.class, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        float fieldValue = jsonReader.readFloatValue();
        propertyAccessor.setFloatValue(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readFloatValue();
    }

    @Override
    public void accept(T object, Object value) {
        float floatValue = TypeUtils.toFloatValue(value);
        propertyAccessor.setFloatValue(object, floatValue);
    }
}
