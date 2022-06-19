package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.util.OptionalLong;

class ObjectReaderImplOptionalLong
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplOptionalLong INSTANCE = new ObjectReaderImplOptionalLong();

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        Long integer = jsonReader.readInt64();
        if (integer == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(integer.longValue());
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        Long integer = jsonReader.readInt64();
        if (integer == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(integer.longValue());
    }
}
