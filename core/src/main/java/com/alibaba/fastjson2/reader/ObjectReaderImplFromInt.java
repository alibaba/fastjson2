package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.IntFunction;

import java.lang.reflect.Type;

public final class ObjectReaderImplFromInt<T>
        extends ObjectReaderPrimitive<T> {
    final IntFunction<T> creator;

    public ObjectReaderImplFromInt(Class<T> objectClass, IntFunction creator) {
        super(objectClass);
        this.creator = creator;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return creator.apply(
                jsonReader.readInt32Value()
        );
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return creator.apply(
                jsonReader.readInt32Value()
        );
    }
}
