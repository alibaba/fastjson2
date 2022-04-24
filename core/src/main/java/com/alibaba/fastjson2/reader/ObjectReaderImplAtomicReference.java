package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

final class ObjectReaderImplAtomicReference extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplAtomicReference INSTANCE = new ObjectReaderImplAtomicReference(Object.class);

    final Type referenceType;

    public ObjectReaderImplAtomicReference(Type referenceType) {
        this.referenceType = referenceType;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        Object value = jsonReader.read(referenceType);
        return new AtomicReference<>(value);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        Object value = jsonReader.read(referenceType);
        return new AtomicReference<>(value);
    }
}
