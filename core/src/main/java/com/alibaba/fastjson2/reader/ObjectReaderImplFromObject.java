package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.function.Function;

public final class ObjectReaderImplFromObject<T>
        extends ObjectReaderPrimitive<T> {
    final Function creator;
    final ObjectReader valueReader;

    public ObjectReaderImplFromObject(Class<T> objectClass, Function creator, ObjectReader valueReader) {
        super(objectClass);
        this.creator = creator;
        this.valueReader = valueReader;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        Object value = valueReader.readJSONBObject(jsonReader, null, null, 0L);

        return (T) creator.apply(value);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        Object value = valueReader.readJSONBObject(jsonReader, null, null, 0L);
        return (T) creator.apply(value);
    }
}
