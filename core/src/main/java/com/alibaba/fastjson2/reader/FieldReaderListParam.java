package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Locale;

public class FieldReaderListParam
        extends FieldReaderList {
    final Parameter parameter;
    final String paramName;
    final long paramNameHash;

    public FieldReaderListParam(
            String fieldName,
            Type fieldType,
            String paramName,
            Parameter parameter,
            Class fieldClass,
            Type itemType,
            Class itemClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema
    ) {
        super(fieldName, fieldType, fieldClass, itemType, itemClass, ordinal, features, format, locale, defaultValue, schema, null, null, null);

        this.paramName = paramName;
        this.paramNameHash = Fnv.hashCode64(paramName);
        this.parameter = parameter;
    }
}
