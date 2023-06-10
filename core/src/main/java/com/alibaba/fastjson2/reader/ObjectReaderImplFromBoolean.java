package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.Function;

import java.lang.reflect.Type;

public final class ObjectReaderImplFromBoolean<T>
        extends ObjectReaderPrimitive<T> {
    final Function<Boolean, T> creator;

    public ObjectReaderImplFromBoolean(Class<T> objectClass, Function<Boolean, T> creator) {
        super(objectClass);
        this.creator = creator;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return creator.apply(
                jsonReader.readBoolValue()
        );
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return creator.apply(
                jsonReader.readBoolValue()
        );
    }
}
