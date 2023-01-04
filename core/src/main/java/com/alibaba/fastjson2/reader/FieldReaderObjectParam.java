package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

class FieldReaderObjectParam<T>
        extends FieldReaderObject<T> {
    final Parameter parameter;
    final String paramName;
    final long paramNameHash;

    FieldReaderObjectParam(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            String paramName,
            Parameter parameter,
            int ordinal,
            long features,
            String format,
            JSONSchema schema) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, null, schema, null, null, null);
        this.paramName = paramName;
        this.paramNameHash = Fnv.hashCode64(paramName);
        this.parameter = parameter;
    }

    @Override
    public void accept(T object, Object value) {
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        throw new JSONException("UnsupportedOperationException");
    }
}
