package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.UUID;

class ObjectReaderImplUUID
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplUUID INSTANCE = new ObjectReaderImplUUID();

    public ObjectReaderImplUUID() {
        super(UUID.class);
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
