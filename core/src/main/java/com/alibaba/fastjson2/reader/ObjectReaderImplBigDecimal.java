package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

final class ObjectReaderImplBigDecimal
        extends ObjectReaderBaseModule.PrimitiveImpl {
    private Function converter = new TypeConverts.ToBigDecimal();
    static final ObjectReaderImplBigDecimal INSTANCE = new ObjectReaderImplBigDecimal();

    @Override
    public Class getObjectClass() {
        return BigDecimal.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readBigDecimal();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readBigDecimal();
    }

    public Object createInstance(Map map, long features) {
        Object value = map.get("value");
        if (value == null) {
            value = map.get("$numberDecimal");
        }

        if (!(value instanceof BigDecimal)) {
            value = converter.apply(value);
        }

        return value;
    }
}
