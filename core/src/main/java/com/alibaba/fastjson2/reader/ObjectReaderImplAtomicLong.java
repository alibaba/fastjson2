package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

final class ObjectReaderImplAtomicLong
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplAtomicLong INSTANCE = new ObjectReaderImplAtomicLong();

    public ObjectReaderImplAtomicLong() {
        super(AtomicLong.class);
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        long value = jsonReader.readInt64Value();
        return new AtomicLong(value);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        long value = jsonReader.readInt64Value();
        return new AtomicLong(value);
    }
}
