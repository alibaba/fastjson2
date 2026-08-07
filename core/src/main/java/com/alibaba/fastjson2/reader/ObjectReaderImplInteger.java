package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;

class ObjectReaderImplInteger
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInteger INSTANCE = new ObjectReaderImplInteger();

    public ObjectReaderImplInteger() {
        super(Integer.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readInt32();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readInt32();
    }
}
