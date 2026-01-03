package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.ObjIntConsumer;

final class FieldReaderInt32Value<T>
        extends FieldReaderObject<T> {
    public FieldReaderInt32Value(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Integer defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjIntConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, int.class, int.class, ordinal, features, format, locale, defaultValue, schema, method, field,
                function, paramName, parameter, null);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int fieldValue = jsonReader.readInt32Value();

        propertyAccessor.setIntValue(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        int intValue = TypeUtils.toIntValue(value);

        propertyAccessor.setIntValue(object, intValue);
    }
}
