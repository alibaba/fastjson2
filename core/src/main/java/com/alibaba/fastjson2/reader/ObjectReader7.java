package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;

import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectReader7<T>
        extends ObjectReaderAdapter<T> {
    protected final FieldReader fieldReader0;
    protected final FieldReader fieldReader1;
    protected final FieldReader fieldReader2;
    protected final FieldReader fieldReader3;
    protected final FieldReader fieldReader4;
    protected final FieldReader fieldReader5;
    protected final FieldReader fieldReader6;

    final long hashCode0;
    final long hashCode1;
    final long hashCode2;
    final long hashCode3;
    final long hashCode4;
    final long hashCode5;
    final long hashCode6;
    final long hashCode0LCase;
    final long hashCode1LCase;
    final long hashCode2LCase;
    final long hashCode3LCase;
    final long hashCode4LCase;
    final long hashCode5LCase;
    final long hashCode6LCase;

    protected ObjectReader objectReader0;
    protected ObjectReader objectReader1;
    protected ObjectReader objectReader2;
    protected ObjectReader objectReader3;
    protected ObjectReader objectReader4;
    protected ObjectReader objectReader5;
    protected ObjectReader objectReader6;

    public ObjectReader7(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            JSONSchema schema,
            Supplier<T> creator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        super(objectClass, typeKey, typeName, features, schema, creator, buildFunction, fieldReaders);

        this.fieldReader0 = fieldReaders[0];
        this.fieldReader1 = fieldReaders[1];
        this.fieldReader2 = fieldReaders[2];
        this.fieldReader3 = fieldReaders[3];
        this.fieldReader4 = fieldReaders[4];
        this.fieldReader5 = fieldReaders[5];
        this.fieldReader6 = fieldReaders[6];

        this.hashCode0 = fieldReader0.fieldNameHash;
        this.hashCode1 = fieldReader1.fieldNameHash;
        this.hashCode2 = fieldReader2.fieldNameHash;
        this.hashCode3 = fieldReader3.fieldNameHash;
        this.hashCode4 = fieldReader4.fieldNameHash;
        this.hashCode5 = fieldReader5.fieldNameHash;
        this.hashCode6 = fieldReader6.fieldNameHash;

        this.hashCode0LCase = fieldReader0.fieldNameHashLCase;
        this.hashCode1LCase = fieldReader1.fieldNameHashLCase;
        this.hashCode2LCase = fieldReader2.fieldNameHashLCase;
        this.hashCode3LCase = fieldReader3.fieldNameHashLCase;
        this.hashCode4LCase = fieldReader4.fieldNameHashLCase;
        this.hashCode5LCase = fieldReader5.fieldNameHashLCase;
        this.hashCode6LCase = fieldReader6.fieldNameHashLCase;
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        if (hashCode == hashCode0) {
            return fieldReader0;
        }

        if (hashCode == hashCode1) {
            return fieldReader1;
        }

        if (hashCode == hashCode2) {
            return fieldReader2;
        }

        if (hashCode == hashCode3) {
            return fieldReader3;
        }

        if (hashCode == hashCode4) {
            return fieldReader4;
        }

        if (hashCode == hashCode5) {
            return fieldReader5;
        }

        if (hashCode == hashCode6) {
            return fieldReader6;
        }

        return null;
    }

    @Override
    public FieldReader getFieldReaderLCase(long hashCode) {
        if (hashCode == hashCode0LCase) {
            return fieldReader0;
        }

        if (hashCode == hashCode1LCase) {
            return fieldReader1;
        }

        if (hashCode == hashCode2LCase) {
            return fieldReader2;
        }

        if (hashCode == hashCode3LCase) {
            return fieldReader3;
        }

        if (hashCode == hashCode4LCase) {
            return fieldReader4;
        }

        if (hashCode == hashCode5LCase) {
            return fieldReader5;
        }

        if (hashCode == hashCode6LCase) {
            return fieldReader6;
        }

        return null;
    }
}
