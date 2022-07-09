package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.UUID;

class ObjectReaderImplUUID
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplUUID INSTANCE = new ObjectReaderImplUUID();

    @Override
    public Class getObjectClass() {
        return UUID.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readUUID();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readUUID();
    }
}
