package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;

final class ObjectWriterImplBigDecimal
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplBigDecimal INSTANCE = new ObjectWriterImplBigDecimal(null);

    private final DecimalFormat format;

    public ObjectWriterImplBigDecimal(DecimalFormat format) {
        this.format = format;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeDecimal((BigDecimal) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        BigDecimal decimal = (BigDecimal) object;

        if (format != null) {
            String str = format.format(object);
            jsonWriter.writeRaw(str);
            return;
        }

        jsonWriter.writeDecimal(decimal, features);
    }
}
