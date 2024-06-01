package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.LongFunction;

import java.lang.reflect.Type;

public final class ObjectReaderImplFromLong<T>
        extends ObjectReaderPrimitive<T> {
    final LongFunction<T> creator;

    public ObjectReaderImplFromLong(Class<T> objectClass, LongFunction creator) {
        super(objectClass);
        this.creator = creator;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return creator.apply(
                jsonReader.readInt64Value()
        );
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return creator.apply(
                jsonReader.readInt64Value()
        );
    }
}
