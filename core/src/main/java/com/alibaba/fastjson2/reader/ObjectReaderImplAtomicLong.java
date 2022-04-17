package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.util.concurrent.atomic.AtomicLong;

final class ObjectReaderImplAtomicLong extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplAtomicLong INSTANCE = new ObjectReaderImplAtomicLong();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        long value = jsonReader.readInt64Value();
        return new AtomicLong(value);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        long value = jsonReader.readInt64Value();
        return new AtomicLong(value);
    }
}
