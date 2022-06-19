package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

class ObjectReaderImplNumber
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplNumber INSTANCE = new ObjectReaderImplNumber();

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readNumber();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readNumber();
    }
}
