package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.function.impl.ToBigDecimal;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;

final class ObjectReaderImplBigDecimal
        extends ObjectReaderPrimitive {
    private Function converter = new ToBigDecimal();
    static final ObjectReaderImplBigDecimal INSTANCE = new ObjectReaderImplBigDecimal(null);

    final Function<BigDecimal, Object> function;

    public ObjectReaderImplBigDecimal(Function<BigDecimal, Object> function) {
        super(BigDecimal.class);
        this.function = function;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        BigDecimal decimal = jsonReader.readBigDecimal();
        if (function != null) {
            return function.apply(decimal);
        }
        return decimal;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        BigDecimal decimal = jsonReader.readBigDecimal();
        if (function != null) {
            return function.apply(decimal);
        }
        return decimal;
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

        BigDecimal decimal = (BigDecimal) value;
        if (function != null) {
            return function.apply(decimal);
        }

        return decimal;
    }
}
