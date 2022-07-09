package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.function.Function;

public final class ObjectReaderImplFromString<T>
        extends ObjectReaderBaseModule.PrimitiveImpl<T> {
    final Function<String, T> creator;
    final Class objectClass;

    public ObjectReaderImplFromString(Class<T> objectClass, Function<String, T> creator) {
        this.objectClass = objectClass;
        this.creator = creator;
    }

    @Override
    public Class getObjectClass() {
        return objectClass;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        String str = jsonReader.readString();
        if (str == null || str.isEmpty()) {
            return null;
        }

        return creator.apply(str);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        String str = jsonReader.readString();
        if (str == null) {
            return null;
        }

        return creator.apply(str);
    }
}
