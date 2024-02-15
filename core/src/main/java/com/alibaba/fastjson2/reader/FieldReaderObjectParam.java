package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Locale;

class FieldReaderObjectParam<T>
        extends FieldReaderObject<T> {
    final String paramName;
    final long paramNameHash;

    FieldReaderObjectParam(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            String paramName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, null, null, null);
        this.paramName = paramName;
        this.paramNameHash = Fnv.hashCode64(paramName);
    }

    @Override
    public void accept(T object, Object value) {
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        throw new JSONException("UnsupportedOperationException");
    }
}
