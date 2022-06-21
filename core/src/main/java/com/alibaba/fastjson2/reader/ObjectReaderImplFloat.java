package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

class ObjectReaderImplFloat
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplFloat INSTANCE = new ObjectReaderImplFloat();

    @Override
    public Class getObjectClass() {
        return Float.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readFloat();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readFloat();
    }
}
