package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;

final class ObjectReaderImplString
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplString INSTANCE = new ObjectReaderImplString();

    public ObjectReaderImplString() {
        super(String.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readString();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readString();
    }
}
