package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.math.BigInteger;

final class ObjectReaderImplBigInteger
        extends ObjectReaderPrimitive<BigInteger> {
    static final ObjectReaderImplBigInteger INSTANCE = new ObjectReaderImplBigInteger();

    public ObjectReaderImplBigInteger() {
        super(BigInteger.class);
    }

    @Override
    public BigInteger readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readBigInteger();
    }

    @Override
    public BigInteger readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readBigInteger();
    }
}
