package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Locale;

final class FieldReaderDateField<T>
        extends FieldReaderImplDate<T> {
    FieldReaderDateField(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Date defaultValue,
            JSONSchema schema,
            Field field
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, null, field);
    }

    @Override
    public void accept(T object, Date value) {
        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
