package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.OptionalDouble;

class ObjectReaderImplOptionalDouble
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplOptionalDouble INSTANCE = new ObjectReaderImplOptionalDouble();

    public ObjectReaderImplOptionalDouble() {
        super(OptionalDouble.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Double value = jsonReader.readDouble();
        if (value == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value.doubleValue());
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Double value = jsonReader.readDouble();
        if (value == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value.doubleValue());
    }
}
