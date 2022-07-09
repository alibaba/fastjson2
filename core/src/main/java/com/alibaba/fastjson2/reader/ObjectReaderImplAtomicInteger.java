package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

final class ObjectReaderImplAtomicInteger
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplAtomicInteger INSTANCE = new ObjectReaderImplAtomicInteger();

    @Override
    public Class getObjectClass() {
        return AtomicInteger.class;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        int value = jsonReader.readInt32Value();
        return new AtomicInteger(value);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        int value = jsonReader.readInt32Value();
        return new AtomicInteger(value);
    }
}
