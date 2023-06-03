package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplInt16
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplInt16 INSTANCE = new ObjectWriterImplInt16();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        short shortValue = (Short) object;
        jsonWriter.writeInt16(shortValue);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        short shortValue = (Short) object;
        jsonWriter.writeInt32(shortValue);
        long features2 = jsonWriter.getFeatures(features);
        if ((features2 & JSONWriter.Feature.WriteClassName.mask) != 0
                && (features2 & JSONWriter.Feature.WriteNonStringKeyAsString.mask) == 0
                && (features2 & JSONWriter.Feature.NotWriteNumberClassName.mask) == 0
                && fieldType != Short.class && fieldType != short.class) {
            jsonWriter.writeRaw('S');
        }
    }
}
