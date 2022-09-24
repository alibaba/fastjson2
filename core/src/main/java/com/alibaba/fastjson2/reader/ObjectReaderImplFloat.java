package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;

class ObjectReaderImplFloat
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplFloat INSTANCE = new ObjectReaderImplFloat();

    ObjectReaderImplFloat() {
        super(Float.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readFloat();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readFloat();
    }
}
