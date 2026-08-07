package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderStringArray
        extends FieldReaderObject {
    public FieldReaderStringArray(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer function
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function);
        initReader = ObjectReaderImplStringArray.INSTANCE;
    }

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        return initReader;
    }

    @Override
    public ObjectReader getObjectReader(JSONReader.Context context) {
        return initReader;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, Object object) {
        accept(
                object,
                jsonReader.readStringArray()
        );
    }
}
