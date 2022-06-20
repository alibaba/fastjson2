package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

class ObjectReaderImplInteger
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplInteger INSTANCE = new ObjectReaderImplInteger();

    @Override
    public Class getObjectClass() {
        return Integer.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readInt32();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readInt32();
    }
}
