package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderOffsetDateTime
        extends FieldReaderObject {
    public FieldReaderOffsetDateTime(
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
        initReader = ObjectReaderImplOffsetDateTime.of(format, locale);
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
        OffsetDateTime dateTime;
        if (format != null) {
            dateTime = (OffsetDateTime) initReader.readObject(jsonReader);
        } else {
            dateTime = jsonReader.readOffsetDateTime();
        }
        accept(object, dateTime);
    }
}
