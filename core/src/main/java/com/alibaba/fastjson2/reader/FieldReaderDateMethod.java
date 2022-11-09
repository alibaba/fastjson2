package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;

final class FieldReaderDateMethod<T>
        extends FieldReaderImplDate<T> {
    FieldReaderDateMethod(
            String fieldName,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            JSONSchema schema,
            Method method
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, null, schema, method, null);
    }

    @Override
    public void accept(T object, Date value) {
        try {
            method.invoke(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
