package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;

class ObjectReaderImplNumber
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplNumber INSTANCE = new ObjectReaderImplNumber();

    public ObjectReaderImplNumber() {
        super(Number.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readNumber();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readNumber();
    }
}
