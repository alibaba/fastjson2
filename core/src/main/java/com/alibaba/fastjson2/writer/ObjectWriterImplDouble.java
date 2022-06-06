package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplDouble
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplDouble INSTANCE = new ObjectWriterImplDouble();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeDouble(((Double) object).doubleValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeDouble(((Double) object).doubleValue());
        if (((jsonWriter.getFeatures() | features) & JSONWriter.Feature.WriteClassName.mask) != 0
                && fieldType != Double.class && fieldType != double.class) {
            jsonWriter.writeRaw('D');
        }
    }
}
