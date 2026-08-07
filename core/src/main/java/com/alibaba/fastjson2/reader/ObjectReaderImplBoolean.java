package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;

class ObjectReaderImplBoolean
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplBoolean INSTANCE = new ObjectReaderImplBoolean();

    ObjectReaderImplBoolean() {
        super(Boolean.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readBool();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readBool();
    }
}
