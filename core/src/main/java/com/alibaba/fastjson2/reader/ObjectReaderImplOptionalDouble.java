package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.util.OptionalDouble;

class ObjectReaderImplOptionalDouble
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplOptionalDouble INSTANCE = new ObjectReaderImplOptionalDouble();

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        Double value = jsonReader.readDouble();
        if (value == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value.doubleValue());
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        Double value = jsonReader.readDouble();
        if (value == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value.doubleValue());
    }
}
