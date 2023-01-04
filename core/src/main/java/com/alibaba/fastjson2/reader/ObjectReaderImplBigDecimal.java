package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.impl.ToBigDecimal;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

final class ObjectReaderImplBigDecimal
        extends ObjectReaderPrimitive {
    private Function converter = new ToBigDecimal();
    static final ObjectReaderImplBigDecimal INSTANCE = new ObjectReaderImplBigDecimal();

    public ObjectReaderImplBigDecimal() {
        super(BigDecimal.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readBigDecimal();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readBigDecimal();
    }

    @Override
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
