package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

final class ObjectReaderImplAtomicReference
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplAtomicReference INSTANCE = new ObjectReaderImplAtomicReference(Object.class);

    final Type referenceType;

    public ObjectReaderImplAtomicReference(Type referenceType) {
        super(AtomicReference.class);
        this.referenceType = referenceType;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        Object value = jsonReader.read(referenceType);
        return new AtomicReference<>(value);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        Object value = jsonReader.read(referenceType);
        return new AtomicReference<>(value);
    }
}
