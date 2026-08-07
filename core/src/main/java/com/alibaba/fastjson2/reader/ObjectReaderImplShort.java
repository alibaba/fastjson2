package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

public final class ObjectReaderImplShort
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplShort INSTANCE = new ObjectReaderImplShort();

    public static final long HASH_TYPE = Fnv.hashCode64("S");

    public ObjectReaderImplShort() {
        super(Short.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Integer i = jsonReader.readInt32();
        if (i == null) {
            return null;
        }
        return i.shortValue();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Integer i = jsonReader.readInt32();
        if (i == null) {
            return null;
        }
        return i.shortValue();
    }
}
