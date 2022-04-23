package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class FieldReaderAtomicReference<T> extends FieldReaderImpl<T> {
    final Type referenceType;

    public FieldReaderAtomicReference(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format);

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

    public void readFieldValue(JSONReader jsonReader, T object) {
        if (jsonReader.nextIfNull()) {
            return;
        }

        Object refValue = jsonReader.read(referenceType);
        accept(object, refValue);
    }

    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.read(referenceType);
    }
}
