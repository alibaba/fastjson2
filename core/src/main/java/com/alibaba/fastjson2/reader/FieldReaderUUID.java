package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.BiConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldReaderUUID
        extends FieldReaderObject {
    public FieldReaderUUID(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            Method method,
            Field field,
            BiConsumer function
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, method, field, function);
        initReader = ObjectReaderImplUUID.INSTANCE;
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
                jsonReader.readUUID()
        );
    }
}
