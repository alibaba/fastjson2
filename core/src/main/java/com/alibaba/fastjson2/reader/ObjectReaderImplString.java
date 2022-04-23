package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

final class ObjectReaderImplString extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplString INSTANCE = new ObjectReaderImplString();

    @Override
    public Class getObjectClass() {
        return String.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readString();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readString();
    }
}
