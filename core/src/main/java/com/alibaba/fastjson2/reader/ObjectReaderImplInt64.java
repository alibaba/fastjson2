package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

class ObjectReaderImplInt64
        extends ObjectReaderBaseModule.PrimitiveImpl<Long> {
    static final ObjectReaderImplInt64 INSTANCE = new ObjectReaderImplInt64();

    @Override
    public Long readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readInt64();
    }

    @Override
    public Long readObject(JSONReader jsonReader, long features) {
        return jsonReader.readInt64();
    }
}
