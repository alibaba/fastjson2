package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.util.concurrent.atomic.AtomicInteger;

final class ObjectReaderImplAtomicInteger extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplAtomicInteger INSTANCE = new ObjectReaderImplAtomicInteger();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        int value = jsonReader.readInt32Value();
        return new AtomicInteger(value);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        int value = jsonReader.readInt32Value();
        return new AtomicInteger(value);
    }
}
