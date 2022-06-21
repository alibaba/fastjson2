package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

class ObjectReaderImplDouble
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplDouble INSTANCE = new ObjectReaderImplDouble();

    @Override
    public Class getObjectClass() {
        return Double.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readDouble();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readDouble();
    }
}
