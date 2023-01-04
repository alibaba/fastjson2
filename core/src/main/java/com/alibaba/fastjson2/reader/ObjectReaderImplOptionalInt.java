package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.OptionalInt;

final class ObjectReaderImplOptionalInt
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplOptionalInt INSTANCE = new ObjectReaderImplOptionalInt();

    public ObjectReaderImplOptionalInt() {
        super(OptionalInt.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Integer integer = jsonReader.readInt32();
        if (integer == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(integer.intValue());
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Integer integer = jsonReader.readInt32();
        if (integer == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(integer.intValue());
    }
}
