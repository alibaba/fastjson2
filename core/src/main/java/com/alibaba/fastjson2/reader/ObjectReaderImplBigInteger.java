package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.math.BigInteger;

final class ObjectReaderImplBigInteger extends ObjectReaderBaseModule.PrimitiveImpl<BigInteger> {
    static final ObjectReaderImplBigInteger INSTANCE = new ObjectReaderImplBigInteger();

    @Override
    public Class getObjectClass() {
        return BigInteger.class;
    }

    @Override
    public BigInteger readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readBigInteger();
    }

    @Override
    public BigInteger readObject(JSONReader jsonReader, long features) {
        return jsonReader.readBigInteger();
    }
}
