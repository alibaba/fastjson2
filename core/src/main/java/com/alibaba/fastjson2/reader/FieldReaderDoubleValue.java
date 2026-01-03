package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.ObjDoubleConsumer;

final class FieldReaderDoubleValue<T>
        extends FieldReaderObject<T> {
    public FieldReaderDoubleValue(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Double defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjDoubleConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, double.class, double.class, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();
        propertyAccessor.setDoubleValue(object, fieldValue);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();
        propertyAccessor.setDoubleValue(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readDoubleValue();
    }
}
