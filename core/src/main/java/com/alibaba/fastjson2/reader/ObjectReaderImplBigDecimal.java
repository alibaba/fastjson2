package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

final class ObjectReaderImplBigDecimal extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplBigDecimal INSTANCE = new ObjectReaderImplBigDecimal();

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readBigDecimal();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readBigDecimal();
    }
}
