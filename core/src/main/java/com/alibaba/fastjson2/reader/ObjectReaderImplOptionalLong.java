package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.OptionalLong;

class ObjectReaderImplOptionalLong
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplOptionalLong INSTANCE = new ObjectReaderImplOptionalLong();

    public ObjectReaderImplOptionalLong() {
        super(OptionalLong.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Long integer = jsonReader.readInt64();
        if (integer == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(integer.longValue());
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Long integer = jsonReader.readInt64();
        if (integer == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(integer.longValue());
    }
}
