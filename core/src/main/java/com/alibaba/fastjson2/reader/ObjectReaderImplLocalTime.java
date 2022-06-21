package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.time.LocalTime;

class ObjectReaderImplLocalTime
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplLocalTime INSTANCE = new ObjectReaderImplLocalTime();

    @Override
    public Class getObjectClass() {
        return LocalTime.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readLocalTime();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readLocalTime();
    }
}
