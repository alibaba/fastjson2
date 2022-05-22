package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;

final class ObjectWriterImplBigDecimal
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplBigDecimal INSTANCE = new ObjectWriterImplBigDecimal();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeDecimal((BigDecimal) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        BigDecimal decimal = (BigDecimal) object;
        jsonWriter.writeDecimal(decimal, features);
    }
}
