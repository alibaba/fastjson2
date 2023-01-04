package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;

class ObjectReaderImplDouble
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplDouble INSTANCE = new ObjectReaderImplDouble();

    ObjectReaderImplDouble() {
        super(Double.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readDouble();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readDouble();
    }
}
