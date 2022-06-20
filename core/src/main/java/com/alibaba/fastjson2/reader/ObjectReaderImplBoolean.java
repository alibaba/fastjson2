package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

class ObjectReaderImplBoolean
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplBoolean INSTANCE = new ObjectReaderImplBoolean();

    @Override
    public Class getObjectClass() {
        return Boolean.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readBool();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readBool();
    }
}
