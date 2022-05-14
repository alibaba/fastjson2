package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ObjectReaderInterface<T> extends ObjectReaderAdapter<T> {
    public ObjectReaderInterface(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            Supplier creator,
            Function buildFunction,
            FieldReader[] fieldReaders
    ) {
        super(objectClass, typeKey, typeName, features, creator, buildFunction, fieldReaders);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, long features) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T createInstance(long features) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T createInstance(Map map, long features) {
        throw new UnsupportedOperationException();
    }
}
