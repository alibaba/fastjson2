package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.Function;

final class ObjectWriterImplBigDecimal
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplBigDecimal INSTANCE = new ObjectWriterImplBigDecimal(null, null);

    private final DecimalFormat format;

    final Function<Object, BigDecimal> function;

    public ObjectWriterImplBigDecimal(DecimalFormat format, Function<Object, BigDecimal> function) {
        this.format = format;
        this.function = function;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        BigDecimal decimal;
        if (function != null && object != null) {
            decimal = function.apply(object);
        } else {
            decimal = (BigDecimal) object;
        }
        jsonWriter.writeDecimal(decimal);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        BigDecimal decimal;
        if (function != null && object != null) {
            decimal = function.apply(object);
        } else {
            decimal = (BigDecimal) object;
        }

        if (format != null) {
            String str = format.format(object);
            jsonWriter.writeRaw(str);
            return;
        }

        jsonWriter.writeDecimal(decimal, features);
    }

    public Function getFunction() {
        return function;
    }
}
