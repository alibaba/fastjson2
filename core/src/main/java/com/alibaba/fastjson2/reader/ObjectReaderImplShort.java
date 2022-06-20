package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

public final class ObjectReaderImplShort
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplShort INSTANCE = new ObjectReaderImplShort();

    public static final long HASH_TYPE = Fnv.hashCode64("S");

    @Override
    public Class getObjectClass() {
        return Short.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        Integer i = jsonReader.readInt32();
        if (i == null) {
            return null;
        }
        return i.shortValue();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        Integer i = jsonReader.readInt32();
        if (i == null) {
            return null;
        }
        return i.shortValue();
    }
}
