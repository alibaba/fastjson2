package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;

import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectReaderAdapterUF
        extends ObjectReaderAdapter {
    public ObjectReaderAdapterUF(Class objectClass, String typeKey, String typeName, long features, JSONSchema schema, Supplier creator, Function buildFunction, FieldReader[] fieldReaders) {
        super(objectClass, typeKey, typeName, features, schema, creator, buildFunction, fieldReaders);
    }
}
