package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

class ObjectReaderImplCharacter
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplCharacter INSTANCE = new ObjectReaderImplCharacter();

    @Override
    public Class getObjectClass() {
        return Character.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        String str = jsonReader.readString();
        if (str == null) {
            return null;
        }
        return str.charAt(0);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        String str = jsonReader.readString();
        if (str == null) {
            return null;
        }
        return str.charAt(0);
    }
}
