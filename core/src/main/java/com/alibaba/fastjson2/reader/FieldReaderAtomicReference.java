package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.*;

public abstract class FieldReaderAtomicReference<T>
        extends FieldReader<T> {
    final Type referenceType;

    public FieldReaderAtomicReference(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            JSONSchema schema,
            Method method,
            Field field
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, null, schema, method, field);

        Type referenceType = null;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) fieldType;
            Type[] arguments = paramType.getActualTypeArguments();
            if (arguments.length == 1) {
                referenceType = arguments[0];
            }
        }
        this.referenceType = referenceType;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (jsonReader.nextIfNull()) {
            return;
        }

        Object refValue = jsonReader.read(referenceType);
        accept(object, refValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.read(referenceType);
    }
}
