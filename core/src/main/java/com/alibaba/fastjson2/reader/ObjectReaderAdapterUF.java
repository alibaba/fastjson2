package com.alibaba.fastjson2.reader;

import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectReaderAdapterUF extends ObjectReaderAdapter {
    public ObjectReaderAdapterUF(Class objectClass, String typeKey, String typeName, long features, Supplier creator, Function buildFunction, FieldReader[] fieldReaders) {
        super(objectClass, typeKey, typeName, features, creator, buildFunction, fieldReaders);
    }
}
