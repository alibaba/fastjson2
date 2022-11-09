package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;

abstract class ObjectReaderPrimitive<T>
        implements ObjectReader<T> {
    protected final Class objectClass;

    public ObjectReaderPrimitive(Class objectClass) {
        this.objectClass = objectClass;
    }

    @Override
    public Class getObjectClass() {
        return objectClass;
    }

    @Override
    public T createInstance(long features) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        return null;
    }

    @Override
    public abstract T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features);
}
